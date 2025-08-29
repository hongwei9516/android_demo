package com.example.dvpdemo3.ui.components.mydeivce

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dvpdemo3.R
import com.example.dvpdemo3.ui.theme.DvpDemo3Theme

@Composable
fun DeviceSummaryCard(modifier: Modifier = Modifier, onAddDeviceClick: () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(18.dp))
            Icon(
                painter = painterResource(R.drawable.ic_monitor),
                contentDescription = "No devices",
                modifier = Modifier.size(40.dp),
                tint = Color.Gray
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "暂无设备",
                color = Color.DarkGray,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(15.dp))

            val splitLineColor = colorResource(R.color.button_gray)

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(1.dp)
                    .background(splitLineColor)
            )

            // 底部按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(
                    text = "添加设备",
                    onClick = onAddDeviceClick
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(18.dp)
                        .background(splitLineColor)
                )
                ActionButton(
                    text = "扫一扫",
                    onClick = { /* TODO: Implement Scan Action */ }
                )
            }
        }
    }
}

@Composable
private fun ActionButton(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Preview
@Composable
fun DeviceSummaryCardPreview() {
    DvpDemo3Theme {
        DeviceSummaryCard(onAddDeviceClick = {})
    }
}