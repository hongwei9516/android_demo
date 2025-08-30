package com.example.dvpdemo3.ui.components.mydeivce

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dvpdemo3.ui.theme.DvpDemo3Theme

@Composable
fun MyDeviceHeader(
    contentColor: Color,
    showFilterSheet: Boolean,
    onFilterClick: () -> Unit,
    onClearHistoryClick: () -> Unit,
    totalSelectedCount: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "我的设备",
                color = contentColor,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "添加设备",
                modifier = Modifier.size(28.dp),
                tint = contentColor
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onFilterClick)
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "产品",
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor
                )
                if (totalSelectedCount > 0) {
                    CountBadge(
                        count = totalSelectedCount,
                        isFilterVisible = showFilterSheet
                    )
                }
                ArrowIcon(isFilterVisible = showFilterSheet)
            }

            if (!showFilterSheet) {
                ClearButton(
                    text = "清空历史",
                    onClearHistoryClick
                )
            }
        }
    }
}

@Composable
fun ClearButton(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onPrimary,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    )
}


@Composable
private fun CountBadge(count: Int, isFilterVisible: Boolean) {
    val backgroundColor =
        if (isFilterVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
    val textColor =
        if (isFilterVisible) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(color = backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
        )
    }
}

@Composable
private fun ArrowIcon(isFilterVisible: Boolean) {
    val icon =
        if (isFilterVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(color = MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "产品筛选",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Preview(name = "MyDeviceHeaderPreview")
@Composable
fun MyDeviceHeaderPreview() {
    DvpDemo3Theme {
        MyDeviceHeader(
            contentColor = Color.White,
            showFilterSheet = false,
            onFilterClick = {},
            onClearHistoryClick = {},
            totalSelectedCount = 0
        )
    }
}

@Preview(name = "MyDeviceHeaderWithCountPreview")
@Composable
fun MyDeviceHeaderWithCountPreview() {
    DvpDemo3Theme {
        MyDeviceHeader(
            contentColor = Color.White,
            showFilterSheet = false,
            onFilterClick = {},
            onClearHistoryClick = {},
            totalSelectedCount = 3
        )
    }
}

@Preview(name = "MyDeviceHeaderWithFilterPreview")
@Composable
fun MyDeviceHeaderWithFilterPreview() {
    DvpDemo3Theme {
        MyDeviceHeader(
            contentColor = Color.Black,
            showFilterSheet = true,
            onFilterClick = {},
            onClearHistoryClick = {},
            totalSelectedCount = 3
        )
    }
}
