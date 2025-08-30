package com.example.dvpdemo3.ui.components.mydeivce

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dvpdemo3.R
import com.example.dvpdemo3.ui.theme.DvpDemo3Theme

private val CardShape = RoundedCornerShape(20.dp)

/**
 * A card displayed on the main screen when no devices have been added.
 * It shows a message and provides actions to "Add Device" or "Scan QR".
 *
 * @param modifier The modifier to be applied to the card.
 * @param onAddDeviceClick Callback invoked when the "Add Device" button is clicked.
 */
@Composable
fun DeviceSummaryCard(
    modifier: Modifier = Modifier,
    onAddDeviceClick: () -> Unit,
    onScanQrClick: () -> Unit // Added callback for scan action
) {
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
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // "No devices" section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
            }

            // Divider
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(0.9f),
                thickness = DividerDefaults.Thickness, color = MaterialTheme.colorScheme.outlineVariant
            )

            // Bottom action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(
                    text = "添加设备",
                    onClick = onAddDeviceClick
                )
                VerticalDivider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp),
                    thickness = DividerDefaults.Thickness, color = MaterialTheme.colorScheme.outlineVariant
                )
                ActionButton(
                    text = "扫一扫",
                    onClick = onScanQrClick
                )
            }
        }
    }
}

/**
 * A text-based button for actions within the [DeviceSummaryCard].
 */
@Composable
private fun ActionButton(text: String, onClick: () -> Unit) {
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

@Preview
@Composable
private fun DeviceSummaryCardPreview() {
    DvpDemo3Theme {
        DeviceSummaryCard(
            onAddDeviceClick = {},
            onScanQrClick = {}
        )
    }
}