package com.example.dvpdemo3.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dvpdemo3.R
import com.example.dvpdemo3.ui.theme.DvpDemo3Theme

@Composable
fun BluetoothScanScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
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

        // 2. 上半部分扫描动画
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "添加设备",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
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
                .background(colorResource(R.color.button_gray)),
        )

        // 3. 下半部分留空
        Spacer(modifier = Modifier.weight(1.5f))
    }
}

@Composable
private fun BluetoothScanningAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning_animation")
    val primaryColor = MaterialTheme.colorScheme.primary

    // 我们创建三个动画，每个都有不同的延迟，以产生波纹效果
    val animationValues = (1..5).map { index ->
        val scale by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset((index - 1) * 200) // 每个波纹延迟 500ms
            ), label = "scale_$index"
        )
        val alpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset((index - 1) * 200)
            ), label = "alpha_$index"
        )
        scale to alpha
    }

    Box(contentAlignment = Alignment.Center) {


        Canvas(modifier = Modifier.size(300.dp)) {

            drawCircle(
                color = primaryColor, // 蓝色背景
                radius = 30.dp.toPx(), // 圆的半径，比图标大一点
                center = center
            )

            val canvasWidth = size.width
//            val canvasHeight = size.height

            animationValues.forEach { (scale, alpha) ->
                drawCircle(
                    color = primaryColor.copy(alpha = alpha),
                    radius = (canvasWidth / 2) * scale,
                    center = center,
                    style = Stroke(width = 2.dp.toPx() * (1 - scale))
                )
            }
        }

        // 中间的蓝牙图标
        Icon(
            painter = painterResource(id = R.drawable.ic_bluetooth_searching), // 请确保你有名为 ic_bluetooth 的图标资源
            contentDescription = "蓝牙扫描",
            tint = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .size(35.dp)
        )

        Text(
            text = "正在扫描设备...",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.BottomCenter) // 1. 对齐到 Box 的底部中心
                .padding(bottom = 40.dp)      // 2. 从底部向上留出一些间距
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BluetoothScanScreenPreview() {
    DvpDemo3Theme {
        BluetoothScanScreen(onBackClick = {})
    }
}