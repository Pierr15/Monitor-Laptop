package com.aetherglass.presentation.screen.performance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aetherglass.core.util.Result
import com.aetherglass.domain.model.ConnectionStatus
import com.aetherglass.domain.model.Device
import com.aetherglass.domain.model.SystemMetrics
import com.aetherglass.domain.usecase.DeviceUseCase
import com.aetherglass.domain.usecase.MetricsUseCase
import com.aetherglass.domain.usecase.ConnectionUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class PerformanceViewModel @Inject constructor(
    private val deviceUseCase: DeviceUseCase,
    private val metricsUseCase: MetricsUseCase,
    private val connectionUseCase: ConnectionUseCase
) : ViewModel() {

    private val _device = MutableStateFlow<Device?>(null)
    val device: StateFlow<Device?> = _device

    private val _metrics = MutableStateFlow<Result<SystemMetrics>?>(null)
    val metrics: StateFlow<Result<SystemMetrics>?> = _metrics

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Unknown)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus

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
                startMetricsObservation(deviceId)
                startConnectionObservation(deviceId)
            }
        }
    }

    private fun startMetricsObservation(deviceId: java.util.UUID) {
        viewModelScope.launch {
            metricsUseCase.observeMetrics(deviceId).collect { result ->
                _metrics.value = result
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

    fun refresh() {
        _device.value?.let { device ->
            viewModelScope.launch {
                metricsUseCase.getCurrentMetrics(device.id).onSuccess { metrics ->
                    _metrics.value = Result.success(metrics)
                }
            }
        }
    }
}