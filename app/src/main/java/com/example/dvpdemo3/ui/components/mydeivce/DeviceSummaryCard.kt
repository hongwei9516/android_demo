package com.example.dvpdemo3.ui.components.mydeivce

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dvpdemo3.R
import com.example.dvpdemo3.data.ConnectedDevice
import com.example.dvpdemo3.data.DeviceType
import com.example.dvpdemo3.ui.theme.DvpDemo3Theme
import com.example.dvpdemo3.ui.viewmodel.MyDeviceViewModel
import com.example.dvpdemo3.ui.viewmodel.ScanResult
import com.example.dvpdemo3.ui.viewmodel.ScanViewModel
import com.example.dvpdemo3.ui.viewmodel.ScanViewModelFactory
import com.juul.kable.State

private val CardShape = RoundedCornerShape(20.dp)

@Composable
fun DeviceSummaryCard(
    modifier: Modifier = Modifier,
    onAddDeviceClick: () -> Unit,
    onScanQrClick: () -> Unit,
    scanViewModel: ScanViewModel = viewModel(
        factory = ScanViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    myDeviceViewModel: MyDeviceViewModel
) {
    val historyDevices by scanViewModel.historyDevices.collectAsState()
    val deviceConnectionStates by scanViewModel.deviceConnectionStates.collectAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                if (historyDevices.isEmpty()) {

                    Spacer(modifier = Modifier.padding(top = 14.dp))

                    Icon(
                        painter = painterResource(R.drawable.ic_monitor),
                        contentDescription = "No devices icon",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "暂无设备",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth(0.92f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(historyDevices, key = { it.address }) { device ->
                            val connectionState = deviceConnectionStates[device.address]
                            val isConnecting = connectionState is State.Connecting
                            val isConnected = connectionState is State.Connected
                            HistoryDeviceItem(
                                device = device,
                                imageRes = myDeviceViewModel.getIconForDeviceType(device.deviceType),
                                isConnecting = isConnecting,
                                isConnected = isConnected,
                                onConnectClick = { targetDevice ->
                                    if (isConnected) {
                                        val scanResult = ScanResult(
                                            deviceName = device.deviceName,
                                            deviceType = device.deviceType,
                                            address = device.address,
                                            rssi = 0
                                        )
                                        scanViewModel.disconnectDevice(scanResult)
                                    } else {
                                        scanViewModel.connectToDevice(targetDevice)
                                    }
                                },
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(0.9f),
                thickness = DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ActionButton(
                    text = "添加设备",
                    onClick = onAddDeviceClick
                )
                VerticalDivider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp),
                    thickness = DividerDefaults.Thickness,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                ActionButton(
                    text = "扫一扫",
                    onClick = onScanQrClick
                )
            }
        }
    }
}

@Composable
fun ActionButton(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun HistoryDeviceItem(
    device: ConnectedDevice,
    imageRes: Int?,
    isConnecting: Boolean,
    isConnected: Boolean,
    onConnectClick: (ConnectedDevice) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.weight(1f)
            ) {
                imageRes?.let {
                    Image(
                        painter = painterResource(it),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                ) {
                    Text(
                        text = device.deviceName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = device.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = { onConnectClick(device) },
                enabled = !isConnecting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isConnected -> MaterialTheme.colorScheme.secondary
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

@Preview(name = "HistoryDeviceItem Preview")
@Composable
private fun HistoryDeviceItemPreview() {
    DvpDemo3Theme {
        HistoryDeviceItem(
            device = ConnectedDevice(
                address = "00:1B:44:11:3A:B7",
                deviceName = "CPT1-ABCD",
                deviceType = DeviceType.CPT1
            ),
            imageRes = R.drawable.ic_icon_cpt1,
            isConnecting = false,
            isConnected = false,
            onConnectClick = {}
        )
    }
}

