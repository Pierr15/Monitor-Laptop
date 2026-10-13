package com.aetherglass.presentation.screen.processes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModelScope
import com.aetherglass.core.util.Result
import com.aetherglass.domain.model.ConnectionStatus
import com.aetherglass.domain.model.Device
import com.aetherglass.domain.model.ProcessInfo
import com.aetherglass.domain.usecase.DeviceUseCase
import com.aetherglass.domain.usecase.ProcessUseCase
import com.aetherglass.domain.usecase.ConnectionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProcessesViewModel @Inject constructor(
    private val deviceUseCase: DeviceUseCase,
    private val processUseCase: ProcessUseCase,
    private val connectionUseCase: ConnectionUseCase
) : ViewModel() {

    private val _device = MutableStateFlow<Device?>(null)
    val device: StateFlow<Device?> = _device

    private val _processes = MutableStateFlow<Result<List<ProcessInfo>>?>(null)
    val processes: StateFlow<Result<List<ProcessInfo>>?> = _processes

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Unknown)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus

    init {
        loadActiveDevice()
    }

    private fun loadActiveDevice() {
        deviceUseCase.observeActiveDevice().collectAsStateWithLifecycle().value?.let { deviceId ->
            deviceId?.let { loadDevice(it) }
        }
    }

    private fun loadDevice(deviceId: java.util.UUID) {
        viewModelScope.launch {
            deviceUseCase.getDevice(deviceId).onSuccess { device ->
                _device.value = device
                startProcessesObservation(deviceId)
                startConnectionObservation(deviceId)
            }
        }
    }

    private fun startProcessesObservation(deviceId: java.util.UUID) {
        viewModelScope.launch {
            processUseCase.observeProcesses(deviceId).collect { result ->
                _processes.value = result
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

    fun terminateProcess(pid: Int, force: Boolean) {
        _device.value?.let { device ->
            viewModelScope.launch {
                processUseCase.terminateProcess(device.id, pid, force)
            }
        }
    }

    fun refresh() {
        _device.value?.let { device ->
            viewModelScope.launch {
                processUseCase.getProcesses(device.id).onSuccess { processes ->
                    _processes.value = Result.success(processes)
                }
            }
        }
    }
}