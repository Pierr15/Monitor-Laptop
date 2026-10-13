package com.aetherglass.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Command types for agent control.
 */
@Serializable
sealed class Command {
    @Serializable
    data class TerminateProcess(
        val pid: Int,
        val processName: String,
        val force: Boolean = false
    ) : Command()

    @Serializable
    data class PowerAction(
        val action: PowerActionType,
        val delaySeconds: Int = 0
    ) : Command()

    @Serializable
    data class FileOperation(
        val operation: FileOperationType,
        val path: String,
        val destinationPath: String? = null,
        val overwrite: Boolean = false
    ) : Command()

    @Serializable
    data class AgentConfig(
        val settings: DeviceSettings
    ) : Command()

    @Serializable
    data class Ping(
        val requestId: String = UUID.randomUUID().toString()
    ) : Command()
}

enum class PowerActionType { Sleep, Restart, Shutdown }
enum class FileOperationType { List, Read, Write, Delete, CreateDirectory, Upload, Download }

/**
 * Command request with metadata.
 */
@Serializable
data class CommandRequest(
    val id: UUID = UUID.randomUUID(),
    val deviceId: UUID,
    val command: Command,
    val idempotencyKey: String,
    val requestedAt: Long = System.currentTimeMillis(),
    val requestedBy: UUID, // User ID
    val requiresConfirmation: Boolean = false,
    val confirmationLevel: ConfirmationLevel = ConfirmationLevel.None
)

enum class ConfirmationLevel { None, Simple, Biometric, Critical }

/**
 * Command response from agent.
 */
@Serializable
data class CommandResponse(
    val commandId: UUID,
    val status: CommandStatus,
    val result: String? = null,
    val error: CommandError? = null,
    val completedAt: Long? = null
)

enum class CommandStatus {
    Queued, Validating, Executing, Succeeded, Failed, Unknown, Timeout
}

@Serializable
data class CommandError(
    val code: String,
    val message: String,
    val recoverable: Boolean = false
)

/**
 * Command audit record.
 */
@Serializable
data class CommandAudit(
    val id: UUID = UUID.randomUUID(),
    val deviceId: UUID,
    val ownerId: UUID,
    val commandId: UUID,
    val actionType: String,
    val requestedAt: Long,
    val completedAt: Long?,
    val status: CommandStatus,
    val resultSummary: String?,
    val errorCode: String?,
    val idempotencyKey: String
)

/**
 * File system models.
 */
@Serializable
data class FileEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val modifiedAt: Long = 0,
    val permissions: String? = null,
    val mimeType: String? = null
)

@Serializable
data class FileListing(
    val path: String,
    val entries: List<FileEntry> = emptyList(),
    val parentPath: String? = null,
    val totalCount: Int = 0
)

@Serializable
data class FileTransferProgress(
    val transferId: UUID,
    val fileName: String,
    val totalBytes: Long,
    val transferredBytes: Long,
    val status: TransferStatus,
    val error: String? = null
) {
    val progressPercent: Double
        get() = if (totalBytes > 0) (transferredBytes.toDouble() / totalBytes) * 100 else 0.0
}

enum class TransferStatus { Pending, InProgress, Paused, Completed, Failed, Cancelled }

/**
 * Alert event.
 */
@Serializable
data class AlertEvent(
    val id: UUID = UUID.randomUUID(),
    val deviceId: UUID,
    val ownerId: UUID,
    val alertType: AlertType,
    val severity: AlertSeverity,
    val message: String,
    val createdAt: Long = System.currentTimeMillis(),
    val acknowledgedAt: Long? = null,
    val metadata: Map<String, String> = emptyMap()
)

enum class AlertType {
    DeviceOnline, DeviceOffline, HighCpu, HighMemory, LowBattery,
    LowStorage, HighLatency, AgentError, CommandFailed, ConnectionLost
}

enum class AlertSeverity { Info, Warning, Critical }

/**
 * User profile.
 */
@Serializable
data class UserProfile(
    val id: UUID,
    val email: String,
    val name: String? = null,
    val avatarUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * API error response.
 */
@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis(),
    val requestId: String? = null
)