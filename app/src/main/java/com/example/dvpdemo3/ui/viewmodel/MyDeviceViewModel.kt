package com.example.dvpdemo3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dvpdemo3.R
import com.example.dvpdemo3.data.Device
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 修正：定义所有用户交互事件
sealed class MyDeviceUiEvent {
    object ToggleFilterSheet : MyDeviceUiEvent()
    object DismissFilter : MyDeviceUiEvent()
    object ConfirmFilter : MyDeviceUiEvent()
    object ResetFilter : MyDeviceUiEvent()
    data class ToggleDeviceSelected(
        val category: String,
        val device: Device,
        val isSelected: Boolean
    ) : MyDeviceUiEvent()

    object ShowAddDeviceScreen : MyDeviceUiEvent()
    object DismissAddDeviceScreen : MyDeviceUiEvent()

}

// 修正：将顶层变量重命名为 initialCategorizedDevices，以避免与数据类参数的歧义
val initialCategorizedDevices = mapOf(
    "压力" to listOf(
        Device("手持高精度测温仪", "压力", R.drawable.ic_icon_mp1),
        Device("手持高精度测温仪2", "压力", R.drawable.ic_icon_mp1)
    ),
    "温度" to listOf(
        Device("精密铂电阻温湿度计", "温度", R.drawable.ic_icon_cpt1),
        Device("实时无线记录器", "温度", R.drawable.ic_icon_zigbee),
        Device("手持高精度测温仪", "温度", R.drawable.ic_icon_mp1)
    ),
    "过程" to listOf(
        Device("实时无线记录器", "过程", R.drawable.ic_icon_zigbee)
    )
)

data class MyDeviceUiState(
    val showFilterSheet: Boolean = false,
    val totalSelectedCount: Int = 0,
    val showAddDeviceScreen: Boolean = false,
    val categorizedDevices: Map<String, List<Device>> = initialCategorizedDevices, // 修正
    val selectedDevicesMap: Map<String, List<Device>> = initialCategorizedDevices.keys.associateWith { emptyList() } // 修正
)

class MyDeviceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MyDeviceUiState())
    val uiState: StateFlow<MyDeviceUiState> = _uiState.asStateFlow()

    // 修正：统一的事件处理入口，确保所有状态变更都通过这里
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
        _uiState.update { it.copy(selectedDevicesMap = initialCategorizedDevices.keys.associateWith { emptyList() }) }
    }

    private fun toggleDeviceSelected(category: String, device: Device, isSelected: Boolean) {
        val currentList =
            _uiState.value.selectedDevicesMap[category]?.toMutableList() ?: mutableListOf()
        if (isSelected) {
            currentList.add(device)
        } else {
            currentList.remove(device)
        }
        _uiState.update {
            it.copy(
                selectedDevicesMap = it.selectedDevicesMap.toMutableMap().apply {
                    put(category, currentList)
                }
            )
        }
    }

    private fun showAddDeviceScreen() {
        _uiState.update { it.copy(showAddDeviceScreen = true) }
    }

    private fun dismissAddDeviceScreen() {
        _uiState.update { it.copy(showAddDeviceScreen = false) }
    }
}