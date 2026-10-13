package com.aetherglass.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Current system metrics snapshot.
 */
@Serializable
data class SystemMetrics(
    val deviceId: UUID,
    val timestamp: Long = System.currentTimeMillis(),
    val cpu: CpuMetrics,
    val memory: MemoryMetrics,
    val battery: BatteryMetrics?,
    val storage: List<StorageVolume>,
    val network: NetworkMetrics,
    val processes: List<ProcessInfo> = emptyList(),
    val sensors: SensorMetrics = SensorMetrics(),
    val agent: AgentHealth = AgentHealth()
)

/**
 * CPU metrics.
 */
@Serializable
data class CpuMetrics(
    val usagePercent: Double,
    val cores: Int,
    val frequencyMHz: Int? = null,
    val temperatureCelsius: Double? = null,
    val loadAverage1m: Double? = null,
    val loadAverage5m: Double? = null,
    val loadAverage15m: Double? = null
) {
    val status: MetricStatus
        get() = when {
            usagePercent >= 90 -> MetricStatus.Danger
            usagePercent >= 75 -> MetricStatus.Warning
            else -> MetricStatus.Normal
        }
}

/**
 * Memory metrics.
 */
@Serializable
data class MemoryMetrics(
    val totalBytes: Long,
    val usedBytes: Long,
    val availableBytes: Long,
    val swapTotalBytes: Long = 0,
    val swapUsedBytes: Long = 0
) {
    val usagePercent: Double
        get() = if (totalBytes > 0) (usedBytes.toDouble() / totalBytes) * 100 else 0.0

    val status: MetricStatus
        get() = when {
            usagePercent >= 90 -> MetricStatus.Danger
            usagePercent >= 75 -> MetricStatus.Warning
            else -> MetricStatus.Normal
        }
}

/**
 * Battery metrics.
 */
@Serializable
data class BatteryMetrics(
    val levelPercent: Int,
    val isCharging: Boolean,
    val isPresent: Boolean = true,
    val health: BatteryHealth = BatteryHealth.Unknown,
    val technology: String? = null,
    val voltageMv: Int? = null,
    val temperatureCelsius: Double? = null,
    val capacityMah: Int? = null,
    val timeRemainingMinutes: Int? = null
) {
    val status: MetricStatus
        get() = when {
            !isPresent -> MetricStatus.Unknown
            levelPercent <= 10 -> MetricStatus.Danger
            levelPercent <= 20 -> MetricStatus.Warning
            else -> MetricStatus.Normal
        }
}

enum class BatteryHealth { Good, Overheat, Dead, OverVoltage, UnspecifiedFailure, Unknown }

/**
 * Storage volume metrics.
 */
@Serializable
data class StorageVolume(
    val id: String,
    val label: String,
    val path: String,
    val totalBytes: Long,
    val freeBytes: Long,
    val type: VolumeType = VolumeType.Fixed,
    val fileSystem: String? = null
) {
    val usedBytes: Long
        get() = totalBytes - freeBytes

    val usagePercent: Double
        get() = if (totalBytes > 0) (usedBytes.toDouble() / totalBytes) * 100 else 0.0

    val status: MetricStatus
        get() = when {
            usagePercent >= 95 -> MetricStatus.Danger
            usagePercent >= 85 -> MetricStatus.Warning
            else -> MetricStatus.Normal
        }
}

enum class VolumeType { Fixed, Removable, Network, CdRom, RamDisk, Unknown }

/**
 * Network metrics.
 */
@Serializable
data class NetworkMetrics(
    val interfaces: List<NetworkInterface> = emptyList(),
    val latencyMs: Int? = null,
    val isConnected: Boolean = false,
    val tailscale: TailscaleStatus = TailscaleStatus()
)

/**
 * Network interface info.
 */
@Serializable
data class NetworkInterface(
    val name: String,
    val displayName: String,
    val ipAddresses: List<String> = emptyList(),
    val macAddress: String? = null,
    val speedBps: Long? = null,
    val bytesSent: Long = 0,
    val bytesReceived: Long = 0,
    val isUp: Boolean = false,
    val type: InterfaceType = InterfaceType.Unknown
)

enum class InterfaceType { Ethernet, WiFi, Loopback, Tunnel, Unknown }

/**
 * Tailscale connection status.
 */
@Serializable
data class TailscaleStatus(
    val isConnected: Boolean = false,
    val ipAddress: String? = null,
    val peerCount: Int = 0,
    val backendState: String? = null,
    val lastHandshake: Long? = null
)

/**
 * Process information.
 */
@Serializable
data class ProcessInfo(
    val pid: Int,
    val name: String,
    val cpuPercent: Double = 0.0,
    val memoryBytes: Long = 0,
    val memoryPercent: Double = 0.0,
    val status: ProcessStatus = ProcessStatus.Unknown,
    val user: String? = null,
    val startTime: Long? = null,
    val isSystem: Boolean = false,
    val isProtected: Boolean = false,
    val commandLine: String? = null,
    val executablePath: String? = null
) {
    val isTerminatable: Boolean
        get() = !isSystem && !isProtected && pid > 0
}

enum class ProcessStatus { Running, Sleeping, Stopped, Zombie, Unknown }

/**
 * Sensor metrics (temperature, fan speed, etc.).
 */
@Serializable
data class SensorMetrics(
    val cpuTemperatureCelsius: Double? = null,
    val gpuTemperatureCelsius: Double? = null,
    val fanSpeedRpm: Int? = null,
    val gpuUtilizationPercent: Double? = null,
    val gpuMemoryUsedBytes: Long? = null,
    val gpuMemoryTotalBytes: Long? = null
)

/**
 * Agent health information.
 */
@Serializable
data class AgentHealth(
    val version: String = "unknown",
    val uptimeSeconds: Long = 0,
    val lastCollectionMs: Long = 0,
    val collectionErrors: Int = 0,
    val isHealthy: Boolean = true,
    val warnings: List<String> = emptyList()
)

/**
 * Metric status enum.
 */
enum class MetricStatus { Normal, Warning, Danger, Unknown }

/**
 * Metric sample for history.
 */
@Serializable
data class MetricSample(
    val id: UUID = UUID.randomUUID(),
    val deviceId: UUID,
    val recordedAt: Long = System.currentTimeMillis(),
    val metricType: MetricType,
    val numericValue: Double,
    val unit: String,
    val metadata: Map<String, String> = emptyMap()
)

enum class MetricType {
    CPU_USAGE, MEMORY_USAGE, BATTERY_LEVEL, STORAGE_USAGE,
    NETWORK_LATENCY, NETWORK_BYTES_SENT, NETWORK_BYTES_RECEIVED,
    PROCESS_COUNT, CPU_TEMPERATURE, GPU_TEMPERATURE, FAN_SPEED
}