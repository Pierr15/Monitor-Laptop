package com.aetherglass.data.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.aetherglass.domain.model.AlertEvent
import com.aetherglass.domain.repository.NotificationRepository
import com.aetherglass.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Implementation of NotificationRepository using Firebase Cloud Messaging (placeholder).
 * In production, integrate with Firebase Messaging service.
 */
class NotificationRepositoryImpl(
    private val context: Context
) : NotificationRepository {

    private val _notifications = MutableStateFlow<List<AlertEvent>>(emptyList())
    override fun observeNotifications() = _notifications.asStateFlow()

    private val channelId = "aether_glass_alerts"

    init {
        createNotificationChannel()
    }

    override suspend fun requestPermission(): Result<Boolean> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires runtime permission
            // In real implementation, use ActivityCompat.requestPermissions
            return Result.success(true)
        }
        return Result.success(true)
    }

    override suspend fun registerForPushNotifications(): Result<String> {
        // TODO: Integrate with Firebase Messaging to get FCM token
        // val token = FirebaseMessaging.getInstance().token.await()
        return Result.success("demo-fcm-token-${UUID.randomUUID()}")
    }

    override suspend fun unregisterFromPushNotifications(): Result<Unit> {
        // TODO: Delete FCM token
        return Result.success(Unit)
    }

    override suspend fun markAsRead(notificationId: UUID): Result<Unit> {
        val current = _notifications.value
        val updated = current.map { if (it.id == notificationId) it.copy(acknowledgedAt = System.currentTimeMillis()) else it }
        _notifications.value = updated
        return Result.success(Unit)
    }

    override suspend fun markAllAsRead(): Result<Unit> {
        val current = _notifications.value
        val updated = current.map { it.copy(acknowledgedAt = System.currentTimeMillis()) }
        _notifications.value = updated
        return Result.success(Unit)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Aether Glass Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "System alerts and notifications from monitored devices"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}