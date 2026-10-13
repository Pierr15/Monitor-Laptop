package com.aetherglass.data.api

import kotlinx.serialization.Serializable
import java.util.UUID

// System Metrics DTOs
@Serializable
data class SystemMetricsResponse(
    val deviceId: UUID,
    val timestamp: Long,
    val cpu: CpuMetricsResponse,
    val memory: MemoryMetricsResponse,
    val battery: BatteryMetricsResponse?,
    val storage: List<StorageVolumeResponse>,
    val network: NetworkStatusResponse,
    val processes: List<ProcessInfoResponse>,
    val sensors: SensorMetricsResponse,
    val agent: AgentHealthResponse
)

@Serializable
data class CpuMetricsResponse(
    val usagePercent: Double,
    val cores: Int,
    val frequencyMHz: Int?,
    val temperatureCelsius: Double?,
    val loadAverage1m: Double?,
    val loadAverage5m: Double?,
    val loadAverage15m: Double?
)

@Serializable
data class MemoryMetricsResponse(
    val totalBytes: Long,
    val usedBytes: Long,
    val availableBytes: Long,
    val swapTotalBytes: Long,
    val swapUsedBytes: Long
)

@Serializable
data class BatteryMetricsResponse(
    val levelPercent: Int,
    val isCharging: Boolean,
    val isPresent: Boolean,
    val health: String,
    val technology: String?,
    val voltageMv: Int?,
    val temperatureCelsius: Double?,
    val capacityMah: Int?,
    val timeRemainingMinutes: Int?
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
data class SensorMetricsResponse(
    val cpuTemperatureCelsius: Double?,
    val gpuTemperatureCelsius: Double?,
    val fanSpeedRpm: Int?,
    val gpuUtilizationPercent: Double?,
    val gpuMemoryUsedBytes: Long?,
    val gpuMemoryTotalBytes: Long?
)

@Serializable
data class AgentHealthResponse(
    val version: String,
    val uptimeSeconds: Long,
    val lastCollectionMs: Long,
    val collectionErrors: Int,
    val isHealthy: Boolean,
    val warnings: List<String>
)

@Serializable
data class MetricSampleResponse(
    val id: UUID,
    val deviceId: UUID,
    val recordedAt: Long,
    val metricType: String,
    val numericValue: Double,
    val unit: String,
    val metadata: Map<String, String>
)

// Device DTOs
@Serializable
data class DeviceResponse(
    val id: UUID,
    val ownerId: UUID,
    val name: String,
    val platform: String,
    val agentVersion: String?,
    val registeredAt: Long,
    val lastSeenAt: Long?,
    val capabilities: DeviceCapabilitiesResponse,
    val settings: DeviceSettingsResponse,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class DeviceCapabilitiesResponse(
    val cpuMonitoring: Boolean,
    val memoryMonitoring: Boolean,
    val batteryMonitoring: Boolean,
    val storageMonitoring: Boolean,
    val networkMonitoring: Boolean,
    val processListing: Boolean,
    val processTermination: Boolean,
    val fileBrowsing: Boolean,
    val fileUpload: Boolean,
    val fileDownload: Boolean,
    val powerSleep: Boolean,
    val powerRestart: Boolean,
    val powerShutdown: Boolean,
    val sensorTemperature: Boolean,
    val sensorFanSpeed: Boolean,
    val sensorGpu: Boolean,
    val websocketStreaming: Boolean,
    val tailscaleIntegration: Boolean
)

@Serializable
data class DeviceSettingsResponse(
    val updateIntervalForegroundMs: Int,
    val updateIntervalBackgroundMs: Int,
    val retentionDays: Int,
    val enablePushNotifications: Boolean,
    val alertCpuPercent: Int,
    val alertMemoryPercent: Int,
    val alertBatteryPercent: Int,
    val alertStoragePercent: Int,
    val alertNetworkLatencyMs: Int,
    val allowlistedPaths: List<String>,
    val blockedProcessNames: List<String>
)

// Request DTOs for device
@Serializable
data class DeviceRequest(
    val id: UUID,
    val ownerId: UUID,
    val name: String,
    val platform: String,
    val agentVersion: String?,
    val registeredAt: Long,
    val lastSeenAt: Long?,
    val capabilities: DeviceCapabilitiesRequest,
    val settings: DeviceSettingsRequest,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class DeviceCapabilitiesRequest(
    val cpuMonitoring: Boolean,
    val memoryMonitoring: Boolean,
    val batteryMonitoring: Boolean,
    val storageMonitoring: Boolean,
    val networkMonitoring: Boolean,
    val processListing: Boolean,
    val processTermination: Boolean,
    val fileBrowsing: Boolean,
    val fileUpload: Boolean,
    val fileDownload: Boolean,
    val powerSleep: Boolean,
    val powerRestart: Boolean,
    val powerShutdown: Boolean,
    val sensorTemperature: Boolean,
    val sensorFanSpeed: Boolean,
    val sensorGpu: Boolean,
    val websocketStreaming: Boolean,
    val tailscaleIntegration: Boolean
)

@Serializable
data class DeviceSettingsRequest(
    val updateIntervalForegroundMs: Int,
    val updateIntervalBackgroundMs: Int,
    val retentionDays: Int,
    val enablePushNotifications: Boolean,
    val alertCpuPercent: Int,
    val alertMemoryPercent: Int,
    val alertBatteryPercent: Int,
    val alertStoragePercent: Int,
    val alertNetworkLatencyMs: Int,
    val allowlistedPaths: List<String>,
    val blockedProcessNames: List<String>
)

// Remote Access DTOs
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

// Process DTOs
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

// File DTOs
@Serializable
data class FileEntryResponse(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val modifiedAt: Long,
    val permissions: String?,
    val mimeType: String?
)

@Serializable
data class FileListingResponse(
    val path: String,
    val entries: List<FileEntryResponse>,
    val parentPath: String?,
    val totalCount: Int
)

@Serializable
data class FileUploadResponse(
    val success: Boolean,
    val path: String,
    val size: Long
)

// System Summary DTO
@Serializable
data class SystemSummaryResponse(
    val deviceId: UUID,
    val name: String,
    val platform: String,
    val agentVersion: String,
    val uptimeSeconds: Long,
    val osVersion: String,
    val capabilities: DeviceCapabilitiesResponse
)