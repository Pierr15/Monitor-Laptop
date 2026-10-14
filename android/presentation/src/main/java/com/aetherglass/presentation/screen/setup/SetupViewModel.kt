package com.aetherglass.presentation.screen.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aetherglass.core.util.Result
import com.aetherglass.domain.model.Device
import com.aetherglass.domain.model.DeviceRegistrationRequest
import com.aetherglass.domain.model.DeviceCapabilities
import com.aetherglass.domain.usecase.AuthUseCase
import com.aetherglass.domain.usecase.DeviceUseCase
import com.aetherglass.domain.usecase.PreferencesUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SetupViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val deviceUseCase: DeviceUseCase,
    private val preferencesUseCase: PreferencesUseCase
) : ViewModel() {

    private val _devices = MutableStateFlow<Result<List<Device>>?>(null)
    val devices: StateFlow<Result<List<Device>>?> = _devices

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadDevices()
    }

    fun loadDevices() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            authUseCase.getCurrentUser().onSuccess { user ->
                if (user != null) {
                    deviceUseCase.getRegisteredDevices().onSuccess { devices ->
                        _devices.value = Result.success(devices)
                        _isLoading.value = false
                    }.onFailure { error ->
                        _error.value = error.message
                        _isLoading.value = false
                    }
                } else {
                    _devices.value = Result.success(emptyList())
                    _isLoading.value = false
                }
            }.onFailure { error ->
                _error.value = error.message
                _isLoading.value = false
            }
        }
    }

    fun registerDevice(name: String, platform: String, agentVersion: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val request = DeviceRegistrationRequest(
                name = name,
                platform = platform,
                agentVersion = agentVersion,
                capabilities = DeviceCapabilities()
            )
            deviceUseCase.registerDevice(request).onSuccess { response ->
                _lastRegisteredDevice.value = response.device
                _isLoading.value = false
                loadDevices()
            }.onFailure { error ->
                _error.value = error.message
                _isLoading.value = false
            }
        }
    }

    fun setActiveDevice(deviceId: java.util.UUID?) {
        viewModelScope.launch {
            deviceUseCase.setActiveDevice(deviceId)
        }
    }

    fun removeDevice(deviceId: java.util.UUID) {
        _isLoading.value = true
        viewModelScope.launch {
            deviceUseCase.removeDevice(deviceId).onSuccess {
                loadDevices()
            }.onFailure { error ->
                _error.value = error.message
                _isLoading.value = false
            }
        }
    }

    private val _lastRegisteredDevice = MutableStateFlow<Device?>(null)
    val lastRegisteredDevice: StateFlow<Device?> = _lastRegisteredDevice

    val demoMode: Boolean
        get() = preferencesUseCase.getDemoMode()

    fun setDemoMode(enabled: Boolean) {
        preferencesUseCase.setDemoMode(enabled)
    }
}