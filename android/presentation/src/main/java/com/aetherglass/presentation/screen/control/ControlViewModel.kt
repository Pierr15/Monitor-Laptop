package com.aetherglass.presentation.screen.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aetherglass.core.util.Result
import com.aetherglass.domain.model.Command
import com.aetherglass.domain.model.CommandResponse
import com.aetherglass.domain.model.CommandStatus
import com.aetherglass.domain.model.ConnectionStatus
import com.aetherglass.domain.model.Device
import com.aetherglass.domain.model.DeviceSettings
import com.aetherglass.domain.usecase.DeviceUseCase
import com.aetherglass.domain.usecase.PowerUseCase
import com.aetherglass.domain.usecase.CommandUseCase
import com.aetherglass.domain.usecase.ConnectionUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ControlViewModel @Inject constructor(
    private val deviceUseCase: DeviceUseCase,
    private val powerUseCase: PowerUseCase,
    private val commandUseCase: CommandUseCase,
    private val connectionUseCase: ConnectionUseCase
) : ViewModel() {

    private val _device = MutableStateFlow<Device?>(null)
    val device: StateFlow<Device?> = _device

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Unknown)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus

    private val _lastCommandResult = MutableStateFlow<String?>(null)
    val lastCommandResult: StateFlow<String?> = _lastCommandResult

    init {
        loadActiveDevice()
    }

    private fun loadActiveDevice() {
        viewModelScope.launch {
            deviceUseCase.observeActiveDevice().collect { deviceId ->
                if (deviceId != null) loadDevice(deviceId)
            }
        }
    }

    private fun loadDevice(deviceId: java.util.UUID) {
        viewModelScope.launch {
            deviceUseCase.getDevice(deviceId).onSuccess { device ->
                _device.value = device
                startConnectionObservation(deviceId)
            }
        }
    }

    private fun startConnectionObservation(deviceId: java.util.UUID) {
        viewModelScope.launch {
            connectionUseCase.observeConnectionStatus(deviceId).collect { status ->
                _connectionStatus.value = status
            }
        }
    }

    fun sleep(delaySeconds: Int = 0) {
        executePowerAction(Command.PowerActionType.Sleep, delaySeconds)
    }

    fun restart(delaySeconds: Int = 0) {
        executePowerAction(Command.PowerActionType.Restart, delaySeconds)
    }

    fun shutdown(delaySeconds: Int = 0) {
        executePowerAction(Command.PowerActionType.Shutdown, delaySeconds)
    }

    private fun executePowerAction(action: Command.PowerActionType, delaySeconds: Int) {
        _device.value?.let { device ->
            viewModelScope.launch {
                powerUseCase.executePowerAction(device.id, action, delaySeconds).onSuccess { response ->
                    _lastCommandResult.value = "${action.name} ${if (response.status == CommandStatus.Succeeded) "initiated" else "failed: ${response.error?.message}"}"
                }
            }
        }
    }

    fun sendPing() {
        _device.value?.let { device ->
            viewModelScope.launch {
                val request = CommandRequest(
                    deviceId = device.id,
                    command = Command.Ping(),
                    idempotencyKey = java.util.UUID.randomUUID().toString(),
                    requestedBy = java.util.UUID.randomUUID() // TODO: Get actual user ID
                )
                commandUseCase.sendCommand(request).onSuccess { response ->
                    _lastCommandResult.value = "Ping: ${response.status.name} - ${response.result ?: "pong"}"
                }
            }
        }
    }
}