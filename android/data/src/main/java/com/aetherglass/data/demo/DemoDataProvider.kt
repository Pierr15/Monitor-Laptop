package com.aetherglass.data.demo

import com.aetherglass.domain.model.*
import com.aetherglass.domain.repository.DeviceRepository
import com.aetherglass.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Demo data provider for offline/development mode.
 * Provides synthetic data that mimics real device metrics.
 */
class DemoDataProvider {

    private val devices = ConcurrentHashMap<UUID, Device>()
    private val _activeDeviceId = MutableStateFlow<UUID?>(null)
    val activeDeviceId: UUID? = null

    init {
        // Pre-populate with demo devices
        val demoDevice = Device(
            id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
            ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
            name = "Laptop Pro",
            platform = "Windows 11",
            agentVersion = "1.0.0",
            lastSeenAt = System.currentTimeMillis(),
            capabilities = DeviceCapabilities(),
            settings = DeviceSettings()
        )
        devices[demoDevice.id] = demoDevice
        _activeDeviceId.value = demoDevice.id
    }

    suspend fun registerDevice(request: DeviceRegistrationRequest): Result<DeviceRegistrationResponse> {
        val device = Device(
            id = UUID.randomUUID(),
            ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
            name = request.name,
            platform = request.platform,
            agentVersion = request.agentVersion,
            capabilities = request.capabilities,
            lastSeenAt = System.currentTimeMillis()
        )
        devices[device.id] = device
        return Result.success(DeviceRegistrationResponse(
            device = device,
            accessToken = "demo-token-${device.id}",
            refreshToken = "demo-refresh-${device.id}",
            expiresIn = 86400000
        ))
    }

    suspend fun getRegisteredDevices(): Result<List<Device>> {
        return Result.success(devices.values.toList())
    }

    suspend fun getDevice(deviceId: UUID): Result<Device> {
        return devices[deviceId]?.let { Result.success(it) }
            ?: Result.failure(com.aetherglass.util.Error("NOT_FOUND", "Device not found"))
    }

    suspend fun updateDevice(device: Device): Result<Device> {
        devices[device.id] = device.copy(updatedAt = System.currentTimeMillis())
        return Result.success(devices[device.id]!!)
    }

    suspend fun removeDevice(deviceId: UUID): Result<Unit> {
        devices.remove(deviceId)
        return Result.success(Unit)
    }

    suspend fun updateDeviceSettings(deviceId: UUID, settings: DeviceSettings): Result<Unit> {
        devices[deviceId]?.let { existing ->
            devices[deviceId] = existing.copy(settings = settings, updatedAt = System.currentTimeMillis())
        }
        return Result.success(Unit)
    }

    suspend fun getDeviceSettings(deviceId: UUID): Result<DeviceSettings> {
        return devices[deviceId]?.settings?.let { Result.success(it) }
            ?: Result.failure(com.aetherglass.util.Error("NOT_FOUND", "Device not found"))
    }

    suspend fun getCurrentMetrics(deviceId: UUID): Result<SystemMetrics> {
        return Result.success(generateMetrics(deviceId))
    }

    suspend fun getMetricHistory(
        deviceId: UUID,
        metricType: MetricType,
        from: Long,
        to: Long,
        limit: Int
    ): Result<List<MetricSample>> {
        val samples = mutableListOf<MetricSample>()
        val interval = (to - from) / limit.max(1)
        var time = from
        repeat(limit) {
            val value = when (metricType) {
                MetricType.CPU_USAGE -> (20.0 + Math.random() * 60.0)
                MetricType.MEMORY_USAGE -> (30.0 + Math.random() * 50.0)
                MetricType.BATTERY_LEVEL -> (20.0 + Math.random() * 80.0)
                MetricType.STORAGE_USAGE -> (40.0 + Math.random() * 40.0)
                MetricType.NETWORK_LATENCY -> (10.0 + Math.random() * 100.0)
                else -> Math.random() * 100.0
            }
            samples.add(MetricSample(
                deviceId = deviceId,
                recordedAt = time,
                metricType = metricType,
                numericValue = value,
                unit = when (metricType) {
                    MetricType.CPU_USAGE, MetricType.MEMORY_USAGE, MetricType.BATTERY_LEVEL,
                    MetricType.STORAGE_USAGE -> "%"
                    MetricType.NETWORK_LATENCY -> "ms"
                    else -> ""
                }
            ))
            time += interval
        }
        return Result.success(samples)
    }

    suspend fun getAggregatedMetrics(
        deviceId: UUID,
        metricType: MetricType,
        from: Long,
        to: Long,
        interval: DeviceRepository.AggregationInterval
    ): Result<List<MetricSample>> {
        return getMetricHistory(deviceId, metricType, from, to, 100)
    }

    suspend fun getProcesses(deviceId: UUID): Result<List<ProcessInfo>> {
        return Result.success(generateProcesses())
    }

    suspend fun terminateProcess(deviceId: UUID, pid: Int, force: Boolean): Result<CommandResponse> {
        return Result.success(CommandResponse(
            commandId = UUID.randomUUID(),
            status = CommandStatus.Succeeded,
            result = "Process $pid terminated",
            completedAt = System.currentTimeMillis()
        ))
    }

    suspend fun listFiles(deviceId: UUID, path: String): Result<FileListing> {
        val entries = listOf(
            FileEntry("Documents", "$path/Documents", true, 0, System.currentTimeMillis()),
            FileEntry("Downloads", "$path/Downloads", true, 0, System.currentTimeMillis()),
            FileEntry("Pictures", "$path/Pictures", true, 0, System.currentTimeMillis()),
            FileEntry("readme.txt", "$path/readme.txt", false, 1024, System.currentTimeMillis()),
            FileEntry("config.json", "$path/config.json", false, 2048, System.currentTimeMillis())
        )
        return Result.success(FileListing(
            path = path,
            entries = entries,
            parentPath = if (path != "C:\\Users") path.substringBeforeLast("\\") else null,
            totalCount = entries.size
        ))
    }

    suspend fun downloadFile(deviceId: UUID, path: String, destinationPath: String): Result<FileTransferProgress> {
        return Result.success(FileTransferProgress(
            transferId = UUID.randomUUID(),
            fileName = path.substringAfterLast("\\"),
            totalBytes = 1024 * 1024,
            transferredBytes = 1024 * 1024,
            status = TransferStatus.Completed
        ))
    }

    suspend fun uploadFile(deviceId: UUID, localPath: String, remotePath: String): Result<FileTransferProgress> {
        return Result.success(FileTransferProgress(
            transferId = UUID.randomUUID(),
            fileName = localPath.substringAfterLast("\\"),
            totalBytes = 1024 * 1024,
            transferredBytes = 1024 * 1024,
            status = TransferStatus.Completed
        ))
    }

    suspend fun executePowerAction(deviceId: UUID, action: Command.PowerActionType, delaySeconds: Int): Result<CommandResponse> {
        return Result.success(CommandResponse(
            commandId = UUID.randomUUID(),
            status = CommandStatus.Succeeded,
            result = "${action.name} initiated",
            completedAt = System.currentTimeMillis()
        ))
    }

    suspend fun sendCommand(request: CommandRequest): Result<CommandResponse> {
        return Result.success(CommandResponse(
            commandId = request.id,
            status = CommandStatus.Succeeded,
            result = "Command executed (demo)",
            completedAt = System.currentTimeMillis()
        ))
    }

    suspend fun getCommandStatus(commandId: UUID): Result<CommandResponse> {
        return Result.success(CommandResponse(
            commandId = commandId,
            status = CommandStatus.Succeeded,
            result = "Completed (demo)",
            completedAt = System.currentTimeMillis()
        ))
    }

    suspend fun getCommandHistory(deviceId: UUID, limit: Int): Result<List<CommandAudit>> {
        return Result.success(emptyList())
    }

    suspend fun getAlertHistory(deviceId: UUID, limit: Int): Result<List<AlertEvent>> {
        return Result.success(emptyList())
    }

    private fun generateMetrics(deviceId: UUID): SystemMetrics {
        val cpuUsage = 15.0 + Math.random() * 30.0
        val memoryUsage = 40.0 + Math.random() * 30.0
        val batteryLevel = (20.0 + Math.random() * 70.0).toInt()

        return SystemMetrics(
            deviceId = deviceId,
            cpu = CpuMetrics(
                usagePercent = cpuUsage,
                cores = 8,
                frequencyMHz = 3200,
                temperatureCelsius = 45.0 + Math.random() * 20.0,
                loadAverage1m = 1.2,
                loadAverage5m = 1.0,
                loadAverage15m = 0.8
            ),
            memory = MemoryMetrics(
                totalBytes = 16L * 1024 * 1024 * 1024,
                usedBytes = (16L * 1024 * 1024 * 1024 * memoryUsage / 100).toLong(),
                availableBytes = (16L * 1024 * 1024 * 1024 * (100 - memoryUsage) / 100).toLong(),
                swapTotalBytes = 4L * 1024 * 1024 * 1024,
                swapUsedBytes = 512L * 1024 * 1024
            ),
            battery = BatteryMetrics(
                levelPercent = batteryLevel,
                isCharging = Math.random() > 0.5,
                isPresent = true,
                health = BatteryHealth.Good,
                technology = "Li-ion",
                voltageMv = 11400,
                temperatureCelsius = 25.0 + Math.random() * 10.0,
                capacityMah = 5000,
                timeRemainingMinutes = if (Math.random() > 0.5) (batteryLevel * 2) else null
            ),
            storage = listOf(
                StorageVolume(
                    id = "C:",
                    label = "Windows (C:)",
                    path = "C:\\",
                    totalBytes = 512L * 1024 * 1024 * 1024,
                    freeBytes = 128L * 1024 * 1024 * 1024,
                    type = VolumeType.Fixed,
                    fileSystem = "NTFS"
                ),
                StorageVolume(
                    id = "D:",
                    label = "Data (D:)",
                    path = "D:\\",
                    totalBytes = 1L * 1024 * 1024 * 1024 * 1024,
                    freeBytes = 512L * 1024 * 1024 * 1024,
                    type = VolumeType.Fixed,
                    fileSystem = "NTFS"
                )
            ),
            network = NetworkMetrics(
                interfaces = listOf(
                    com.aetherglass.domain.model.NetworkInterface(
                        name = "Ethernet0",
                        displayName = "Ethernet",
                        ipAddresses = listOf("192.168.1.100", "fe80::1"),
                        macAddress = "00:1B:44:11:3A:B7",
                        speedBps = 1_000_000_000,
                        bytesSent = 1_024_000_000,
                        bytesReceived = 2_048_000_000,
                        isUp = true,
                        type = InterfaceType.Ethernet
                    )
                ),
                latencyMs = 12,
                isConnected = true,
                tailscale = TailscaleStatus(
                    isConnected = true,
                    ipAddress = "100.64.0.1",
                    peerCount = 3,
                    backendState = "Running",
                    lastHandshake = System.currentTimeMillis() - 30_000
                )
            ),
            processes = generateProcesses(),
            sensors = SensorMetrics(
                cpuTemperatureCelsius = 55.0 + Math.random() * 15.0,
                gpuTemperatureCelsius = 45.0 + Math.random() * 20.0,
                fanSpeedRpm = 1200 + (Math.random() * 800).toInt(),
                gpuUtilizationPercent = 10.0 + Math.random() * 30.0,
                gpuMemoryUsedBytes = 2L * 1024 * 1024 * 1024,
                gpuMemoryTotalBytes = 8L * 1024 * 1024 * 1024
            ),
            agent = AgentHealth(
                version = "1.0.0",
                uptimeSeconds = 3600,
                lastCollectionMs = 50,
                collectionErrors = 0,
                isHealthy = true,
                warnings = emptyList()
            )
        )
    }

    private fun generateProcesses(): List<ProcessInfo> {
        val processes = mutableListOf<ProcessInfo>()
        val processNames = listOf(
            "System" to true,
            "csrss.exe" to true,
            "wininit.exe" to true,
            "services.exe" to true,
            "lsass.exe" to true,
            "explorer.exe" to false,
            "chrome.exe" to false,
            "code.exe" to false,
            "Teams.exe" to false,
            "Discord.exe" to false,
            "Spotify.exe" to false,
            "Steam.exe" to false,
            "OBS64.exe" to false,
            "msedge.exe" to false
        )

        processNames.forEachIndexed { index, (name, isSystem) ->
            val pid = if (isSystem) index + 4 else 1000 + index * 10
            processes.add(ProcessInfo(
                pid = pid,
                name = name,
                cpuPercent = if (isSystem) Math.random() * 2.0 else Math.random() * 15.0,
                memoryBytes = if (isSystem) (50 + Math.random() * 100).toLong() * 1024 * 1024 else (100 + Math.random() * 500).toLong() * 1024 * 1024,
                memoryPercent = if (isSystem) Math.random() * 2.0 else Math.random() * 10.0,
                status = ProcessStatus.Running,
                user = if (isSystem) "SYSTEM" else "User",
                startTime = System.currentTimeMillis() - (3600_000 * (1 + Math.random() * 10).toLong()),
                isSystem = isSystem,
                isProtected = isSystem,
                commandLine = if (isSystem) null else "C:\\Program Files\\$name",
                executablePath = if (isSystem) "C:\\Windows\\System32\\$name" else "C:\\Program Files\\$name"
            ))
        }

        return processes
    }
}