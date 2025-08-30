package com.example.dvpdemo3.ui.components.mydeivce

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dvpdemo3.R
import com.example.dvpdemo3.data.Device
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.tooling.preview.Preview
import com.example.dvpdemo3.ui.theme.DvpDemo3Theme
import com.example.dvpdemo3.ui.viewmodel.initialCategorizedDevices

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ProductFilterSheet(
    categorizedDevices: Map<String, List<Device>>,
    selectedDevicesMap: Map<String, List<Device>>,
    onConfirm: () -> Unit,
    onReset: () -> Unit,
    onToggleSelected: (String, Device, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedItemIndex by remember { mutableIntStateOf(0) }
    val filterCategories = remember { categorizedDevices.keys.toList() }

    val currentCategory by remember(selectedItemIndex) {
        derivedStateOf { filterCategories.getOrNull(selectedItemIndex) ?: "" }
    }
    val currentDeviceList by remember(currentCategory) {
        derivedStateOf { categorizedDevices[currentCategory] ?: emptyList() }
    }
    val currentSelectedDevices by remember(currentCategory, selectedDevicesMap) {
        derivedStateOf { selectedDevicesMap[currentCategory] ?: emptyList() }
    }

    val totalSelectedCount by remember(selectedDevicesMap) {
        derivedStateOf { selectedDevicesMap.values.sumOf { it.size } }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.58f)
            .clip(
                RoundedCornerShape(
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                )
            )
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                filterCategories.forEachIndexed { index, text ->
                    FilterItem(
                        text = text,
                        isSelected = selectedItemIndex == index,
                        count = selectedDevicesMap.getValue(text).size,
                        onClick = { selectedItemIndex = index }
                    )
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(horizontal = 6.dp)
            ) {
                val itemHeight = (maxHeight - (6.dp * 2)) / 3
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(currentDeviceList) { device ->
                        ProductGridItem(
                            device = device,
                            itemHeight = itemHeight,
                            isSelected = currentSelectedDevices.contains(device),
                            onToggleSelected = { isSelected ->
                                onToggleSelected(currentCategory, device, isSelected)
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onReset,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
            ) {
                Text("重置", color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("确定 ($totalSelectedCount)", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun FilterItem(
    text: String,
    isSelected: Boolean = false,
    count: Int = 0,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                color = if (isSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                color = colorResource(R.color.text_gray),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        if (count > 0) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(end = 1.dp)
                )
            }
        }
    }
}


@Composable
private fun ProductGridItem(
    device: Device,
    itemHeight: Dp,
    isSelected: Boolean,
    onToggleSelected: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight)
            .clip(RoundedCornerShape(4.dp))
            .background(color = if (!isSelected) Color.White else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable {
                onToggleSelected(!isSelected)
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Image(
                painter = painterResource(id = device.imageRes),
                contentDescription = device.content,
                modifier = Modifier.size(60.dp)
            )
            Text(
                text = device.content,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                color = colorResource(R.color.text_gray),
                textAlign = TextAlign.Center,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(name = "ProductFilterSheetPreview", showBackground = true)
@Composable
fun ProductFilterSheetPreview() {
    DvpDemo3Theme {
        ProductFilterSheet(
            categorizedDevices = initialCategorizedDevices,
            selectedDevicesMap = initialCategorizedDevices.keys.associateWith { emptyList() },
            onConfirm = {},
            onReset = {},
            onToggleSelected = { _, _, _ -> }
        )
    }
}

@Preview(name = "ProductFilterSheetWithSelectionPreview", showBackground = true)
@Composable
fun ProductFilterSheetWithSelectionPreview() {
    DvpDemo3Theme {

        val selectedDevices = mapOf(
            "压力" to listOf(initialCategorizedDevices["压力"]!!.first()),
            "温度" to listOf(initialCategorizedDevices["温度"]!!.first())
        )
        ProductFilterSheet(
            categorizedDevices = initialCategorizedDevices,
            selectedDevicesMap = selectedDevices,
            onConfirm = {},
            onReset = {},
            onToggleSelected = { _, _, _ -> }
        )
    }
}