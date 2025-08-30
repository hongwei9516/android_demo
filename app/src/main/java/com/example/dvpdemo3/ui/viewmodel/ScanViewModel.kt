package com.example.dvpdemo3.ui.viewmodel

import android.Manifest
import android.app.Application
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dvpdemo3.data.ConnectedDevice
import com.example.dvpdemo3.data.ConnectedDevicesRepository
import com.example.dvpdemo3.data.DeviceType
import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.State
import com.juul.kable.logs.Logging
import com.juul.kable.logs.SystemLogEngine
import com.juul.kable.peripheral
import com.juul.kable.read
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi


class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val scanner = Scanner {
        logging {
            engine = SystemLogEngine
            level = Logging.Level.Data
        }
    }

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val discoveredDevicesMap = mutableMapOf<String, ScanResult>()
    private val connectedPeripherals = mutableMapOf<String, Peripheral>()
    private val peripheralStateJobs = mutableMapOf<String, Job>()

    private val _deviceConnectionStates = MutableStateFlow<Map<String, State>>(emptyMap())
    val deviceConnectionStates: StateFlow<Map<String, State>> =
        _deviceConnectionStates.asStateFlow()

    private val repository = ConnectedDevicesRepository(application)

    val historyDevices: StateFlow<List<ConnectedDevice>> = repository.connectedDevices.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        emptyList()
    )

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted.asStateFlow()

    private var scanJob: Job? = null

    private val _snackbarMessages = MutableSharedFlow<String>()
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()


    fun setPermissionsGranted(isGranted: Boolean) {
        _permissionsGranted.value = isGranted
    }

    fun startScan(selectedDeviceTypes: List<DeviceType>) {
        Log.i(TAG, "Starting scan for device type: $selectedDeviceTypes")
        scanJob?.cancel()

        discoveredDevicesMap.clear()
        _uiState.update {
            it.copy(
                isScanning = true,
                discoveredDevices = emptyList(),
                errorMessage = null
            )
        }

        scanJob = scanner.advertisements
            .catch { cause ->
                Log.e(TAG, "Scan failed: $cause")
                _uiState.update { state ->
                    state.copy(
                        isScanning = false,
                        errorMessage = "Scan failed: ${cause.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
            .filter { it.name != null }
            .map { advertisement ->
                val deviceType = getDeviceTypeFromAdvertisement(advertisement)
                advertisement to deviceType
            }
            .filter { (_, deviceType) ->
                deviceType in selectedDeviceTypes
            }
            .onEach { (advertisement, deviceType) ->
                val scanResult = ScanResult(
                    deviceName = advertisement.name!!,
                    address = advertisement.address,
                    rssi = advertisement.rssi,
                    deviceType = deviceType
                )
                discoveredDevicesMap[scanResult.address] = scanResult

                _uiState.update { currentState ->
                    currentState.copy(
                        discoveredDevices = discoveredDevicesMap.values.toList()
                            .sortedByDescending { it.rssi }
                    )
                }
            }
            .launchIn(viewModelScope)
    }


    private fun getDeviceTypeFromAdvertisement(advertisement: Advertisement): DeviceType {
        return when {
            advertisement.name?.contains("WT300", ignoreCase = true) == true -> DeviceType.WT300
            advertisement.name?.contains("CPT1", ignoreCase = true) == true -> DeviceType.CPT1
            advertisement.name?.contains("CPH1", ignoreCase = true) == true -> DeviceType.CPH1
            advertisement.name?.contains("WTL-2", ignoreCase = true) == true -> DeviceType.WTL_2
            advertisement.name?.contains("WPL-2", ignoreCase = true) == true -> DeviceType.WPL_2
            advertisement.name?.contains("WHL-LM2", ignoreCase = true) == true -> DeviceType.WHL_LM2
            else -> DeviceType.UNKNOW
        }
    }


    fun stopScan() {
        Log.i(TAG, "Stopping scan")
        scanJob?.cancel()
        scanJob = null
        _uiState.update { it.copy(isScanning = false) }
    }


    fun connectToDevice(scanResult: ScanResult) {
        viewModelScope.launch {

            if (connectedPeripherals.containsKey(scanResult.address)) {
                Log.d(TAG, "Device already connecting/connected. Ignoring request.")
                return@launch
            }

            try {

                val peripheral = viewModelScope.peripheral(scanResult.address)
                connectedPeripherals[scanResult.address] = peripheral

                peripheralStateJobs[scanResult.address] = peripheral.state
                    .onEach { state ->
                        Log.d(TAG, "Device ${scanResult.deviceName} state: $state")
                        _deviceConnectionStates.update { it + (scanResult.address to state) }
                        if (state is State.Connected) {
                            _uiState.update { it.copy(infoMessage = "连接成功！") }
                            _snackbarMessages.emit("${scanResult.deviceName} 连接成功！")
                        }
                    }
                    .catch { throwable ->
                        _uiState.update { it.copy(errorMessage = "连接 ${scanResult.deviceName} 失败: ${throwable.message ?: "未知错误"}") }
                        cleanupAfterConnectionFailure(scanResult.address)
                        Log.e(TAG, "连接 ${scanResult.deviceName} 失败", throwable)
                    }
                    .launchIn(viewModelScope)

                Log.d(TAG, "Connecting to device: ${scanResult.deviceName}")
                peripheral.connect()
                Log.d(
                    TAG,
                    "Connection successful to: ${scanResult.deviceName}. Discovering services..."
                )

                repository.saveConnectedDevice(
                    ConnectedDevice(
                        address = scanResult.address,
                        deviceName = scanResult.deviceName,
                        deviceType = scanResult.deviceType
                    )
                )

                readDeviceCharacteristics(peripheral)

            } catch (e: Exception) {
                Log.e(TAG, "Connection to ${scanResult.deviceName} failed: ${e.message}", e)
                _snackbarMessages.emit("连接 ${scanResult.deviceName} 失败: ${e.message ?: "未知错误"}")
                cleanupAfterConnectionFailure(scanResult.address)
                _uiState.update {
                    it.copy(errorMessage = "Connection failed: ${e.localizedMessage ?: "Unknown error"}")
                }
            }
        }
    }


    fun connectToDevice(connectedDevice: ConnectedDevice) {
        connectToDevice(
            ScanResult(
                deviceName = connectedDevice.deviceName,
                address = connectedDevice.address,
                rssi = 0,
                deviceType = connectedDevice.deviceType
            )
        )
    }


    fun disconnectDevice(scanResult: ScanResult) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Disconnecting from ${scanResult.deviceName}")
                connectedPeripherals[scanResult.address]?.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error during disconnection: ${e.message}", e)
            } finally {
                cleanupAfterDisconnection(scanResult.address)
                _snackbarMessages.emit("${scanResult.deviceName} 已断开连接！")
                _uiState.update { it.copy(infoMessage = "Disconnected.") }
            }

        }
    }


    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }


    fun clearInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }


    fun checkPermissions() {
        val granted = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                getApplication(),
                it
            ) == PermissionChecker.PERMISSION_GRANTED
        }
        _permissionsGranted.value = granted
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }


    @OptIn(ExperimentalUuidApi::class)
    private suspend fun readDeviceCharacteristics(peripheral: Peripheral) {
        try {
            val services = peripheral.services.first() ?: emptyList()
            services.forEach { service ->
                Log.d(TAG, "Service UUID: ${service.serviceUuid}")
                service.characteristics.forEach { characteristic ->
                    Log.d(TAG, "  Characteristic UUID: ${characteristic.characteristicUuid}")
                    Log.d(TAG, "    Properties: ${characteristic.properties}")

                    if (characteristic.properties.read) {
                        try {
                            val value = peripheral.read(characteristic)
                            Log.d(TAG, "    Value (Hex): ${value.toHexString()}")
                            Log.d(TAG, "    Value (String): ${String(value)}")
                        } catch (e: Exception) {
                            Log.e(
                                TAG,
                                "    Failed to read characteristic ${characteristic.characteristicUuid}: ${e.localizedMessage}"
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to discover services or read characteristics: ${e.message}", e)
        }
    }


    private fun cleanupAfterConnectionFailure(address: String) {
        peripheralStateJobs[address]?.cancel()
        peripheralStateJobs.remove(address)
        connectedPeripherals.remove(address)
        _deviceConnectionStates.update { it - address }
    }


    private fun cleanupAfterDisconnection(address: String) {
        peripheralStateJobs[address]?.cancel()
        peripheralStateJobs.remove(address)
        connectedPeripherals.remove(address)
        _deviceConnectionStates.update { it - address }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            connectedPeripherals.values.forEach { it.disconnect() }
            connectedPeripherals.clear()
            peripheralStateJobs.values.forEach { it.cancel() }
            peripheralStateJobs.clear()
        }
        Log.d(TAG, "ScanViewModel cleared.")
    }

    private companion object {
        private const val TAG = "ScanViewModel"
    }
}

val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
    )
} else {
    arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}


fun ByteArray.toHexString(): String =
    joinToString(separator = " ") { "%02X".format(it) }

data class ScanUiState(
    val isScanning: Boolean = false,
    val discoveredDevices: List<ScanResult> = emptyList(),
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

data class ScanResult(
    val deviceName: String,
    val address: String,
    val rssi: Int,
    val deviceType: DeviceType,
)