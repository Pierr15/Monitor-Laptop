package com.aetherglass.domain.usecase

import com.aetherglass.domain.model.Command
import com.aetherglass.domain.model.CommandAudit
import com.aetherglass.domain.model.CommandRequest
import com.aetherglass.domain.model.CommandResponse
import com.aetherglass.domain.model.Device
import com.aetherglass.domain.model.DeviceRegistrationRequest
import com.aetherglass.domain.model.DeviceRegistrationResponse
import com.aetherglass.domain.model.FileEntry
import com.aetherglass.domain.model.FileListing
import com.aetherglass.domain.model.FileTransferProgress
import com.aetherglass.domain.model.MetricSample
import com.aetherglass.domain.model.SystemMetrics
import com.aetherglass.domain.model.UserProfile
import com.aetherglass.domain.repository.AuthRepository
import com.aetherglass.domain.repository.DeviceRepository
import com.aetherglass.domain.repository.NotificationRepository
import com.aetherglass.domain.repository.PreferencesRepository
import com.aetherglass.domain.repository.SecureStorageRepository
import com.aetherglass.util.Result
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

/**
 * Use case for authentication operations.
 */
class AuthUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val secureStorage: SecureStorageRepository
) {
    suspend fun signIn(email: String, password: String): Result<UserProfile> {
        return authRepository.signIn(email, password).onSuccess { user ->
            // Store tokens securely
            secureStorage.putString("auth_token", user.id.toString())
        }
    }

    suspend fun signUp(email: String, password: String, name: String): Result<UserProfile> {
        return authRepository.signUp(email, password, name)
    }

    suspend fun signInWithOAuth(provider: AuthRepository.OAuthProvider): Result<UserProfile> {
        return authRepository.signInWithOAuth(provider)
    }

    suspend fun signOut(): Result<Unit> {
        return authRepository.signOut().onSuccess {
            secureStorage.clear()
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return authRepository.resetPassword(email)
    }

    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<UserProfile> {
        return authRepository.updatePassword(currentPassword, newPassword)
    }

    suspend fun getCurrentUser(): Result<UserProfile?> {
        return authRepository.getCurrentUser()
    }

    fun observeAuthState(): Flow<AuthRepository.AuthState> {
        return authRepository.observeAuthState()
    }

    suspend fun refreshSession(): Result<UserProfile> {
        return authRepository.refreshSession()
    }
}

/**
 * Use case for device management.
 */
class DeviceUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val preferences: PreferencesRepository
) {
    suspend fun registerDevice(request: DeviceRegistrationRequest): Result<DeviceRegistrationResponse> {
        return deviceRepository.registerDevice(request)
    }

    suspend fun getRegisteredDevices(): Result<List<Device>> {
        return deviceRepository.getRegisteredDevices()
    }

    suspend fun getDevice(deviceId: UUID): Result<Device> {
        return deviceRepository.getDevice(deviceId)
    }

    suspend fun updateDevice(device: Device): Result<Device> {
        return deviceRepository.updateDevice(device)
    }

    suspend fun removeDevice(deviceId: UUID): Result<Unit> {
        return deviceRepository.removeDevice(deviceId)
    }

    suspend fun setActiveDevice(deviceId: UUID?): Result<Unit> {
        return deviceRepository.setActiveDevice(deviceId).onSuccess {
            deviceId?.let { preferences.putLong("active_device_id", it.mostSignificantBits) }
        }
    }

    fun getActiveDeviceId(): UUID? {
        val id = deviceRepository.getActiveDeviceId()
        return id
    }

    fun observeActiveDevice(): Flow<UUID?> {
        return deviceRepository.observeActiveDevice()
    }

    suspend fun updateDeviceSettings(deviceId: UUID, settings: com.aetherglass.domain.model.DeviceSettings): Result<Unit> {
        return deviceRepository.updateDeviceSettings(deviceId, settings)
    }

    suspend fun getDeviceSettings(deviceId: UUID): Result<com.aetherglass.domain.model.DeviceSettings> {
        return deviceRepository.getDeviceSettings(deviceId)
    }
}

/**
 * Use case for metrics operations.
 */
class MetricsUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    suspend fun getCurrentMetrics(deviceId: UUID): Result<SystemMetrics> {
        return deviceRepository.getCurrentMetrics(deviceId)
    }

    fun observeMetrics(deviceId: UUID): Flow<Result<SystemMetrics>> {
        return deviceRepository.observeMetrics(deviceId)
    }

    suspend fun getMetricHistory(
        deviceId: UUID,
        metricType: com.aetherglass.domain.model.MetricType,
        from: Long,
        to: Long,
        limit: Int = 1000
    ): Result<List<MetricSample>> {
        return deviceRepository.getMetricHistory(deviceId, metricType, from, to, limit)
    }

    suspend fun getAggregatedMetrics(
        deviceId: UUID,
        metricType: com.aetherglass.domain.model.MetricType,
        from: Long,
        to: Long,
        interval: DeviceRepository.AggregationInterval
    ): Result<List<MetricSample>> {
        return deviceRepository.getAggregatedMetrics(deviceId, metricType, from, to, interval)
    }
}

/**
 * Use case for process management.
 */
class ProcessUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    suspend fun getProcesses(deviceId: UUID): Result<List<com.aetherglass.domain.model.ProcessInfo>> {
        return deviceRepository.getProcesses(deviceId)
    }

    fun observeProcesses(deviceId: UUID): Flow<Result<List<com.aetherglass.domain.model.ProcessInfo>>> {
        return deviceRepository.observeProcesses(deviceId)
    }

    suspend fun terminateProcess(deviceId: UUID, pid: Int, force: Boolean): Result<CommandResponse> {
        return deviceRepository.terminateProcess(deviceId, pid, force)
    }
}

/**
 * Use case for file operations.
 */
class FileUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    suspend fun listFiles(deviceId: UUID, path: String): Result<FileListing> {
        return deviceRepository.listFiles(deviceId, path)
    }

    suspend fun downloadFile(deviceId: UUID, path: String, destinationPath: String): Result<FileTransferProgress> {
        return deviceRepository.downloadFile(deviceId, path, destinationPath)
    }

    suspend fun uploadFile(deviceId: UUID, localPath: String, remotePath: String): Result<FileTransferProgress> {
        return deviceRepository.uploadFile(deviceId, localPath, remotePath)
    }

    suspend fun deleteFile(deviceId: UUID, path: String): Result<Unit> {
        return deviceRepository.deleteFile(deviceId, path)
    }

    suspend fun createDirectory(deviceId: UUID, path: String): Result<Unit> {
        return deviceRepository.createDirectory(deviceId, path)
    }
}

/**
 * Use case for power actions.
 */
class PowerUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    suspend fun sleep(deviceId: UUID, delaySeconds: Int = 0): Result<CommandResponse> {
        return deviceRepository.executePowerAction(deviceId, Command.PowerActionType.Sleep, delaySeconds)
    }

    suspend fun restart(deviceId: UUID, delaySeconds: Int = 0): Result<CommandResponse> {
        return deviceRepository.executePowerAction(deviceId, Command.PowerActionType.Restart, delaySeconds)
    }

    suspend fun shutdown(deviceId: UUID, delaySeconds: Int = 0): Result<CommandResponse> {
        return deviceRepository.executePowerAction(deviceId, Command.PowerActionType.Shutdown, delaySeconds)
    }
}

/**
 * Use case for command operations.
 */
class CommandUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    suspend fun sendCommand(request: CommandRequest): Result<CommandResponse> {
        return deviceRepository.sendCommand(request)
    }

    suspend fun getCommandStatus(commandId: UUID): Result<CommandResponse> {
        return deviceRepository.getCommandStatus(commandId)
    }

    fun observeCommandStatus(commandId: UUID): Flow<Result<CommandResponse>> {
        return deviceRepository.observeCommandStatus(commandId)
    }

    suspend fun getCommandHistory(deviceId: UUID, limit: Int = 100): Result<List<CommandAudit>> {
        return deviceRepository.getCommandHistory(deviceId, limit)
    }

    suspend fun getAlertHistory(deviceId: UUID, limit: Int = 100): Result<List<com.aetherglass.domain.model.AlertEvent>> {
        return deviceRepository.getAlertHistory(deviceId, limit)
    }
}

/**
 * Use case for connection management.
 */
class ConnectionUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    suspend fun testConnection(deviceId: UUID): Result<Boolean> {
        return deviceRepository.testConnection(deviceId)
    }

    fun observeConnectionStatus(deviceId: UUID): Flow<com.aetherglass.domain.model.ConnectionStatus> {
        return deviceRepository.observeConnectionStatus(deviceId)
    }
}

/**
 * Use case for notifications.
 */
class NotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend fun requestPermission(): Result<Boolean> {
        return notificationRepository.requestPermission()
    }

    suspend fun registerForPushNotifications(): Result<String> {
        return notificationRepository.registerForPushNotifications()
    }

    suspend fun unregisterFromPushNotifications(): Result<Unit> {
        return notificationRepository.unregisterFromPushNotifications()
    }

    fun observeNotifications(): Flow<List<com.aetherglass.domain.model.AlertEvent>> {
        return notificationRepository.observeNotifications()
    }

    suspend fun markAsRead(notificationId: UUID): Result<Unit> {
        return notificationRepository.markAsRead(notificationId)
    }

    suspend fun markAllAsRead(): Result<Unit> {
        return notificationRepository.markAllAsRead()
    }
}

/**
 * Use case for preferences.
 */
class PreferencesUseCase @Inject constructor(
    private val preferences: PreferencesRepository
) {
    fun getUpdateIntervalForeground(): Int {
        return preferences.getLong("update_interval_foreground", 2000).toInt()
    }

    fun setUpdateIntervalForeground(intervalMs: Int): Result<Unit> {
        return preferences.putLong("update_interval_foreground", intervalMs.toLong())
    }

    fun getUpdateIntervalBackground(): Int {
        return preferences.getLong("update_interval_background", 30000).toInt()
    }

    fun setUpdateIntervalBackground(intervalMs: Int): Result<Unit> {
        return preferences.putLong("update_interval_background", intervalMs.toLong())
    }

    fun getRetentionDays(): Int {
        return preferences.getLong("retention_days", 7).toInt()
    }

    fun setRetentionDays(days: Int): Result<Unit> {
        return preferences.putLong("retention_days", days.toLong())
    }

    fun getEnablePushNotifications(): Boolean {
        return preferences.getBoolean("enable_push_notifications", true)
    }

    fun setEnablePushNotifications(enabled: Boolean): Result<Unit> {
        return preferences.putBoolean("enable_push_notifications", enabled)
    }

    fun getDemoMode(): Boolean {
        return preferences.getBoolean("demo_mode", false)
    }

    fun setDemoMode(enabled: Boolean): Result<Unit> {
        return preferences.putBoolean("demo_mode", enabled)
    }

    fun getBiometricEnabled(): Boolean {
        return preferences.getBoolean("biometric_enabled", true)
    }

    fun setBiometricEnabled(enabled: Boolean): Result<Unit> {
        return preferences.putBoolean("biometric_enabled", enabled)
    }
}