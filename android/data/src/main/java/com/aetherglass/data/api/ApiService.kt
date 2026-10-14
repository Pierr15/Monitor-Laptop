package com.aetherglass.data.api

import com.aetherglass.core.util.Result
import com.aetherglass.domain.model.Device
import com.aetherglass.domain.model.DeviceRegistrationRequest as DomainDeviceRegistrationRequest
import com.aetherglass.domain.model.DeviceRegistrationResponse as DomainDeviceRegistrationResponse
import com.aetherglass.domain.model.SystemMetrics
import com.aetherglass.domain.model.AlertEvent as DomainAlertEvent
import com.aetherglass.domain.model.CommandAudit as DomainCommandAudit
import com.aetherglass.domain.model.CommandResponse as DomainCommandResponse
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import java.util.UUID

// Domain-backed aliases used by both Android repositories and the API client.
typealias DeviceRegistrationRequest = DomainDeviceRegistrationRequest
typealias DeviceRegistrationResponse = DomainDeviceRegistrationResponse
typealias CommandResponse = DomainCommandResponse
typealias CommandAudit = DomainCommandAudit
typealias AlertEvent = DomainAlertEvent

/**
 * Typed contract for the Windows agent REST API.
 * The implementation uses Ktor directly; Retrofit annotations do not apply.
 */
interface AgentApiService {
    suspend fun getSystemSummary(): Result<SystemSummaryResponse>
    suspend fun getCurrentMetrics(): Result<SystemMetricsResponse>
    suspend fun getMetricHistory(range: String = "1h", metricType: String? = null): Result<List<MetricSampleResponse>>
    suspend fun getSensors(): Result<SensorMetricsResponse>
    suspend fun getStorageVolumes(): Result<List<StorageVolumeResponse>>
    suspend fun getNetworkStatus(): Result<NetworkStatusResponse>
    suspend fun getRemoteAccessServices(): Result<RemoteAccessResponse>
    suspend fun getProcesses(): Result<ProcessListResponse>
    suspend fun terminateProcess(pid: Int, request: TerminateProcessRequest): Result<CommandResponse>
    suspend fun listFiles(path: String): Result<FileListingResponse>
    suspend fun downloadFile(path: String): HttpResponse
    suspend fun uploadFile(request: UploadFileRequest): Result<FileUploadResponse>
    suspend fun sleep(request: PowerRequest): Result<CommandResponse>
    suspend fun restart(request: PowerRequest): Result<CommandResponse>
    suspend fun shutdown(request: PowerRequest): Result<CommandResponse>
    suspend fun getCommandStatus(id: UUID): Result<CommandResponse>
    fun connectWebSocket(): Flow<AgentWebSocketEvent>
}

/**
 * Typed contract for Supabase Auth and PostgREST.
 * User-scoped database access still requires a real authenticated access token
 * and backend RLS policies; the public anon key is not an authorization bypass.
 */
interface SupabaseApiService {
    suspend fun signIn(request: SignInRequest): Result<AuthResponse>
    suspend fun signUp(request: SignUpRequest): Result<AuthResponse>
    suspend fun refreshToken(request: RefreshTokenRequest): Result<AuthResponse>
    suspend fun signOut(): Result<Unit>
    suspend fun getUser(accessToken: String): Result<UserResponse>
    suspend fun registerDevice(request: DeviceRegistrationRequest): Result<DeviceRegistrationResponse>
    suspend fun getDevices(): Result<List<DeviceResponse>>
    suspend fun getDevice(id: UUID): Result<DeviceResponse>
    suspend fun updateDevice(device: Device): Result<DeviceResponse>
    suspend fun deleteDevice(id: UUID): Result<Unit>
    suspend fun insertMetrics(samples: List<MetricSampleResponse>): Result<Unit>
    suspend fun getMetrics(deviceId: UUID, metricType: String, from: Long, to: Long, limit: Int): Result<List<MetricSampleResponse>>
    suspend fun insertCommandAudit(audit: CommandAudit): Result<Unit>
    suspend fun getCommandHistory(deviceId: UUID, limit: Int): Result<List<CommandAudit>>
    suspend fun insertAlert(alert: AlertEvent): Result<Unit>
    suspend fun getAlertHistory(deviceId: UUID, limit: Int): Result<List<AlertEvent>>
    suspend fun getDeviceSettings(deviceId: UUID): Result<DeviceSettingsResponse>
    suspend fun upsertDeviceSettings(request: DeviceSettingsRequest): Result<Unit>
}

@Serializable
data class TerminateProcessRequest(val force: Boolean = false)

@Serializable
data class UploadFileRequest(
    val path: String,
    val contentBase64: String,
    val overwrite: Boolean = false
)

@Serializable
data class PowerRequest(
    val delaySeconds: Int = 0,
    val idempotencyKey: String = UUID.randomUUID().toString()
)

@Serializable
data class SignInRequest(val email: String, val password: String)

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val data: Map<String, String> = emptyMap()
)

@Serializable
data class RefreshTokenRequest(val refresh_token: String)

@Serializable
data class AuthResponse(
    val access_token: String,
    val refresh_token: String,
    val expires_in: Long,
    val token_type: String,
    val user: UserResponse
)

@Serializable
data class UserResponse(
    val id: UUID,
    val email: String,
    val user_metadata: Map<String, String> = emptyMap(),
    val created_at: String
)

sealed class AgentWebSocketEvent {
    @Serializable
    data class MetricsUpdate(val metrics: SystemMetrics) : AgentWebSocketEvent()

    @Serializable
    data class DeviceStatus(val status: String, val timestamp: Long) : AgentWebSocketEvent()

    @Serializable
    data class ServiceStatus(val service: String, val running: Boolean) : AgentWebSocketEvent()

    @Serializable
    data class AlertTriggered(val alert: AlertEvent) : AgentWebSocketEvent()

    @Serializable
    data class CommandStatus(val commandId: UUID, val status: String, val result: String?) : AgentWebSocketEvent()

    @Serializable
    data class Pong(val requestId: String) : AgentWebSocketEvent()
}
