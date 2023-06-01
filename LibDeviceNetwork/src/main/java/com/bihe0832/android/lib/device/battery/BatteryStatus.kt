package com.bihe0832.android.lib.device.battery

import android.os.BatteryManager
import com.bihe0832.android.lib.utils.MathUtils

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/3/30.
 * Description: Description
 *
 */
data class BatteryStatus(
        // 当前是否在充电
        val isCharging: Boolean,
        // 当前充电类型
        val plugged: Int,
        // 当前电量的相对值
        val currentValue: Int,
        // 电量的基准值
        val scale: Int) {
    override fun toString(): String {
        return "BatteryStatus(isCharging=$isCharging, plugged=$plugged, batteryPct=$currentValue)"
    }

    fun getBatteryPercentDesc(): String {
        return MathUtils.getFormatPercentDesc(getBatteryPercent())
    }

    fun getBatteryPercent(): Float {
        return MathUtils.getFormatPercent(currentValue, scale, 4)
    }

    fun getChargeTypeDesc(): String {
        return when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "WIRELESS"
            else -> "UNKNOWN"
        }
    }

}