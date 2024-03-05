package com.bihe0832.android.lib.device.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/3/30.
 * Description: Description
 *
 */

object BatteryHelper {

    fun startReceiveBatteryChanged(context: Context?, statusChangeReceiver: BroadcastReceiver) {
        val powerConnectedFilter = IntentFilter(Intent.ACTION_POWER_CONNECTED)
        powerConnectedFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        powerConnectedFilter.addAction(Intent.ACTION_BATTERY_LOW)
        powerConnectedFilter.addAction(Intent.ACTION_BATTERY_OKAY)
        context?.registerReceiver(statusChangeReceiver, powerConnectedFilter)
    }

    fun stopReceiveBatteryChanged(context: Context?, statusChangeReceiver: BroadcastReceiver) {
        context?.unregisterReceiver(statusChangeReceiver)
    }

    fun getBatteryStatus(context: Context?): BatteryStatus? {
        context?.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.let { batteryStatus ->
            // Are we charging / charged?
            val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

            // How are we charging?
            val chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

            // Get the current battery percentage
            val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            return BatteryStatus(isCharging, chargePlug, level, scale)
        }
        return null
    }

    /**
     * 获取电池温度
     *
     * @param mContext the m context
     * @return the battery temperature
     */
    fun getBatteryTemperature(context: Context?): Float {
        var temperature = -1f
        if (context == null) {
            return temperature
        }
        try {
            context.registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )?.let { batteryIntent ->
                batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0).let { temp ->
                    temperature = if (temp == 0) {
                        return temperature
                    } else {
                        (temp / 10).toFloat()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return temperature
    }
}