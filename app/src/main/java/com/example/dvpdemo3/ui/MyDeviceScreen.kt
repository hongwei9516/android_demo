package com.example.dvpdemo3.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dvpdemo3.ui.components.mydeivce.DeviceSummaryCard
import com.example.dvpdemo3.ui.components.mydeivce.MyDeviceHeader
import com.example.dvpdemo3.ui.components.mydeivce.ProductFilterSheet
import com.example.dvpdemo3.ui.theme.DvpDemo3Theme
import com.example.dvpdemo3.ui.viewmodel.MyDeviceViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dvpdemo3.ui.viewmodel.MyDeviceUiEvent
import com.example.dvpdemo3.ui.viewmodel.MyDeviceUiState
import com.example.dvpdemo3.ui.viewmodel.initialCategorizedDevices

@Composable
fun MyDeviceScreen(modifier: Modifier = Modifier, viewModel: MyDeviceViewModel = viewModel()) {

    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {

        val backgroundStartColor =
            if (uiState.showFilterSheet) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
        val backgroundEndColor = if (uiState.showFilterSheet) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background
        val contentColor = if (uiState.showFilterSheet) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary

        val animatedBackgroundStartColor by animateColorAsState(
            targetValue = backgroundStartColor,
            animationSpec = tween(durationMillis = 300)
        )
        val animatedBackgroundEndColor by animateColorAsState(
            targetValue = backgroundEndColor,
            animationSpec = tween(durationMillis = 300)
        )
        val animatedContentColor by animateColorAsState(
            targetValue = contentColor,
            animationSpec = tween(durationMillis = 300)
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to animatedBackgroundStartColor,
                            0.4f to animatedBackgroundEndColor,
                            1.0f to animatedBackgroundEndColor
                        )
                    )
                )
                .statusBarsPadding()
        ) {
            MyDeviceHeader(
                animatedContentColor = animatedContentColor,
                showFilterSheet = uiState.showFilterSheet,
                onFilterClick = { viewModel.onEvent(MyDeviceUiEvent.ToggleFilterSheet) },
                totalSelectedCount = uiState.totalSelectedCount
            )

            Spacer(Modifier.height(10.dp))

            MyDeviceContent(
                uiState = uiState,
                onEvent = viewModel::onEvent
            )
        }

        AnimatedVisibility(
            visible = uiState.showAddDeviceScreen,
            enter = scaleIn(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
            exit = scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        ) {
            BluetoothScanScreen(
                onBackClick = { viewModel.onEvent(MyDeviceUiEvent.DismissAddDeviceScreen) }
            )
        }
    }
}

@Composable
private fun MyDeviceContent(
    uiState: MyDeviceUiState,
    onEvent: (MyDeviceUiEvent) -> Unit
) {
    val sheetTransition = remember {
        slideInVertically(
            animationSpec = tween(durationMillis = 300),
            initialOffsetY = { -it }
        ).togetherWith(
            fadeOut(
                animationSpec = tween(durationMillis = 300),
            )
        )
    }

    val cardTransition = remember {
        fadeIn(
            animationSpec = tween(durationMillis = 300)
        ).togetherWith(
            slideOutVertically(
                animationSpec = tween(durationMillis = 300),
                targetOffsetY = { -it }
            )
        )
    }

    AnimatedContent(
        targetState = uiState.showFilterSheet,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            if (targetState) {
                sheetTransition
            } else {
                cardTransition
            }
        }
    ) { isFilterVisible ->
        if (isFilterVisible) {
            ProductFilterSheetWithOverlay(
                uiState = uiState,
                onEvent = onEvent
            )
        } else {
            DeviceListSection(onAddDeviceClick = { onEvent(MyDeviceUiEvent.ShowAddDeviceScreen) })
        }
    }
}

/**
 * 包含筛选弹窗和背景遮罩。
 */
@Composable
private fun ProductFilterSheetWithOverlay(
    uiState: MyDeviceUiState,
    onEvent: (MyDeviceUiEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = { onEvent(MyDeviceUiEvent.DismissFilter) })
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
    ) {
        ProductFilterSheet(
            categorizedDevices = uiState.categorizedDevices,
            selectedDevicesMap = uiState.selectedDevicesMap,
            onConfirm = { onEvent(MyDeviceUiEvent.ConfirmFilter) },
            onReset = { onEvent(MyDeviceUiEvent.ResetFilter) },
            onToggleSelected = { category, device, isSelected ->
                onEvent(MyDeviceUiEvent.ToggleDeviceSelected(category, device, isSelected))
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}
        )
    }
}

@Composable
private fun DeviceListSection(onAddDeviceClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        DeviceSummaryCard(
            modifier = Modifier.align(Alignment.TopCenter),
            onAddDeviceClick = onAddDeviceClick
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyDeviceScreenPreview() {
    DvpDemo3Theme {
        MyDeviceScreen()
    }
}


@Preview(showBackground = true)
@Composable
fun MyDeviceScreenWithFilterPreview() {
    DvpDemo3Theme {
        val mockUiState = MyDeviceUiState(
            showFilterSheet = true,
            categorizedDevices = initialCategorizedDevices,
            selectedDevicesMap = initialCategorizedDevices.keys.associateWith { emptyList() }
        )
        MyDeviceContent(
            uiState = mockUiState,
            onEvent = {}
        )
    }
}