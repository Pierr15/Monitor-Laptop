package com.aetherglass.data.repository

import com.aetherglass.data.api.AgentApiService
import com.aetherglass.data.api.SupabaseApiService
import com.aetherglass.data.demo.DemoDataProvider
import com.aetherglass.data.mapper.MetricMapper
import com.aetherglass.data.mapper.ProcessMapper
import com.aetherglass.data.mapper.DeviceMapper
import com.aetherglass.domain.model.Command
import com.aetherglass.domain.model.CommandAudit
import com.aetherglass.domain.model.CommandRequest
import com.aetherglass.domain.model.CommandResponse
import com.aetherglass.domain.model.ConnectionStatus
import com.aetherglass.domain.model.Device
import com.aetherglass.domain.model.DeviceRegistrationRequest
import com.aetherglass.domain.model.DeviceRegistrationResponse
import com.aetherglass.domain.model.DeviceSettings
import com.aetherglass.domain.model.FileEntry
import com.aetherglass.domain.model.FileListing
import com.aetherglass.domain.model.FileTransferProgress
import com.aetherglass.domain.model.MetricSample
import com.aetherglass.domain.model.ProcessInfo
import com.aetherglass.domain.model.SystemMetrics
import com.aetherglass.domain.model.AlertEvent
import com.aetherglass.domain.repository.DeviceRepository
import com.aetherglass.util.Result
import com.aetherglass.util.runCatching
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepositoryImpl @Inject constructor(
    private val agentApi: AgentApiService,
    private val supabaseApi: SupabaseApiService,
    private val demoProvider: DemoDataProvider,
    private val preferences: PreferencesRepository,
    private val secureStorage: SecureStorageRepository
) : DeviceRepository {

    private val _activeDeviceId = MutableStateFlow<UUID?>(null)
    override fun observeActiveDevice(): Flow<UUID?> = _activeDeviceId

    override fun getActiveDeviceId(): UUID? = _activeDeviceId.value

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Unknown)
    private val _metrics = MutableStateFlow<Result<SystemMetrics>>(Result.failure(com.aetherglass.util.Error("NOT_LOADED", "Metrics not loaded")))

    override fun observeConnectionStatus(deviceId: UUID): Flow<ConnectionStatus> = _connectionStatus
    override fun observeMetrics(deviceId: UUID): Flow<Result<SystemMetrics>> = _metrics

    override fun observeProcesses(deviceId: UUID): Flow<Result<List<ProcessInfo>>> {
        return _metrics.map { metricsResult ->
            metricsResult.map { it.processes }
        }
    }

    // Device registration and management
    override suspend fun registerDevice(request: DeviceRegistrationRequest): Result<DeviceRegistrationResponse> {
        return if (preferences.getDemoMode()) {
            demoProvider.registerDevice(request)
        } else {
            runCatching {
                supabaseApi.registerDevice(request).getOrThrow()
            }
        }
    }

    override suspend fun getRegisteredDevices(): Result<List<Device>> {
        return if (preferences.getDemoMode()) {
            demoProvider.getRegisteredDevices()
        } else {
            runCatching {
                supabaseApi.getDevices().getOrThrow().map(DeviceMapper::toDomain)
            }
        }
    }

    override suspend fun getDevice(deviceId: UUID): Result<Device> {
        return if (preferences.getDemoMode()) {
            demoProvider.getDevice(deviceId)
        } else {
            runCatching {
                DeviceMapper.toDomain(supabaseApi.getDevice(deviceId).getOrThrow())
            }
        }
    }

    override suspend fun updateDevice(device: Device): Result<Device> {
        return if (preferences.getDemoMode()) {
            demoProvider.updateDevice(device)
        } else {
            runCatching {
                DeviceMapper.toDomain(supabaseApi.updateDevice(device).getOrThrow())
            }
        }
    }

    override suspend fun removeDevice(deviceId: UUID): Result<Unit> {
        return if (preferences.getDemoMode()) {
            demoProvider.removeDevice(deviceId)
        } else {
            runCatching {
                supabaseApi.deleteDevice(deviceId).getOrThrow()
            }
        }
    }

    override suspend fun setActiveDevice(deviceId: UUID?): Result<Unit> {
        _activeDeviceId.value = deviceId
        deviceId?.let { preferences.putLong("active_device_id", it.mostSignificantBits) }
        return Result.success(Unit)
    }

    // Device settings
    override suspend fun updateDeviceSettings(deviceId: UUID, settings: DeviceSettings): Result<Unit> {
        return if (preferences.getDemoMode()) {
            demoProvider.updateDeviceSettings(deviceId, settings)
        } else {
            runCatching {
                supabaseApi.upsertDeviceSettings(com.aetherglass.data.api.DeviceSettingsRequest(
                    deviceId = deviceId,
                    ownerId = getCurrentUserId()!!,
                    settings = settings
                )).getOrThrow()
            }
        }
    }

    override suspend fun getDeviceSettings(deviceId: UUID): Result<DeviceSettings> {
        return if (preferences.getDemoMode()) {
            demoProvider.getDeviceSettings(deviceId)
        } else {
            runCatching {
                DeviceMapper.toDomain(supabaseApi.getDeviceSettings(deviceId).getOrThrow())
            }
        }
    }

    // Metrics
    override suspend fun getCurrentMetrics(deviceId: UUID): Result<SystemMetrics> {
        return if (preferences.getDemoMode()) {
            demoProvider.getCurrentMetrics(deviceId)
        } else {
            runCatching {
                MetricMapper.toDomain(agentApi.getCurrentMetrics().getOrThrow())
            }.onSuccess { metrics ->
                _metrics.value = Result.success(metrics)
                _connectionStatus.value = ConnectionStatus.Connected
            }.onFailure { error ->
                _connectionStatus.value = ConnectionStatus.Disconnected
            }
        }
    }

    override suspend fun getMetricHistory(
        deviceId: UUID,
        metricType: com.aetherglass.domain.model.MetricType,
        from: Long,
        to: Long,
        limit: Int
    ): Result<List<MetricSample>> {
        return if (preferences.getDemoMode()) {
            demoProvider.getMetricHistory(deviceId, metricType, from, to, limit)
        } else {
            runCatching {
                supabaseApi.getMetrics(
                    deviceId = deviceId,
                    metricType = metricType.name,
                    from = from,
                    to = to,
                    limit = limit
                ).getOrThrow().map(MetricMapper::toMetricSample)
            }
        }
    }

    override suspend fun getAggregatedMetrics(
        deviceId: UUID,
        metricType: com.aetherglass.domain.model.MetricType,
        from: Long,
        to: Long,
        interval: AggregationInterval
    ): Result<List<MetricSample>> {
        // For now, delegate to history with raw interval
        return getMetricHistory(deviceId, metricType, from, to, 1000)
    }

    // Processes
    override suspend fun getProcesses(deviceId: UUID): Result<List<ProcessInfo>> {
        return if (preferences.getDemoMode()) {
            demoProvider.getProcesses(deviceId)
        } else {
            runCatching {
                agentApi.getProcesses().getOrThrow().processes.map(ProcessMapper::toDomain)
            }
        }
    }

    override suspend fun terminateProcess(deviceId: UUID, pid: Int, force: Boolean): Result<CommandResponse> {
        return if (preferences.getDemoMode()) {
            demoProvider.terminateProcess(deviceId, pid, force)
        } else {
            runCatching {
                agentApi.terminateProcess(
                    pid = pid,
                    request = com.aetherglass.data.api.TerminateProcessRequest(force = force)
                ).getOrThrow()
            }
        }
    }

    // Files
    override suspend fun listFiles(deviceId: UUID, path: String): Result<FileListing> {
        return if (preferences.getDemoMode()) {
            demoProvider.listFiles(deviceId, path)
        } else {
            runCatching {
                agentApi.listFiles(path = path).getOrThrow().let { listing ->
                    FileListing(
                        path = listing.path,
                        entries = listing.entries.map { entry ->
                            FileEntry(
                                name = entry.name,
                                path = entry.path,
                                isDirectory = entry.isDirectory,
                                size = entry.size,
                                modifiedAt = entry.modifiedAt,
                                permissions = entry.permissions,
                                mimeType = entry.mimeType
                            )
                        },
                        parentPath = listing.parentPath,
                        totalCount = listing.totalCount
                    )
                }
            }
        }
    }

    override suspend fun downloadFile(deviceId: UUID, path: String, destinationPath: String): Result<FileTransferProgress> {
        return if (preferences.getDemoMode()) {
            demoProvider.downloadFile(deviceId, path, destinationPath)
        } else {
            // TODO: Implement streaming download
            runCatching {
                // Placeholder
                FileTransferProgress(
                    transferId = UUID.randomUUID(),
                    fileName = path.substringAfterLast("/"),
                    totalBytes = 0,
                    transferredBytes = 0,
                    status = com.aetherglass.domain.model.TransferStatus.Failed,
                    error = "Not implemented"
                )
            }
        }
    }

    override suspend fun uploadFile(deviceId: UUID, localPath: String, remotePath: String): Result<FileTransferProgress> {
        return if (preferences.getDemoMode()) {
            demoProvider.uploadFile(deviceId, localPath, remotePath)
        } else {
            // TODO: Implement file upload
            runCatching {
                FileTransferProgress(
                    transferId = UUID.randomUUID(),
                    fileName = localPath.substringAfterLast("/"),
                    totalBytes = 0,
                    transferredBytes = 0,
                    status = com.aetherglass.domain.model.TransferStatus.Failed,
                    error = "Not implemented"
                )
            }
        }
    }

    override suspend fun deleteFile(deviceId: UUID, path: String): Result<Unit> {
        return Result.failure(com.aetherglass.util.Error("NOT_IMPLEMENTED", "File deletion not implemented"))
    }

    override suspend fun createDirectory(deviceId: UUID, path: String): Result<Unit> {
        return Result.failure(com.aetherglass.util.Error("NOT_IMPLEMENTED", "Directory creation not implemented"))
    }

    // Power actions
    override suspend fun executePowerAction(deviceId: UUID, action: Command.PowerActionType, delaySeconds: Int): Result<CommandResponse> {
        val idempotencyKey = UUID.randomUUID().toString()
        return if (preferences.getDemoMode()) {
            demoProvider.executePowerAction(deviceId, action, delaySeconds)
        } else {
            runCatching {
                when (action) {
                    Command.PowerActionType.Sleep -> agentApi.sleep(com.aetherglass.data.api.PowerRequest(delaySeconds, idempotencyKey)).getOrThrow()
                    Command.PowerActionType.Restart -> agentApi.restart(com.aetherglass.data.api.PowerRequest(delaySeconds, idempotencyKey)).getOrThrow()
                    Command.PowerActionType.Shutdown -> agentApi.shutdown(com.aetherglass.data.api.PowerRequest(delaySeconds, idempotencyKey)).getOrThrow()
                }
            }
        }
    }

    // Commands
    override suspend fun sendCommand(request: CommandRequest): Result<CommandResponse> {
        return if (preferences.getDemoMode()) {
            demoProvider.sendCommand(request)
        } else {
            runCatching {
                // Map command to appropriate API call
                when (request.command) {
                    is Command.TerminateProcess -> agentApi.terminateProcess(
                        pid = request.command.pid,
                        request = com.aetherglass.data.api.TerminateProcessRequest(force = request.command.force)
                    ).getOrThrow()
                    is Command.PowerAction -> when (request.command.action) {
                        Command.PowerActionType.Sleep -> agentApi.sleep(com.aetherglass.data.api.PowerRequest(request.command.delaySeconds, request.idempotencyKey)).getOrThrow()
                        Command.PowerActionType.Restart -> agentApi.restart(com.aetherglass.data.api.PowerRequest(request.command.delaySeconds, request.idempotencyKey)).getOrThrow()
                        Command.PowerActionType.Shutdown -> agentApi.shutdown(com.aetherglass.data.api.PowerRequest(request.command.delaySeconds, request.idempotencyKey)).getOrThrow()
                    }
                    is Command.Ping -> CommandResponse(
                        commandId = request.id,
                        status = CommandStatus.Succeeded,
                        result = "pong",
                        completedAt = System.currentTimeMillis()
                    )
                    else -> CommandResponse(
                        commandId = request.id,
                        status = CommandStatus.Failed,
                        error = CommandError("UNKNOWN_COMMAND", "Unknown command type")
                    )
                }
            }
        }
    }

    override suspend fun getCommandStatus(commandId: UUID): Result<CommandResponse> {
        return if (preferences.getDemoMode()) {
            demoProvider.getCommandStatus(commandId)
        } else {
            runCatching {
                agentApi.getCommandStatus(commandId).getOrThrow()
            }
        }
    }

    override fun observeCommandStatus(commandId: UUID): Flow<Result<CommandResponse>> {
        // TODO: Implement WebSocket-based command status observation
        return kotlinx.coroutines.flow.flowOf(Result.failure(com.aetherglass.util.Error("NOT_IMPLEMENTED", "WebSocket not implemented")))
    }

    // Audit & History
    override suspend fun getCommandHistory(deviceId: UUID, limit: Int): Result<List<CommandAudit>> {
        return if (preferences.getDemoMode()) {
            demoProvider.getCommandHistory(deviceId, limit)
        } else {
            runCatching {
                supabaseApi.getCommandHistory(deviceId, limit).getOrThrow()
            }
        }
    }

    override suspend fun getAlertHistory(deviceId: UUID, limit: Int): Result<List<AlertEvent>> {
        return if (preferences.getDemoMode()) {
            demoProvider.getAlertHistory(deviceId, limit)
        } else {
            runCatching {
                supabaseApi.getAlertHistory(deviceId, limit).getOrThrow()
            }
        }
    }

    // Connection
    override suspend fun testConnection(deviceId: UUID): Result<Boolean> {
        return if (preferences.getDemoMode()) {
            Result.success(true)
        } else {
            runCatching {
                agentApi.getSystemSummary().getOrThrow()
                true
            }.onFailure { _connectionStatus.value = ConnectionStatus.Disconnected }
        }
    }

    private fun getCurrentUserId(): UUID? {
        // TODO: Get from auth repository
        return null
    }
}