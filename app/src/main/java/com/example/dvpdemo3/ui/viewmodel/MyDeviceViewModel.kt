package com.example.dvpdemo3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dvpdemo3.R
import com.example.dvpdemo3.data.Device
import com.example.dvpdemo3.data.DeviceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Defines all possible user interactions (events) on the MyDevice screen.
 */
sealed interface MyDeviceUiEvent {
    object ToggleFilterSheet : MyDeviceUiEvent
    object DismissFilter : MyDeviceUiEvent
    object ConfirmFilter : MyDeviceUiEvent
    object ResetFilter : MyDeviceUiEvent
    data class ToggleDeviceSelected(
        val category: String,
        val device: Device,
        val isSelected: Boolean
    ) : MyDeviceUiEvent

    object ShowAddDeviceScreen : MyDeviceUiEvent
    object DismissAddDeviceScreen : MyDeviceUiEvent
}

/**
 * Represents the UI state for the MyDevice screen.
 *
 * @param showFilterSheet Whether the product filter sheet is visible.
 * @param showAddDeviceScreen Whether the device scanning screen is visible.
 * @param categorizedDevices The master list of all available devices, grouped by category.
 * @param selectedDevicesMap The map of currently selected devices, grouped by category.
 */
data class MyDeviceUiState(
    val showFilterSheet: Boolean = false,
    val showAddDeviceScreen: Boolean = false,
    val totalSelectedCount: Int = 0,
    val categorizedDevices: Map<String, List<Device>> = initialCategorizedDevices,
    val selectedDevicesMap: Map<String, List<Device>> = initialCategorizedDevices.keys.associateWith { emptyList() }
)

/**
 * ViewModel for the MyDevice screen.
 * Manages the UI state and handles user events.
 */
class MyDeviceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MyDeviceUiState())
    val uiState: StateFlow<MyDeviceUiState> = _uiState.asStateFlow()

    /**
     * Central entry point for all UI events.
     * This ensures all state mutations are handled consistently.
     */
    fun onEvent(event: MyDeviceUiEvent) {
        viewModelScope.launch {
            when (event) {
                is MyDeviceUiEvent.ToggleFilterSheet -> toggleFilterSheet()
                is MyDeviceUiEvent.DismissFilter -> dismissFilter()
                is MyDeviceUiEvent.ConfirmFilter -> confirmFilter()
                is MyDeviceUiEvent.ResetFilter -> resetFilter()
                is MyDeviceUiEvent.ToggleDeviceSelected -> toggleDeviceSelected(
                    event.category,
                    event.device,
                    event.isSelected
                )
                is MyDeviceUiEvent.ShowAddDeviceScreen -> showAddDeviceScreen()
                is MyDeviceUiEvent.DismissAddDeviceScreen -> dismissAddDeviceScreen()
            }
        }
    }

    /**
     * Returns a flattened list of all selected [DeviceType]s.
     */
    fun getSelectedDeviceTypes(): List<DeviceType> {
        return uiState.value.selectedDevicesMap.values.flatMap { devices ->
            devices.flatMap { it.type }
        }
    }

    private fun toggleFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = !it.showFilterSheet) }
    }

    private fun dismissFilter() {
        _uiState.update { it.copy(showFilterSheet = false) }
    }

    private fun confirmFilter() {
        val totalCount = _uiState.value.selectedDevicesMap.values.sumOf { it.size }
        _uiState.update { it.copy(totalSelectedCount = totalCount, showFilterSheet = false) }
    }

    private fun resetFilter() {
        _uiState.update {
            it.copy(selectedDevicesMap = it.categorizedDevices.keys.associateWith { emptyList() })
        }
    }

    private fun toggleDeviceSelected(category: String, device: Device, isSelected: Boolean) {
        _uiState.update { currentState ->
            val updatedMap = currentState.selectedDevicesMap.toMutableMap()
            val currentList = updatedMap[category]?.toMutableList() ?: mutableListOf()

            if (isSelected) {
                if (!currentList.contains(device)) currentList.add(device)
            } else {
                currentList.remove(device)
            }
            updatedMap[category] = currentList
            currentState.copy(selectedDevicesMap = updatedMap)
        }
    }

    private fun showAddDeviceScreen() {
        _uiState.update { it.copy(showAddDeviceScreen = true) }
    }

    private fun dismissAddDeviceScreen() {
        _uiState.update { it.copy(showAddDeviceScreen = false) }
    }
}


// This data could be moved to a separate data source file in a larger project.
val initialCategorizedDevices = mapOf(
    "压力" to listOf(
        Device("手持高精度测温仪", "压力", listOf(DeviceType.WT300), R.drawable.ic_icon_mp1),
    ),
    "温度" to listOf(
        Device(
            "精密铂电阻温湿度计",
            "温度",
            listOf(DeviceType.CPT1, DeviceType.CPH1),
            R.drawable.ic_icon_cpt1
        ),
    ),
    "过程" to listOf(
        Device(
            "实时无线记录器",
            "过程",
            listOf(
                DeviceType.WTL_2, DeviceType.WPL_2,
                DeviceType.WHL_LM2
            ), R.drawable.ic_icon_zigbee
        )
    )
)