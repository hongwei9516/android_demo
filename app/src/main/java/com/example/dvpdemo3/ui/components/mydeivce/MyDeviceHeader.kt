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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dvpdemo3.R
import com.example.dvpdemo3.ui.theme.DvpDemo3Theme

/**
 * “我的设备”界面的头部，包含标题和筛选按钮。
 *
 * @param animatedContentColor 动画内容颜色，用于标题和图标。
 * @param showFilterSheet 指示筛选弹窗是否可见。
 * @param onFilterClick 点击筛选按钮时的回调。
 * @param totalSelectedCount 已选择的设备总数，用于显示。
 */
@Composable
fun MyDeviceHeader(
    animatedContentColor: Color,
    showFilterSheet: Boolean,
    onFilterClick: () -> Unit,
    totalSelectedCount: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 标题
            Text(
                text = "我的设备",
                color = animatedContentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "添加设备",
                modifier = Modifier.size(28.dp),
                tint = animatedContentColor
            )

        }

        // 筛选按钮
        Row(
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, bottom = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onFilterClick)
                .padding(vertical = 2.dp, horizontal = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "产品",
                fontSize = 16.sp,
                color = animatedContentColor
            )
            if (totalSelectedCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            color = if (showFilterSheet) MaterialTheme.colorScheme.primary else Color.White
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$totalSelectedCount",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = if(showFilterSheet) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            val iconId =
                if (showFilterSheet) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        color = if (showFilterSheet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconId,
                    contentDescription = "产品筛选",
//                    tint = animatedContentColor,
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview(name = "MyDeviceHeaderPreview")
@Composable
fun MyDeviceHeaderPreview() {
    DvpDemo3Theme {
        MyDeviceHeader(
            animatedContentColor = Color.White,
            showFilterSheet = false,
            onFilterClick = {},
            totalSelectedCount = 0
        )
    }
}

@Preview(name = "MyDeviceHeaderWithCountPreview")
@Composable
fun MyDeviceHeaderWithCountPreview() {
    DvpDemo3Theme {
        MyDeviceHeader(
            animatedContentColor = Color.White,
            showFilterSheet = false,
            onFilterClick = {},
            totalSelectedCount = 3
        )
    }
}

@Preview(name = "MyDeviceHeaderWithFilterPreview")
@Composable
fun MyDeviceHeaderWithFilterPreview() {
    DvpDemo3Theme {
        MyDeviceHeader(
            animatedContentColor = Color.Black,
            showFilterSheet = true,
            onFilterClick = {},
            totalSelectedCount = 3
        )
    }
}