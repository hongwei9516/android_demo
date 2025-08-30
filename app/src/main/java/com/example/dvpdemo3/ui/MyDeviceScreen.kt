package com.example.dvpdemo3.ui

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dvpdemo3.ui.components.mydeivce.DeviceSummaryCard
import com.example.dvpdemo3.ui.components.mydeivce.MyDeviceHeader
import com.example.dvpdemo3.ui.components.mydeivce.ProductFilterSheet
import com.example.dvpdemo3.ui.viewmodel.*

private const val ANIMATION_DURATION = 300

@Composable
fun MyDeviceScreen(
    modifier: Modifier = Modifier,
    myDeviceViewModel: MyDeviceViewModel = viewModel(),
    scanViewModel: ScanViewModel = viewModel(
        factory = ScanViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by myDeviceViewModel.uiState.collectAsState()

    val animatedUiColors = getAnimatedUiColors(uiState.showFilterSheet)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            animatedUiColors.backgroundStartColor,
                            animatedUiColors.backgroundEndColor
                        ),
                        endY = 800f
                    )
                )
                .statusBarsPadding()
        ) {

            MyDeviceHeader(
                contentColor = animatedUiColors.contentColor,
                showFilterSheet = uiState.showFilterSheet,
                onFilterClick = { myDeviceViewModel.onEvent(MyDeviceUiEvent.ToggleFilterSheet) },
                onClearHistoryClick = {scanViewModel.clearHistory()},
                totalSelectedCount = uiState.totalSelectedCount
            )

            Spacer(Modifier.height(10.dp))

            MyDeviceContent(
                uiState = uiState,
                onEvent = myDeviceViewModel::onEvent,
                scanViewModel = scanViewModel,
                myDeviceViewModel = myDeviceViewModel
            )
        }

        AnimatedVisibility(
            visible = uiState.showAddDeviceScreen,
            enter = scaleIn(animationSpec = tween(ANIMATION_DURATION)) + fadeIn(tween(ANIMATION_DURATION)),
            exit = scaleOut(animationSpec = tween(ANIMATION_DURATION)) + fadeOut(tween(ANIMATION_DURATION))
        ) {
            BluetoothScanScreen(
                onBackClick = { myDeviceViewModel.onEvent(MyDeviceUiEvent.DismissAddDeviceScreen) },
                scanViewModel = scanViewModel
            )
        }
    }
}

private data class AnimatedUiColors(
    val backgroundStartColor: Color,
    val backgroundEndColor: Color,
    val contentColor: Color
)

@Composable
private fun getAnimatedUiColors(showFilterSheet: Boolean): AnimatedUiColors {
    val targetStartColor = if (showFilterSheet) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
    val targetEndColor = if (showFilterSheet) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background
    val targetContentColor = if (showFilterSheet) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary

    val animationSpec = tween<Color>(durationMillis = ANIMATION_DURATION)

    val backgroundStartColor by animateColorAsState(targetValue = targetStartColor, animationSpec = animationSpec, label = "bgStartColor")
    val backgroundEndColor by animateColorAsState(targetValue = targetEndColor, animationSpec = animationSpec, label = "bgEndColor")
    val contentColor by animateColorAsState(targetValue = targetContentColor, animationSpec = animationSpec, label = "contentColor")

    return AnimatedUiColors(backgroundStartColor, backgroundEndColor, contentColor)
}

@Composable
private fun MyDeviceContent(
    uiState: MyDeviceUiState,
    onEvent: (MyDeviceUiEvent) -> Unit,
    scanViewModel: ScanViewModel,
    myDeviceViewModel: MyDeviceViewModel
) {
    val permissionsGranted by scanViewModel.permissionsGranted.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            scanViewModel.setPermissionsGranted(true)
            val selectedDeviceTypes = myDeviceViewModel.getSelectedDeviceTypes()
            scanViewModel.startScan(selectedDeviceTypes)
            onEvent(MyDeviceUiEvent.ShowAddDeviceScreen)
        } else {

        }
    }

    LaunchedEffect(Unit) {
        scanViewModel.checkPermissions()
    }

    AnimatedContent(
        targetState = uiState.showFilterSheet,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            if (targetState) {
                slideInVertically(animationSpec = tween(ANIMATION_DURATION)) { -it } togetherWith
                        fadeOut(animationSpec = tween(ANIMATION_DURATION))
            } else {
                fadeIn(animationSpec = tween(ANIMATION_DURATION)) togetherWith
                        slideOutVertically(animationSpec = tween(ANIMATION_DURATION)) { -it }
            }
        }, label = "ContentAnimation"
    ) { isFilterVisible ->
        if (isFilterVisible) {
            ProductFilterSheetWithOverlay(
                uiState = uiState,
                onEvent = onEvent
            )
        } else {
            DeviceListSection(
                onAddDeviceClick = {
                    if (permissionsGranted) {
                        val selectedDeviceTypes = myDeviceViewModel.getSelectedDeviceTypes()
                        scanViewModel.startScan(selectedDeviceTypes)
                        onEvent(MyDeviceUiEvent.ShowAddDeviceScreen)
                    } else {
                        permissionLauncher.launch(REQUIRED_PERMISSIONS)
                    }
                },
                onScanQrClick = {  },
                myDeviceViewModel
            )
        }
    }
}

@Composable
private fun ProductFilterSheetWithOverlay(
    uiState: MyDeviceUiState,
    onEvent: (MyDeviceUiEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // No ripple effect
                onClick = { onEvent(MyDeviceUiEvent.DismissFilter) }
            )
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
                .align(Alignment.TopCenter)
                .clickable(enabled = false) {}
        )
    }
}

@Composable
private fun DeviceListSection(onAddDeviceClick: () -> Unit, onScanQrClick: () -> Unit, myDeviceViewModel: MyDeviceViewModel) {
    Box(modifier = Modifier.fillMaxWidth()) {
        DeviceSummaryCard(
            modifier = Modifier.align(Alignment.TopCenter),
            onAddDeviceClick = onAddDeviceClick,
            onScanQrClick = onScanQrClick,
            myDeviceViewModel = myDeviceViewModel
        )
    }
}