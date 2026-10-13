package com.aetherglass.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Registered device model.
 */
@Serializable
data class Device(
    val id: UUID,
    val ownerId: UUID,
    val name: String,
    val platform: String = "Windows",
    val agentVersion: String? = null,
    val registeredAt: Long = System.currentTimeMillis(),
    val lastSeenAt: Long? = null,
    val capabilities: DeviceCapabilities = DeviceCapabilities(),
    val settings: DeviceSettings = DeviceSettings(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isOnline: Boolean
        get() = lastSeenAt != null && (System.currentTimeMillis() - lastSeenAt!!) < 120_000 // 2 minutes

    val isStale: Boolean
        get() = lastSeenAt != null && (System.currentTimeMillis() - lastSeenAt!!) > 30_000 // 30 seconds
}

/**
 * Device capabilities - what the agent supports.
 */
@Serializable
data class DeviceCapabilities(
    val cpuMonitoring: Boolean = true,
    val memoryMonitoring: Boolean = true,
    val batteryMonitoring: Boolean = false,
    val storageMonitoring: Boolean = true,
    val networkMonitoring: Boolean = true,
    val processListing: Boolean = true,
    val processTermination: Boolean = true,
    val fileBrowsing: Boolean = true,
    val fileUpload: Boolean = true,
    val fileDownload: Boolean = true,
    val powerSleep: Boolean = true,
    val powerRestart: Boolean = true,
    val powerShutdown: Boolean = true,
    val sensorTemperature: Boolean = false,
    val sensorFanSpeed: Boolean = false,
    val sensorGpu: Boolean = false,
    val websocketStreaming: Boolean = true,
    val tailscaleIntegration: Boolean = true
)

/**
 * User-configurable device settings.
 */
@Serializable
data class DeviceSettings(
    val updateIntervalForegroundMs: Int = 2000,
    val updateIntervalBackgroundMs: Int = 30000,
    val retentionDays: Int = 7,
    val enablePushNotifications: Boolean = true,
    val alertThresholds: AlertThresholds = AlertThresholds(),
    val allowlistedPaths: List<String> = listOf("C:\\Users", "D:\\"),
    val blockedProcessNames: List<String> = listOf("System", "csrss.exe", "wininit.exe", "services.exe", "lsass.exe")
)

/**
 * Alert thresholds for notifications.
 */
@Serializable
data class AlertThresholds(
    val cpuPercent: Int = 90,
    val memoryPercent: Int = 90,
    val batteryPercent: Int = 15,
    val storagePercent: Int = 95,
    val networkLatencyMs: Int = 500
)

/**
 * Device registration request.
 */
@Serializable
data class DeviceRegistrationRequest(
    val name: String,
    val platform: String,
    val agentVersion: String,
    val capabilities: DeviceCapabilities,
    val publicKey: String? = null // For device-to-device encryption
)

/**
 * Device registration response.
 */
@Serializable
data class DeviceRegistrationResponse(
    val device: Device,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)