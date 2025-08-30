package com.example.dvpdemo3.data

import androidx.annotation.DrawableRes
import com.google.gson.annotations.SerializedName

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
