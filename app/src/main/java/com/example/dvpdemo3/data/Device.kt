package com.example.dvpdemo3.data

import androidx.annotation.DrawableRes

/**
 * 设备数据模型。
 *
 * @param content 设备名称。
 * @param imageRes 设备图标的资源ID。
 */
data class Device(
    val content: String,
    val type: String,
    @DrawableRes val imageRes: Int
)

/**
 * 假数据：用于预览和测试的设备列表。
 */
val dummyDevices = listOf(
    Device("手持高精度测温仪", "压力",com.example.dvpdemo3.R.drawable.ic_icon_mp1),
    Device("精密铂电阻温湿度计", "温度", com.example.dvpdemo3.R.drawable.ic_icon_cpt1),
    Device("实时无线记录器", "过程", com.example.dvpdemo3.R.drawable.ic_icon_zigbee),

)