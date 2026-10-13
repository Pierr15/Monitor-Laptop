package com.aetherglass.data.api

import com.aetherglass.domain.model.Command
import com.aetherglass.domain.model.CommandRequest
import com.aetherglass.domain.model.CommandResponse
import com.aetherglass.domain.model.Device
import com.aetherglass.domain.model.DeviceRegistrationRequest
import com.aetherglass.domain.model.DeviceRegistrationResponse
import com.aetherglass.domain.model.FileEntry
import com.aetherglass.domain.model.FileListing
import com.aetherglass.domain.model.MetricSample
import com.aetherglass.domain.model.SystemMetrics
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * API service interface for Windows agent communication.
 * Implemented via Ktor client.
 */
interface AgentApiService {

    // System info
    @GET("/api/v1/system/summary")
    suspend fun getSystemSummary(): Result<SystemSummaryResponse>

    @GET("/api/v1/metrics/current")
    suspend fun getCurrentMetrics(): Result<SystemMetrics>

    @GET("/api/v1/metrics/history")
    suspend fun getMetricHistory(
        range: String = "1h",
        metricType: String? = null
    ): Result<List<MetricSample>>

    @GET("/api/v1/hardware/sensors")
    suspend fun getSensors(): Result<SensorMetricsResponse>

    @GET("/api/v1/storage/volumes")
    suspend fun getStorageVolumes(): Result<List<StorageVolumeResponse>>

    @GET("/api/v1/network/status")
    suspend fun getNetworkStatus(): Result<NetworkStatusResponse>

    @GET("/api/v1/services/remote-access")
    suspend fun getRemoteAccessServices(): Result<RemoteAccessResponse>

    // Processes
    @GET("/api/v1/processes")
    suspend fun getProcesses(): Result<ProcessListResponse>

    @POST("/api/v1/processes/{pid}/terminate")
    suspend fun terminateProcess(
        pid: Int,
        request: TerminateProcessRequest
    ): Result<CommandResponse>

    // Files
    @GET("/api/v1/files")
    suspend fun listFiles(
        path: String
    ): Result<FileListing>

    @GET("/api/v1/files/download")
    suspend fun downloadFile(
        path: String
    ): HttpResponse // Streaming response

    @POST("/api/v1/files/upload")
    suspend fun uploadFile(
        request: UploadFileRequest
    ): Result<FileUploadResponse>

    // Power
    @POST("/api/v1/power/sleep")
    suspend fun sleep(request: PowerRequest): Result<CommandResponse>

    @POST("/api/v1/power/restart")
    suspend fun restart(request: PowerRequest): Result<CommandResponse>

    @POST("/api/v1/power/shutdown")
    suspend fun shutdown(request: PowerRequest): Result<CommandResponse>

    // Commands
    @GET("/api/v1/commands/{id}")
    suspend fun getCommandStatus(id: UUID): Result<CommandResponse>

    // WebSocket
    fun connectWebSocket(): Flow<AgentWebSocketEvent>
}

/**
 * API service interface for Supabase communication.
 */
interface SupabaseApiService {
    // Auth
    @POST("/auth/v1/token?grant_type=password")
    suspend fun signIn(request: SignInRequest): Result<AuthResponse>

    @POST("/auth/v1/signup")
    suspend fun signUp(request: SignUpRequest): Result<AuthResponse>

    @POST("/auth/v1/token?grant_type=refresh_token")
    suspend fun refreshToken(request: RefreshTokenRequest): Result<AuthResponse>

    @POST("/auth/v1/logout")
    suspend fun signOut(): Result<Unit>

    @GET("/auth/v1/user")
    suspend fun getUser(accessToken: String): Result<UserResponse>

    // Devices
    @POST("/rest/v1/devices")
    suspend fun registerDevice(request: DeviceRegistrationRequest): Result<DeviceRegistrationResponse>

    @GET("/rest/v1/devices")
    suspend fun getDevices(): Result<List<Device>>

    @GET("/rest/v1/devices")
    suspend fun getDevice(id: UUID): Result<Device>

    @PATCH("/rest/v1/devices")
    suspend fun updateDevice(device: Device): Result<Device>

    @DELETE("/rest/v1/devices")
    suspend fun deleteDevice(id: UUID): Result<Unit>

    // Metrics
    @POST("/rest/v1/metric_samples")
    suspend fun insertMetrics(samples: List<MetricSample>): Result<Unit>

    @GET("/rest/v1/metric_samples")
    suspend fun getMetrics(
        deviceId: UUID,
        metricType: String,
        from: Long,
        to: Long,
        limit: Int
    ): Result<List<MetricSample>>

    // Commands
    @POST("/rest/v1/command_audit")
    suspend fun insertCommandAudit(audit: CommandAudit): Result<Unit>

    @GET("/rest/v1/command_audit")
    suspend fun getCommandHistory(
        deviceId: UUID,
        limit: Int
    ): Result<List<CommandAudit>>

    // Alerts
    @POST("/rest/v1/alert_events")
    suspend fun insertAlert(alert: com.aetherglass.domain.model.AlertEvent): Result<Unit>

    @GET("/rest/v1/alert_events")
    suspend fun getAlertHistory(
        deviceId: UUID,
        limit: Int
    ): Result<List<com.aetherglass.domain.model.AlertEvent>>

    // Settings
    @GET("/rest/v1/device_settings")
    suspend fun getDeviceSettings(deviceId: UUID): Result<DeviceSettingsResponse>

    @POST("/rest/v1/device_settings")
    suspend fun upsertDeviceSettings(request: DeviceSettingsRequest): Result<Unit>
}

// DTOs for API responses

@Serializable
data class SystemSummaryResponse(
    val deviceId: UUID,
    val name: String,
    val platform: String,
    val agentVersion: String,
    val uptimeSeconds: Long,
    val osVersion: String,
    val capabilities: com.aetherglass.domain.model.DeviceCapabilities
)

@Serializable
data class SensorMetricsResponse(
    val cpuTemperatureCelsius: Double?,
    val gpuTemperatureCelsius: Double?,
    val fanSpeedRpm: Int?,
    val gpuUtilizationPercent: Double?,
    val gpuMemoryUsedBytes: Long?,
    val gpuMemoryTotalBytes: Long?
)

@Serializable
data class StorageVolumeResponse(
    val id: String,
    val label: String,
    val path: String,
    val totalBytes: Long,
    val freeBytes: Long,
    val type: String,
    val fileSystem: String?
)

@Serializable
data class NetworkStatusResponse(
    val interfaces: List<NetworkInterfaceResponse>,
    val latencyMs: Int?,
    val isConnected: Boolean,
    val tailscale: TailscaleStatusResponse
)

@Serializable
data class NetworkInterfaceResponse(
    val name: String,
    val displayName: String,
    val ipAddresses: List<String>,
    val macAddress: String?,
    val speedBps: Long?,
    val bytesSent: Long,
    val bytesReceived: Long,
    val isUp: Boolean,
    val type: String
)

@Serializable
data class TailscaleStatusResponse(
    val isConnected: Boolean,
    val ipAddress: String?,
    val peerCount: Int,
    val backendState: String?,
    val lastHandshake: Long?
)

@Serializable
data class RemoteAccessResponse(
    val ssh: ServiceStatus,
    val rdp: ServiceStatus,
    val tailscale: ServiceStatus
)

@Serializable
data class ServiceStatus(
    val enabled: Boolean,
    val running: Boolean,
    val port: Int?,
    val lastCheck: Long
)

@Serializable
data class ProcessListResponse(
    val processes: List<ProcessInfoResponse>
)

@Serializable
data class ProcessInfoResponse(
    val pid: Int,
    val name: String,
    val cpuPercent: Double,
    val memoryBytes: Long,
    val memoryPercent: Double,
    val status: String,
    val user: String?,
    val startTime: Long?,
    val isSystem: Boolean,
    val isProtected: Boolean,
    val commandLine: String?,
    val executablePath: String?
)

@Serializable
data class TerminateProcessRequest(
    val force: Boolean = false
)

@Serializable
data class UploadFileRequest(
    val path: String,
    val contentBase64: String,
    val overwrite: Boolean = false
)

@Serializable
data class FileUploadResponse(
    val success: Boolean,
    val path: String,
    val size: Long
)

@Serializable
data class PowerRequest(
    val delaySeconds: Int = 0,
    val idempotencyKey: String = UUID.randomUUID().toString()
)

@Serializable
data class SignInRequest(
    val email: String,
    val password: String
)

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val data: Map<String, String> = emptyMap()
)

@Serializable
data class RefreshTokenRequest(
    val refresh_token: String
)

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
    val user_metadata: Map<String, String>,
    val created_at: String
)

@Serializable
data class DeviceSettingsResponse(
    val settings: com.aetherglass.domain.model.DeviceSettings
)

@Serializable
data class DeviceSettingsRequest(
    val deviceId: UUID,
    val ownerId: UUID,
    val settings: com.aetherglass.domain.model.DeviceSettings
)

/**
 * WebSocket events from agent.
 */
sealed class AgentWebSocketEvent {
    @Serializable
    data class MetricsUpdate(val metrics: SystemMetrics) : AgentWebSocketEvent()

    @Serializable
    data class DeviceStatus(val status: String, val timestamp: Long) : AgentWebSocketEvent()

    @Serializable
    data class ServiceStatus(val service: String, val running: Boolean) : AgentWebSocketEvent()

    @Serializable
    data class AlertTriggered(val alert: com.aetherglass.domain.model.AlertEvent) : AgentWebSocketEvent()

    @Serializable
    data class CommandStatus(val commandId: UUID, val status: String, val result: String?) : AgentWebSocketEvent()

    @Serializable
    data class Pong(val requestId: String) : AgentWebSocketEvent()
}