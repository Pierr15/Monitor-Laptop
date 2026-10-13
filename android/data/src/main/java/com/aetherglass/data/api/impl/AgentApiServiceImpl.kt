package com.aetherglass.data.api.impl

import com.aetherglass.data.api.AgentApiService
import com.aetherglass.data.api.SystemMetricsResponse
import com.aetherglass.data.api.SystemSummaryResponse
import com.aetherglass.data.api.SensorMetricsResponse
import com.aetherglass.data.api.StorageVolumeResponse
import com.aetherglass.data.api.NetworkStatusResponse
import com.aetherglass.data.api.RemoteAccessResponse
import com.aetherglass.data.api.ProcessListResponse
import com.aetherglass.data.api.CommandResponse
import com.aetherglass.data.api.TerminateProcessRequest
import com.aetherglass.data.api.FileListing
import com.aetherglass.data.api.UploadFileRequest
import com.aetherglass.data.api.FileUploadResponse
import com.aetherglass.data.api.PowerRequest
import com.aetherglass.data.api.AgentWebSocketEvent
import com.aetherglass.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import java.net.URL
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentApiServiceImpl @Inject constructor(
    private val client: HttpClient,
    private val baseUrl: String
) : AgentApiService {

    override suspend fun getSystemSummary(): Result<SystemSummaryResponse> {
        return request { client.get("$baseUrl/api/v1/system/summary") }
    }

    override suspend fun getCurrentMetrics(): Result<SystemMetricsResponse> {
        return request { client.get("$baseUrl/api/v1/metrics/current") }
    }

    override suspend fun getMetricHistory(range: String = "1h", metricType: String? = null): Result<List<com.aetherglass.data.api.MetricSampleResponse>> {
        var url = "$baseUrl/api/v1/metrics/history?range=$range"
        metricType?.let { url += "&metric_type=$it" }
        return request { client.get(url) }
    }

    override suspend fun getSensors(): Result<SensorMetricsResponse> {
        return request { client.get("$baseUrl/api/v1/hardware/sensors") }
    }

    override suspend fun getStorageVolumes(): Result<List<StorageVolumeResponse>> {
        return request { client.get("$baseUrl/api/v1/storage/volumes") }
    }

    override suspend fun getNetworkStatus(): Result<NetworkStatusResponse> {
        return request { client.get("$baseUrl/api/v1/network/status") }
    }

    override suspend fun getRemoteAccessServices(): Result<RemoteAccessResponse> {
        return request { client.get("$baseUrl/api/v1/services/remote-access") }
    }

    override suspend fun getProcesses(): Result<ProcessListResponse> {
        return request { client.get("$baseUrl/api/v1/processes") }
    }

    override suspend fun terminateProcess(pid: Int, request: TerminateProcessRequest): Result<CommandResponse> {
        return request { client.post("$baseUrl/api/v1/processes/$pid/terminate") { body = request } }
    }

    override suspend fun listFiles(path: String): Result<FileListing> {
        return request { client.get("$baseUrl/api/v1/files?path=${path.encodeUrl()}") }
    }

    override suspend fun downloadFile(path: String): HttpResponse {
        return client.get("$baseUrl/api/v1/files/download?path=${path.encodeUrl()}") { }
    }

    override suspend fun uploadFile(request: UploadFileRequest): Result<FileUploadResponse> {
        return request { client.post("$baseUrl/api/v1/files/upload") { body = request } }
    }

    override suspend fun sleep(request: PowerRequest): Result<CommandResponse> {
        return request { client.post("$baseUrl/api/v1/power/sleep") { body = request } }
    }

    override suspend fun restart(request: PowerRequest): Result<CommandResponse> {
        return request { client.post("$baseUrl/api/v1/power/restart") { body = request } }
    }

    override suspend fun shutdown(request: PowerRequest): Result<CommandResponse> {
        return request { client.post("$baseUrl/api/v1/power/shutdown") { body = request } }
    }

    override suspend fun getCommandStatus(id: UUID): Result<CommandResponse> {
        return request { client.get("$baseUrl/api/v1/commands/$id") }
    }

    override fun connectWebSocket(): Flow<AgentWebSocketEvent> {
        return flow {
            // TODO: Implement WebSocket connection using Ktor WebSocket client
            // This is a placeholder - actual implementation would use client.webSocket()
        }
    }

    private suspend fun <T> request(block: suspend () -> T): Result<T> {
        return com.aetherglass.util.runCatching {
            val response = block()
            response
        }
    }
}