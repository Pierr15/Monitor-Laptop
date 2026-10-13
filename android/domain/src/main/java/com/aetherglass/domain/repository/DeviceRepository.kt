package com.aetherglass.domain.repository

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
import com.aetherglass.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for device operations.
 * Implemented in data layer.
 */
interface DeviceRepository {

    // Device registration and management
    suspend fun registerDevice(request: DeviceRegistrationRequest): Result<DeviceRegistrationResponse>
    suspend fun getRegisteredDevices(): Result<List<Device>>
    suspend fun getDevice(deviceId: java.util.UUID): Result<Device>
    suspend fun updateDevice(device: Device): Result<Device>
    suspend fun removeDevice(deviceId: java.util.UUID): Result<Unit>
    suspend fun setActiveDevice(deviceId: java.util.UUID?): Result<Unit>
    fun getActiveDeviceId(): java.util.UUID?
    fun observeActiveDevice(): Flow<java.util.UUID?>

    // Device settings
    suspend fun updateDeviceSettings(deviceId: java.util.UUID, settings: com.aetherglass.domain.model.DeviceSettings): Result<Unit>
    suspend fun getDeviceSettings(deviceId: java.util.UUID): Result<com.aetherglass.domain.model.DeviceSettings>

    // Metrics
    suspend fun getCurrentMetrics(deviceId: java.util.UUID): Result<SystemMetrics>
    fun observeMetrics(deviceId: java.util.UUID): Flow<Result<SystemMetrics>>
    suspend fun getMetricHistory(
        deviceId: java.util.UUID,
        metricType: com.aetherglass.domain.model.MetricType,
        from: Long,
        to: Long,
        limit: Int = 1000
    ): Result<List<MetricSample>>
    suspend fun getAggregatedMetrics(
        deviceId: java.util.UUID,
        metricType: com.aetherglass.domain.model.MetricType,
        from: Long,
        to: Long,
        interval: AggregationInterval
    ): Result<List<MetricSample>>

    // Processes
    suspend fun getProcesses(deviceId: java.util.UUID): Result<List<com.aetherglass.domain.model.ProcessInfo>>
    fun observeProcesses(deviceId: java.util.UUID): Flow<Result<List<com.aetherglass.domain.model.ProcessInfo>>>
    suspend fun terminateProcess(deviceId: java.util.UUID, pid: Int, force: Boolean): Result<CommandResponse>

    // Files
    suspend fun listFiles(deviceId: java.util.UUID, path: String): Result<FileListing>
    suspend fun downloadFile(deviceId: java.util.UUID, path: String, destinationPath: String): Result<FileTransferProgress>
    suspend fun uploadFile(deviceId: java.util.UUID, localPath: String, remotePath: String): Result<FileTransferProgress>
    suspend fun deleteFile(deviceId: java.util.UUID, path: String): Result<Unit>
    suspend fun createDirectory(deviceId: java.util.UUID, path: String): Result<Unit>

    // Power actions
    suspend fun executePowerAction(deviceId: java.util.UUID, action: Command.PowerActionType, delaySeconds: Int): Result<CommandResponse>

    // Commands
    suspend fun sendCommand(request: CommandRequest): Result<CommandResponse>
    suspend fun getCommandStatus(commandId: java.util.UUID): Result<CommandResponse>
    fun observeCommandStatus(commandId: java.util.UUID): Flow<Result<CommandResponse>>

    // Audit & History
    suspend fun getCommandHistory(deviceId: java.util.UUID, limit: Int = 100): Result<List<CommandAudit>>
    suspend fun getAlertHistory(deviceId: java.util.UUID, limit: Int = 100): Result<List<com.aetherglass.domain.model.AlertEvent>>

    // Connection
    suspend fun testConnection(deviceId: java.util.UUID): Result<Boolean>
    fun observeConnectionStatus(deviceId: java.util.UUID): Flow<com.aetherglass.domain.model.ConnectionStatus>
}

enum class AggregationInterval { Raw, Minute, Hour, Day }

/**
 * Authentication repository.
 */
interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<UserProfile>
    suspend fun signUp(email: String, password: String, name: String): Result<UserProfile>
    suspend fun signInWithOAuth(provider: OAuthProvider): Result<UserProfile>
    suspend fun signOut(): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit>
    suspend fun getCurrentUser(): Result<UserProfile?>
    fun observeAuthState(): Flow<AuthState>
    suspend fun refreshSession(): Result<UserProfile>
}

enum class OAuthProvider { Google, GitHub, Apple, Microsoft }

sealed class AuthState {
    data class Authenticated(val user: UserProfile) : AuthState()
    data class Unauthenticated(val error: com.aetherglass.util.Error? = null) : AuthState()
    object Loading : AuthState()
}

/**
 * Local preferences repository.
 */
interface PreferencesRepository {
    fun getString(key: String, default: String?): String?
    fun putString(key: String, value: String): Result<Unit>
    fun getBoolean(key: String, default: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean): Result<Unit>
    fun getLong(key: String, default: Long): Long
    fun putLong(key: String, value: Long): Result<Unit>
    fun remove(key: String): Result<Unit>
    fun clear(): Result<Unit>
}

/**
 * Secure storage repository.
 */
interface SecureStorageRepository {
    fun putString(key: String, value: String): Result<Unit>
    fun getString(key: String): Result<String?>
    fun putByteArray(key: String, value: ByteArray): Result<Unit>
    fun getByteArray(key: String): Result<ByteArray?>
    fun remove(key: String): Result<Unit>
    fun clear(): Result<Unit>
}

/**
 * Notification repository.
 */
interface NotificationRepository {
    suspend fun requestPermission(): Result<Boolean>
    suspend fun registerForPushNotifications(): Result<String> // Returns token
    suspend fun unregisterFromPushNotifications(): Result<Unit>
    fun observeNotifications(): Flow<List<com.aetherglass.domain.model.AlertEvent>>
    suspend fun markAsRead(notificationId: java.util.UUID): Result<Unit>
    suspend fun markAllAsRead(): Result<Unit>
}