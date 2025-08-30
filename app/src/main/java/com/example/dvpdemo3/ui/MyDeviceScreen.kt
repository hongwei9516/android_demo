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

/**
 * The main screen for the "My Device" feature area.
 * It manages the display of the device list/summary and the product filter sheet.
 *
 * @param modifier The modifier to be applied to the screen.
 * @param myDeviceViewModel ViewModel for managing the UI state of this screen.
 * @param scanViewModel ViewModel for handling Bluetooth operations.
 */
@Composable
fun MyDeviceScreen(
    modifier: Modifier = Modifier,
    myDeviceViewModel: MyDeviceViewModel = viewModel(),
    scanViewModel: ScanViewModel = viewModel(
        factory = ScanViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by myDeviceViewModel.uiState.collectAsState()

    // Animate background and content colors based on whether the filter sheet is shown.
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
                        endY = 800f // Control the gradient height
                    )
                )
                .statusBarsPadding()
        ) {

            MyDeviceHeader(
                contentColor = animatedUiColors.contentColor,
                showFilterSheet = uiState.showFilterSheet,
                onFilterClick = { myDeviceViewModel.onEvent(MyDeviceUiEvent.ToggleFilterSheet) },
                totalSelectedCount = uiState.totalSelectedCount
            )

            Spacer(Modifier.height(10.dp))

            MyDeviceContent(
                uiState = uiState,
                onEvent = myDeviceViewModel::onEvent,
                scanViewModel = scanViewModel
            )
        }

        // Animated overlay for the Bluetooth scan screen.
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

/**
 * A data class to hold animated color values for the UI.
 */
private data class AnimatedUiColors(
    val backgroundStartColor: Color,
    val backgroundEndColor: Color,
    val contentColor: Color
)

/**
 * A composable function that provides animated colors based on the filter sheet visibility.
 * @param showFilterSheet Whether the filter sheet is currently visible.
 * @return An [AnimatedUiColors] instance with smoothly transitioning colors.
 */
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
    scanViewModel: ScanViewModel
) {
    val permissionsGranted by scanViewModel.permissionsGranted.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            scanViewModel.setPermissionsGranted(true)
            scanViewModel.startScan() // Start scan immediately after getting permissions
            onEvent(MyDeviceUiEvent.ShowAddDeviceScreen)
        } else {
            // TODO: Show a message to the user that permissions are required.
        }
    }

    LaunchedEffect(Unit) {
        scanViewModel.checkPermissions()
    }

    // This defines the transition between the device list and the filter sheet.
    AnimatedContent(
        targetState = uiState.showFilterSheet,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            if (targetState) { // Entering filter sheet
                slideInVertically(animationSpec = tween(ANIMATION_DURATION)) { -it } togetherWith
                        fadeOut(animationSpec = tween(ANIMATION_DURATION))
            } else { // Exiting filter sheet
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
                        scanViewModel.startScan()
                        onEvent(MyDeviceUiEvent.ShowAddDeviceScreen)
                    } else {
                        permissionLauncher.launch(REQUIRED_PERMISSIONS)
                    }
                },
                onScanQrClick = { /* TODO: Implement QR scan logic */ }
            )
        }
    }
}

/**
 * Renders the product filter sheet with a semi-transparent background overlay.
 */
@Composable
private fun ProductFilterSheetWithOverlay(
    uiState: MyDeviceUiState,
    onEvent: (MyDeviceUiEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
            // Consume clicks on the overlay to dismiss the sheet
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
                .clickable(enabled = false) {} // Prevent clicks on the sheet from passing through to the overlay
        )
    }
}

@Composable
private fun DeviceListSection(onAddDeviceClick: () -> Unit, onScanQrClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        DeviceSummaryCard(
            modifier = Modifier.align(Alignment.TopCenter),
            onAddDeviceClick = onAddDeviceClick,
            onScanQrClick = onScanQrClick
        )
    }
}