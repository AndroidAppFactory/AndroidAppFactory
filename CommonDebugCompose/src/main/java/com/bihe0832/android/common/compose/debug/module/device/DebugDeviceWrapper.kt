package com.bihe0832.android.common.compose.debug.module.device

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Debug
import android.text.format.Formatter
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.utils.os.ManufacturerUtil

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/31.
 * Description: Description
 *
 */

fun getSimpleDeviceInfo(context: Context): String {
    val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val outInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(outInfo)
    val info: Debug.MemoryInfo = Debug.MemoryInfo()
    Debug.getMemoryInfo(info)
    StringBuffer().apply {
        append(
            "系统内存：${Formatter.formatFileSize(context, outInfo.availMem)}/ ${
                Formatter.formatFileSize(
                    context,
                    outInfo.totalMem,
                )
            }",
        ).append("<BR>")
        append(
            "系统触发GC时内存临界值：${
                Formatter.formatFileSize(
                    context,
                    outInfo.threshold,
                )
            }",
        ).append("系统是否处于低内存运行：${outInfo.lowMemory}").append("<BR>")
        append(
            "当前应用占用内存：${
                Formatter.formatFileSize(
                    context,
                    (info.totalPss * 1024).toLong()
                )
            }"
        )
    }.let {
        return it.toString()
    }
}

@SuppressLint("MissingPermission")
fun getDeviceName(context: Context): String {
    try {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothManager?.adapter?.name ?: ""
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}

fun getMobileInfo(context: Context): List<String> {
    return mutableListOf<String>().apply {
        add("应用包名: ${context.packageName}")
        add("设备ID: ${ZixieContext.deviceId}")
        add("设备名称: ${getDeviceName(context)}")
        add("厂商型号: ${ManufacturerUtil.MANUFACTURER}, ${ManufacturerUtil.MODEL}, ${ManufacturerUtil.BRAND}")
        val sdkVersion =
            "系统版本: Android ${BuildUtils.RELEASE}, API  ${BuildUtils.SDK_INT}" + if (ManufacturerUtil.isHarmonyOs()) {
                ", Harmony(${ManufacturerUtil.getHarmonyVersion()})"
            } else {
                ""
            }
        add("<font color ='#3AC8EF'>$sdkVersion</font>")

        add("系统指纹: ${ManufacturerUtil.FINGERPRINT}")
    }
}
