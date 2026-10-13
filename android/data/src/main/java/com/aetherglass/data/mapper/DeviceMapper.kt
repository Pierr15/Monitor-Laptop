package com.aetherglass.data.mapper

import com.aetherglass.data.api.DeviceResponse
import com.aetherglass.domain.model.Device
import com.aetherglass.domain.model.DeviceCapabilities
import com.aetherglass.domain.model.DeviceSettings
import java.util.UUID

object DeviceMapper {

    fun toDomain(response: DeviceResponse): Device {
        return Device(
            id = response.id,
            ownerId = response.ownerId,
            name = response.name,
            platform = response.platform,
            agentVersion = response.agentVersion,
            registeredAt = response.registeredAt,
            lastSeenAt = response.lastSeenAt,
            capabilities = toDomain(response.capabilities),
            settings = toDomain(response.settings),
            createdAt = response.createdAt,
            updatedAt = response.updatedAt
        )
    }

    fun toDomain(response: com.aetherglass.data.api.DeviceCapabilitiesResponse): DeviceCapabilities {
        return DeviceCapabilities(
            cpuMonitoring = response.cpuMonitoring,
            memoryMonitoring = response.memoryMonitoring,
            batteryMonitoring = response.batteryMonitoring,
            storageMonitoring = response.storageMonitoring,
            networkMonitoring = response.networkMonitoring,
            processListing = response.processListing,
            processTermination = response.processTermination,
            fileBrowsing = response.fileBrowsing,
            fileUpload = response.fileUpload,
            fileDownload = response.fileDownload,
            powerSleep = response.powerSleep,
            powerRestart = response.powerRestart,
            powerShutdown = response.powerShutdown,
            sensorTemperature = response.sensorTemperature,
            sensorFanSpeed = response.sensorFanSpeed,
            sensorGpu = response.sensorGpu,
            websocketStreaming = response.websocketStreaming,
            tailscaleIntegration = response.tailscaleIntegration
        )
    }

    fun toDomain(response: com.aetherglass.data.api.DeviceSettingsResponse): DeviceSettings {
        return DeviceSettings(
            updateIntervalForegroundMs = response.updateIntervalForegroundMs,
            updateIntervalBackgroundMs = response.updateIntervalBackgroundMs,
            retentionDays = response.retentionDays,
            enablePushNotifications = response.enablePushNotifications,
            alertThresholds = com.aetherglass.domain.model.AlertThresholds(
                cpuPercent = response.alertCpuPercent,
                memoryPercent = response.alertMemoryPercent,
                batteryPercent = response.alertBatteryPercent,
                storagePercent = response.alertStoragePercent,
                networkLatencyMs = response.alertNetworkLatencyMs
            ),
            allowlistedPaths = response.allowlistedPaths,
            blockedProcessNames = response.blockedProcessNames
        )
    }

    fun toApi(device: Device): com.aetherglass.data.api.DeviceRequest {
        return com.aetherglass.data.api.DeviceRequest(
            id = device.id,
            ownerId = device.ownerId,
            name = device.name,
            platform = device.platform,
            agentVersion = device.agentVersion,
            registeredAt = device.registeredAt,
            lastSeenAt = device.lastSeenAt,
            capabilities = toApi(device.capabilities),
            settings = toApi(device.settings),
            createdAt = device.createdAt,
            updatedAt = device.updatedAt
        )
    }

    fun toApi(capabilities: DeviceCapabilities): com.aetherglass.data.api.DeviceCapabilitiesRequest {
        return com.aetherglass.data.api.DeviceCapabilitiesRequest(
            cpuMonitoring = capabilities.cpuMonitoring,
            memoryMonitoring = capabilities.memoryMonitoring,
            batteryMonitoring = capabilities.batteryMonitoring,
            storageMonitoring = capabilities.storageMonitoring,
            networkMonitoring = capabilities.networkMonitoring,
            processListing = capabilities.processListing,
            processTermination = capabilities.processTermination,
            fileBrowsing = capabilities.fileBrowsing,
            fileUpload = capabilities.fileUpload,
            fileDownload = capabilities.fileDownload,
            powerSleep = capabilities.powerSleep,
            powerRestart = capabilities.powerRestart,
            powerShutdown = capabilities.powerShutdown,
            sensorTemperature = capabilities.sensorTemperature,
            sensorFanSpeed = capabilities.sensorFanSpeed,
            sensorGpu = capabilities.sensorGpu,
            websocketStreaming = capabilities.websocketStreaming,
            tailscaleIntegration = capabilities.tailscaleIntegration
        )
    }

    fun toApi(settings: DeviceSettings): com.aetherglass.data.api.DeviceSettingsRequest {
        return com.aetherglass.data.api.DeviceSettingsRequest(
            updateIntervalForegroundMs = settings.updateIntervalForegroundMs,
            updateIntervalBackgroundMs = settings.updateIntervalBackgroundMs,
            retentionDays = settings.retentionDays,
            enablePushNotifications = settings.enablePushNotifications,
            alertCpuPercent = settings.alertThresholds.cpuPercent,
            alertMemoryPercent = settings.alertThresholds.memoryPercent,
            alertBatteryPercent = settings.alertThresholds.batteryPercent,
            alertStoragePercent = settings.alertThresholds.storagePercent,
            alertNetworkLatencyMs = settings.alertThresholds.networkLatencyMs,
            allowlistedPaths = settings.allowlistedPaths,
            blockedProcessNames = settings.blockedProcessNames
        )
    }
}