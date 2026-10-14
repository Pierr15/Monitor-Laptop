package com.aetherglass.data.mapper

import com.aetherglass.data.api.CpuMetricsResponse
import com.aetherglass.data.api.MemoryMetricsResponse
import com.aetherglass.data.api.BatteryMetricsResponse
import com.aetherglass.data.api.StorageVolumeResponse
import com.aetherglass.data.api.NetworkStatusResponse
import com.aetherglass.data.api.ProcessInfoResponse
import com.aetherglass.data.api.SensorMetricsResponse
import com.aetherglass.data.api.AgentHealthResponse
import com.aetherglass.data.api.SystemMetricsResponse
import com.aetherglass.domain.model.CpuMetrics
import com.aetherglass.domain.model.MemoryMetrics
import com.aetherglass.domain.model.BatteryMetrics
import com.aetherglass.domain.model.StorageVolume
import com.aetherglass.domain.model.NetworkMetrics
import com.aetherglass.domain.model.ProcessInfo
import com.aetherglass.domain.model.SensorMetrics
import com.aetherglass.domain.model.AgentHealth
import com.aetherglass.domain.model.SystemMetrics
import com.aetherglass.domain.model.MetricSample
import com.aetherglass.domain.model.MetricType
import java.util.UUID

object MetricMapper {

    fun toDomain(response: SystemMetricsResponse): SystemMetrics {
        return SystemMetrics(
            deviceId = response.deviceId,
            timestamp = response.timestamp,
            cpu = toDomain(response.cpu),
            memory = toDomain(response.memory),
            battery = response.battery?.let { toDomain(it) },
            storage = response.storage.map { toDomain(it) },
            network = toDomain(response.network),
            processes = response.processes.map(ProcessMapper::toDomain),
            sensors = toDomain(response.sensors),
            agent = toDomain(response.agent)
        )
    }

    fun toDomain(response: CpuMetricsResponse): CpuMetrics {
        return CpuMetrics(
            usagePercent = response.usagePercent,
            cores = response.cores,
            frequencyMHz = response.frequencyMHz,
            temperatureCelsius = response.temperatureCelsius,
            loadAverage1m = response.loadAverage1m,
            loadAverage5m = response.loadAverage5m,
            loadAverage15m = response.loadAverage15m
        )
    }

    fun toDomain(response: MemoryMetricsResponse): MemoryMetrics {
        return MemoryMetrics(
            totalBytes = response.totalBytes,
            usedBytes = response.usedBytes,
            availableBytes = response.availableBytes,
            swapTotalBytes = response.swapTotalBytes,
            swapUsedBytes = response.swapUsedBytes
        )
    }

    fun toDomain(response: BatteryMetricsResponse): BatteryMetrics {
        return BatteryMetrics(
            levelPercent = response.levelPercent,
            isCharging = response.isCharging,
            isPresent = response.isPresent,
            health = com.aetherglass.domain.model.BatteryHealth.values().firstOrNull { it.name.equals(response.health, ignoreCase = true) }
                ?: com.aetherglass.domain.model.BatteryHealth.Unknown,
            technology = response.technology,
            voltageMv = response.voltageMv,
            temperatureCelsius = response.temperatureCelsius,
            capacityMah = response.capacityMah,
            timeRemainingMinutes = response.timeRemainingMinutes
        )
    }

    fun toDomain(response: StorageVolumeResponse): StorageVolume {
        return StorageVolume(
            id = response.id,
            label = response.label,
            path = response.path,
            totalBytes = response.totalBytes,
            freeBytes = response.freeBytes,
            type = com.aetherglass.domain.model.VolumeType.values().firstOrNull { it.name.equals(response.type, ignoreCase = true) }
                ?: com.aetherglass.domain.model.VolumeType.Unknown,
            fileSystem = response.fileSystem
        )
    }

    fun toDomain(response: NetworkStatusResponse): NetworkMetrics {
        return NetworkMetrics(
            interfaces = response.interfaces.map { toDomain(it) },
            latencyMs = response.latencyMs,
            isConnected = response.isConnected,
            tailscale = toDomain(response.tailscale)
        )
    }

    fun toDomain(response: com.aetherglass.data.api.NetworkInterfaceResponse): com.aetherglass.domain.model.NetworkInterface {
        return com.aetherglass.domain.model.NetworkInterface(
            name = response.name,
            displayName = response.displayName,
            ipAddresses = response.ipAddresses,
            macAddress = response.macAddress,
            speedBps = response.speedBps,
            bytesSent = response.bytesSent,
            bytesReceived = response.bytesReceived,
            isUp = response.isUp,
            type = com.aetherglass.domain.model.InterfaceType.values().firstOrNull { it.name.equals(response.type, ignoreCase = true) }
                ?: com.aetherglass.domain.model.InterfaceType.Unknown
        )
    }

    fun toDomain(response: com.aetherglass.data.api.TailscaleStatusResponse): com.aetherglass.domain.model.TailscaleStatus {
        return com.aetherglass.domain.model.TailscaleStatus(
            isConnected = response.isConnected,
            ipAddress = response.ipAddress,
            peerCount = response.peerCount,
            backendState = response.backendState,
            lastHandshake = response.lastHandshake
        )
    }

    fun toDomain(response: SensorMetricsResponse): SensorMetrics {
        return SensorMetrics(
            cpuTemperatureCelsius = response.cpuTemperatureCelsius,
            gpuTemperatureCelsius = response.gpuTemperatureCelsius,
            fanSpeedRpm = response.fanSpeedRpm,
            gpuUtilizationPercent = response.gpuUtilizationPercent,
            gpuMemoryUsedBytes = response.gpuMemoryUsedBytes,
            gpuMemoryTotalBytes = response.gpuMemoryTotalBytes
        )
    }

    fun toDomain(response: AgentHealthResponse): AgentHealth {
        return AgentHealth(
            version = response.version,
            uptimeSeconds = response.uptimeSeconds,
            lastCollectionMs = response.lastCollectionMs,
            collectionErrors = response.collectionErrors,
            isHealthy = response.isHealthy,
            warnings = response.warnings
        )
    }

    fun toMetricSample(response: com.aetherglass.data.api.MetricSampleResponse): MetricSample {
        return MetricSample(
            id = response.id,
            deviceId = response.deviceId,
            recordedAt = response.recordedAt,
            metricType = MetricType.valueOf(response.metricType),
            numericValue = response.numericValue,
            unit = response.unit,
            metadata = response.metadata
        )
    }
}