package com.aetherglass.presentation.screen.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModelScope
import com.aetherglass.core.util.Result
import com.aetherglass.domain.model.ConnectionStatus
import com.aetherglass.domain.model.Device
import com.aetherglass.domain.model.FileEntry
import com.aetherglass.domain.model.FileListing
import com.aetherglass.domain.usecase.DeviceUseCase
import com.aetherglass.domain.usecase.FileUseCase
import com.aetherglass.domain.usecase.ConnectionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class FilesViewModel @Inject constructor(
    private val deviceUseCase: DeviceUseCase,
    private val fileUseCase: FileUseCase,
    private val connectionUseCase: ConnectionUseCase
) : ViewModel() {

    private val _device = MutableStateFlow<Device?>(null)
    val device: StateFlow<Device?> = _device

    private val _currentPath = MutableStateFlow<String>("C:\\Users")
    val currentPath: StateFlow<String> = _currentPath

    private val _listing = MutableStateFlow<Result<FileListing>?>(null)
    val listing: StateFlow<Result<FileListing>?> = _listing

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Unknown)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus

    private val _pathHistory = mutableListOf<String>()

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
                startConnectionObservation(deviceId)
                browsePath(_currentPath.value)
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

    fun browsePath(path: String) {
        _device.value?.let { device ->
            _pathHistory.add(_currentPath.value)
            _currentPath.value = path
            viewModelScope.launch {
                fileUseCase.listFiles(device.id, path).onSuccess { listing ->
                    _listing.value = Result.success(listing)
                }
            }
        }
    }

    fun browseUp() {
        _listing.value?.getOrNull()?.parentPath?.let { parent ->
            browsePath(parent)
        }
    }

    fun downloadFile(file: FileEntry) {
        // TODO: Implement download
    }

    fun refresh() {
        _device.value?.let { device ->
            viewModelScope.launch {
                fileUseCase.listFiles(device.id, _currentPath.value).onSuccess { listing ->
                    _listing.value = Result.success(listing)
                }
            }
        }
    }
}