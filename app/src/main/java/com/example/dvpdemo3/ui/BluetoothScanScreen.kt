package com.example.dvpdemo3.ui

import android.app.Application
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dvpdemo3.R
import com.example.dvpdemo3.ui.theme.DvpDemo3Theme
import com.example.dvpdemo3.ui.viewmodel.ScanResult
import com.example.dvpdemo3.ui.viewmodel.ScanViewModel
import com.example.dvpdemo3.ui.viewmodel.ScanViewModelFactory
import com.juul.kable.State
import android.util.Log
import androidx.compose.foundation.layout.PaddingValues

private const val TAG = "BluetoothScanScreen"

/**
 * The main screen for scanning and displaying Bluetooth devices.
 *
 * @param onBackClick Callback invoked when the back button is pressed.
 * @param scanViewModel The ViewModel managing the scanning logic.
 */
@Composable
fun BluetoothScanScreen(
    onBackClick: () -> Unit,
    scanViewModel: ScanViewModel = viewModel(
        factory = ScanViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by scanViewModel.uiState.collectAsState()
    val deviceConnectionStates by scanViewModel.deviceConnectionStates.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    // Handles the system back button press to stop the scan before navigating back.
//    BackHandler(enabled = true) {
//        Log.d(TAG, "System back button pressed. Stopping scan.")
//        scanViewModel.stopScan()
//        onBackClick()
//    }

    // Effect to show error messages in a Snackbar.
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackBarHostState.showSnackbar(message)
            scanViewModel.clearErrorMessage()
        }
    }

    // Effect to show info messages in a Snackbar.
    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let { message ->
            snackBarHostState.showSnackbar(message)
            scanViewModel.clearInfoMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(onBackClick = {
            Log.d(TAG, "UI back button pressed. Stopping scan.")
            scanViewModel.stopScan()
            onBackClick()
        })

        // Scanning animation section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "添加设备",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(horizontal = 32.dp, vertical = 20.dp)
            )
            BluetoothScanningAnimation()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )

        // Discovered devices list
        LazyColumn(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(uiState.discoveredDevices, key = { it.address }) { device ->
                val connectionState = deviceConnectionStates[device.address]
                val isConnected = connectionState is State.Connected
                val isConnecting = connectionState is State.Connecting

                DeviceRow(
                    device = device,
                    onButtonClick = { targetDevice ->
                        if (isConnected) {
                            scanViewModel.disconnectDevice(targetDevice)
                        } else {
                            scanViewModel.connectToDevice(targetDevice)
                        }
                    },
                    isConnecting = isConnecting,
                    isConnected = isConnected
                )
            }
        }
    }
}

/**
 * Header for the screen, containing the back button.
 */
@Composable
private fun ScreenHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

/**
 * A composable that displays a pulsating scanning animation.
 */
@Composable
private fun BluetoothScanningAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning_animation")
    val primaryColor = MaterialTheme.colorScheme.primary

    // Create 5 ripple animations with staggered start times.
    val animationValues = List(5) { index ->
        val startDelay = (index * 200).toLong()
        val scale by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(startDelay.toInt())
            ), label = "scale_$index"
        )
        val alpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(startDelay.toInt())
            ), label = "alpha_$index"
        )
        scale to alpha
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            // Draw ripples
            animationValues.forEach { (scale, alpha) ->
                drawCircle(
                    color = primaryColor.copy(alpha = alpha),
                    radius = (canvasWidth / 2) * scale,
                    style = Stroke(width = 2.dp.toPx() * (1 - scale))
                )
            }
            // Draw central solid circle
            drawCircle(
                color = primaryColor,
                radius = 30.dp.toPx()
            )
        }

        // Bluetooth icon in the center
        Icon(
            painter = painterResource(id = R.drawable.ic_bluetooth_searching),
            contentDescription = "蓝牙扫描",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(35.dp)
        )

        Text(
            text = "正在扫描设备...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
    }
}


/**
 * A row composable to display information about a single discovered device.
 */
@Composable
fun DeviceRow(
    device: ScanResult,
    onButtonClick: (ScanResult) -> Unit,
    isConnecting: Boolean,
    isConnected: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = device.deviceName,
                    color = MaterialTheme.colorScheme.primary, // Using theme color
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = getSignalIconForRssi(device.rssi)),
                        contentDescription = "信号强度",
                        tint = Color.Unspecified, // Icon has its own colors
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${device.rssi} dBm",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Button(
                onClick = { onButtonClick(device) },
                enabled = !isConnecting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isConnected -> MaterialTheme.colorScheme.primary
                        isConnecting -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            ) {
                Text(
                    text = when {
                        isConnected -> "断开"
                        isConnecting -> "连接中..."
                        else -> "连接"
                    }
                )
            }
        }
    }
}

/**
 * Returns a drawable resource ID for the corresponding RSSI signal strength.
 */
@DrawableRes
fun getSignalIconForRssi(rssi: Int): Int {
    return when {
        rssi > -30 -> R.drawable.ic_rssi_5
        rssi > -45 -> R.drawable.ic_rssi_4
        rssi > -65 -> R.drawable.ic_rssi_3
        rssi > -80 -> R.drawable.ic_rssi_2
        rssi > -95 -> R.drawable.ic_rssi_1
        else -> R.drawable.ic_rssi_0
    }
}

@Preview(showBackground = true)
@Composable
fun BluetoothScanScreenPreview() {
    DvpDemo3Theme {
        BluetoothScanScreen(onBackClick = {})
    }
}