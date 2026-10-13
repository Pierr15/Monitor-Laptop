package com.aetherglass.data.api.impl

import com.aetherglass.data.api.DeviceRegistrationRequest
import com.aetherglass.data.api.DeviceRegistrationResponse
import com.aetherglass.data.api.DeviceResponse
import com.aetherglass.data.api.DeviceSettingsRequest
import com.aetherglass.data.api.DeviceSettingsResponse
import com.aetherglass.data.api.MetricSampleResponse
import com.aetherglass.data.api.CommandAudit
import com.aetherglass.data.api.AlertEvent
import com.aetherglass.data.api.AuthResponse
import com.aetherglass.data.api.SignInRequest
import com.aetherglass.data.api.SignUpRequest
import com.aetherglass.data.api.RefreshTokenRequest
import com.aetherglass.data.api.UserResponse
import com.aetherglass.data.api.SupabaseApiService
import com.aetherglass.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.patch
import io.ktor.client.request.delete
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseApiServiceImpl @Inject constructor(
    private val client: HttpClient,
    private val baseUrl: String,
    private val anonKey: String
) : SupabaseApiService {

    private suspend fun addAuthHeaders(requestBuilder: io.ktor.client.request.RequestBuilder) {
        // In production, get token from secure storage
        // requestBuilder.header(HttpHeaders.Authorization, "Bearer $accessToken")
        requestBuilder.header(HttpHeaders.Authorization, "Bearer $anonKey")
        requestBuilder.header("apikey", anonKey)
    }

    override suspend fun signIn(request: SignInRequest): Result<AuthResponse> {
        return requestWithAuth { client.post("$baseUrl/auth/v1/token?grant_type=password") { body = request } }
    }

    override suspend fun signUp(request: SignUpRequest): Result<AuthResponse> {
        return requestWithAuth { client.post("$baseUrl/auth/v1/signup") { body = request } }
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): Result<AuthResponse> {
        return requestWithAuth { client.post("$baseUrl/auth/v1/token?grant_type=refresh_token") { body = request } }
    }

    override suspend fun signOut(): Result<Unit> {
        return requestWithAuth { client.post("$baseUrl/auth/v1/logout") }
    }

    override suspend fun getUser(accessToken: String): Result<UserResponse> {
        return com.aetherglass.util.runCatching {
            client.get("$baseUrl/auth/v1/user") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header("apikey", anonKey)
            }.body()
        }
    }

    override suspend fun registerDevice(request: DeviceRegistrationRequest): Result<DeviceRegistrationResponse> {
        return requestWithAuth { client.post("$baseUrl/rest/v1/devices") { body = request } }
    }

    override suspend fun getDevices(): Result<List<DeviceResponse>> {
        return requestWithAuth { client.get("$baseUrl/rest/v1/devices?select=*") }
    }

    override suspend fun getDevice(id: UUID): Result<DeviceResponse> {
        return requestWithAuth { client.get("$baseUrl/rest/v1/devices?id=eq.$id&select=*") }.map { it.firstOrNull()!! }
    }

    override suspend fun updateDevice(device: com.aetherglass.domain.model.Device): Result<DeviceResponse> {
        // Convert domain to API request
        val apiRequest = com.aetherglass.data.mapper.DeviceMapper.toApi(device)
        return requestWithAuth { 
            client.patch("$baseUrl/rest/v1/devices?id=eq.${device.id}") { 
                body = apiRequest
                header("Prefer", "return=representation")
            } 
        }
    }

    override suspend fun deleteDevice(id: UUID): Result<Unit> {
        return requestWithAuth { client.delete("$baseUrl/rest/v1/devices?id=eq.$id") }
    }

    override suspend fun insertMetrics(samples: List<MetricSampleResponse>): Result<Unit> {
        return requestWithAuth { client.post("$baseUrl/rest/v1/metric_samples") { body = samples } }
    }

    override suspend fun getMetrics(deviceId: UUID, metricType: String, from: Long, to: Long, limit: Int): Result<List<MetricSampleResponse>> {
        val url = "$baseUrl/rest/v1/metric_samples?device_id=eq.$deviceId&metric_type=eq.$metricType&recorded_at=gte.$from&recorded_at=lte.$to&order=recorded_at.desc&limit=$limit"
        return requestWithAuth { client.get(url) }
    }

    override suspend fun insertCommandAudit(audit: CommandAudit): Result<Unit> {
        return requestWithAuth { client.post("$baseUrl/rest/v1/command_audit") { body = audit } }
    }

    override suspend fun getCommandHistory(deviceId: UUID, limit: Int): Result<List<CommandAudit>> {
        val url = "$baseUrl/rest/v1/command_audit?device_id=eq.$deviceId&order=requested_at.desc&limit=$limit"
        return requestWithAuth { client.get(url) }
    }

    override suspend fun insertAlert(alert: AlertEvent): Result<Unit> {
        return requestWithAuth { client.post("$baseUrl/rest/v1/alert_events") { body = alert } }
    }

    override suspend fun getAlertHistory(deviceId: UUID, limit: Int): Result<List<AlertEvent>> {
        val url = "$baseUrl/rest/v1/alert_events?device_id=eq.$deviceId&order=created_at.desc&limit=$limit"
        return requestWithAuth { client.get(url) }
    }

    override suspend fun getDeviceSettings(deviceId: UUID): Result<DeviceSettingsResponse> {
        val url = "$baseUrl/rest/v1/device_settings?device_id=eq.$deviceId&select=*&limit=1"
        return requestWithAuth { client.get(url) }.map { it.firstOrNull()!! }
    }

    override suspend fun upsertDeviceSettings(request: DeviceSettingsRequest): Result<Unit> {
        return requestWithAuth { 
            client.post("$baseUrl/rest/v1/device_settings") { 
                body = request
                header("Prefer", "resolution=merge-duplicates")
            } 
        }
    }

    private suspend fun <T> requestWithAuth(block: suspend () -> T): Result<T> {
        return com.aetherglass.util.runCatching {
            val response = block()
            response
        }
    }
}