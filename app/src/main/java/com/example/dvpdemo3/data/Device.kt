package com.example.dvpdemo3.data

import androidx.annotation.DrawableRes
import com.google.gson.annotations.SerializedName

// 定义支持的设备类型
enum class DeviceType {

    @SerializedName("WT300")
    WT300,

    @SerializedName("CPT1")
    CPT1,

    @SerializedName("CPH1")
    CPH1,

    @SerializedName("WTL-2")
    WTL_2,

    @SerializedName("WPL-2")
    WPL_2,

    @SerializedName("WHL-LM2")
    WHL_LM2,

    @SerializedName("UNKNOW")
    UNKNOW,

}


/**
 * 设备数据模型。
 *
 * @param content 设备名称。
 * @param category 设备类别
 * @param imageRes 设备图标的资源ID。
 */
data class Device(
    val content: String,
    val category: String,
    val type: List<DeviceType>,
    @DrawableRes val imageRes: Int
)


data class ConnectedDevice(
    val address: String,
    val deviceName: String,
    val deviceType: DeviceType
)
