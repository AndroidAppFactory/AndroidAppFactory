package com.bihe0832.android.common.debug.module

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Process
import android.text.format.Formatter
import android.view.View
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.item.DebugTipsData
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.debug.icon.DebugLogTips
import com.bihe0832.android.lib.network.DeviceInfoManager
import com.bihe0832.android.lib.network.MobileUtil
import com.bihe0832.android.lib.network.NetworkUtil
import com.bihe0832.android.lib.network.WifiManagerWrapper
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.utils.os.DisplayUtil
import com.bihe0832.android.lib.utils.os.ManufacturerUtil
import com.bihe0832.android.lib.utils.time.DateUtil

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2023/3/20.
 * Description: Description
 *
 */
class DebugDeviceFragment : DebugEnvFragment() {

    val TASK = "DebugDeviceFragment"
    override fun initView(view: View) {
        super.initView(view)
        showResult("点击信息内容可以复制和分享")
    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugTipsData("常用工具"))
            add(DebugItemData("开启悬浮常驻内存展示", { startShowSimpleInfo() }))
            add(DebugItemData("关闭悬浮常驻内存展示", { stopAutoShowSimpleInfo() }))
            add(DebugTipsData("设备信息"))
            add(getInfoItem("Android ID： ${ZixieContext.deviceId}"))
            add(getInfoItem("Build ID： ${Build.ID}"))
            add(getInfoItem("显示ID： ${Build.DISPLAY}"))

            add(DebugTipsData("Android 系统信息"))
            add(getInfoItem("系统版本： Android ${BuildUtils.RELEASE}"))
            add(getInfoItem("系统 API：  ${BuildUtils.SDK_INT}"))
            add(getInfoItem("系统指纹： ${ManufacturerUtil.FINGERPRINT}"))
            add(getInfoItem("系统安全patch 时间：  ${Build.VERSION.SECURITY_PATCH}"))
            add(getInfoItem("系统发布时间：  ${DateUtil.getDateEN(Build.TIME)}"))
            add(getInfoItem("系统版本类型：  ${Build.TYPE}"))
            add(getInfoItem("系统CodeName： ${Build.VERSION.CODENAME}"))
            if (ManufacturerUtil.isHarmonyOs()) {
                add(getInfoItem("特殊系统：Harmony  ${ManufacturerUtil.getHarmonyVersion()}"))
            }

            val activityManager = context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val outInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(outInfo)
            add(DebugTipsData("应用内存配置"))
            add(getInfoItem("单应用堆内存的初始大小：${ManufacturerUtil.getValueByKey("dalvik.vm.heapstartsize", { "" })}"))
            add(getInfoItem("单应用(标准应用)最大堆内存大小：${ManufacturerUtil.getValueByKey("dalvik.vm.heapgrowthlimit", { "" })}"))
            add(getInfoItem("单应用(largeHeap应用)最大堆内存大小：${ManufacturerUtil.getValueByKey("dalvik.vm.heapsize", { "" })}"))
            add(getInfoItem("Dalvik 虚拟机最大内存：${Formatter.formatFileSize(context, Runtime.getRuntime().maxMemory())}"))
            add(getInfoItem("系统内存大小：${Formatter.formatFileSize(context, outInfo.totalMem)}"))
            add(getInfoItem("系统触发GC时内存临界值：${Formatter.formatFileSize(context, outInfo.threshold)}"))

            add(DebugTipsData("当前运行信息"))
            add(getInfoItem("系统剩余内存：${Formatter.formatFileSize(context, outInfo.availMem)}, 是否低内存运行：${outInfo.lowMemory}"))
            // 通过这种方法可以传入当前进程的 pid 获取到当前进程的总内存占用情况，其中不仅包括了虚拟机的内存占用情况，还包括原生层和其它内存占用,AndroidQ 版本对这个 API 增加了限制，当采样率较高时，会一直返回一个相同的值。
            val memInfo = activityManager.getProcessMemoryInfo(intArrayOf(Process.myPid()))
            if (memInfo.isNotEmpty()) {
                add(getInfoItem("当前应用占用内存：${Formatter.formatFileSize(context, (memInfo[0].totalPss * 1024).toLong())}"))
                add(getInfoItem("&nbsp;&nbsp;&nbsp;&nbsp;其中栈内存：${Formatter.formatFileSize(context, (memInfo[0].dalvikPss * 1024).toLong())}"))
                add(getInfoItem("&nbsp;&nbsp;&nbsp;&nbsp;其中堆内存：${Formatter.formatFileSize(context, ((memInfo[0].totalPss - memInfo[0].dalvikPss - memInfo[0].otherPss) * 1024).toLong())}"))
                add(getInfoItem("&nbsp;&nbsp;&nbsp;&nbsp;其他内存：${Formatter.formatFileSize(context, (memInfo[0].otherPss * 1024).toLong())}"))
            }
            add(getInfoItem("Dalvik 单应用最大内存：${Formatter.formatFileSize(context, Runtime.getRuntime().totalMemory())}"))
            add(getInfoItem("&nbsp;&nbsp;&nbsp;&nbsp;当前应用栈内存已用量：${Formatter.formatFileSize(context, Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())}"))
            add(getInfoItem("&nbsp;&nbsp;&nbsp;&nbsp;当前应用栈内存可用量：${Formatter.formatFileSize(context, Runtime.getRuntime().freeMemory())}"))

            add(DebugTipsData("硬件信息"))
            add(getInfoItem("厂商、型号、品牌：${ManufacturerUtil.MANUFACTURER}, ${ManufacturerUtil.MODEL}, ${ManufacturerUtil.BRAND}"))
            add(getInfoItem("硬件名： ${Build.HARDWARE}"))
            add(getInfoItem("产品名：${Build.PRODUCT}"))
            add(getInfoItem("主板名：${Build.BOARD}"))
            add(getInfoItem("CPU 类型：${Build.CPU_ABI}"))
            add(getInfoItem("设备宽度：${DisplayUtil.getRealScreenSizeX(context)}"))
            add(getInfoItem("设备高度：${DisplayUtil.getRealScreenSizeY(context)}"))
            add(getInfoItem("状态栏高度：${DisplayUtil.getStatusBarHeight(context)}"))
            add(getInfoItem("虚拟按键高度：${DisplayUtil.getNavigationBarHeight(context)}"))

            add(DebugTipsData("网络信息"))
            add(getInfoItem("网络类型：${NetworkUtil.getNetworkName(context)}"))
            add(getInfoItem("是否联网：${NetworkUtil.isNetworkConnected(context)}"))
            add(getInfoItem("网络是否可用：${NetworkUtil.isNetworkOnline()}"))
            add(getInfoItem("Wi-Fi SSID：${WifiManagerWrapper.getSSID()}"))
            add(getInfoItem("Wi-Fi BSSID：${WifiManagerWrapper.getBSSID()}"))
            add(getInfoItem("Wi-Fi 强度：${WifiManagerWrapper.getSignalLevel()}"))
            add(getInfoItem("Wi-Fi IP：${NetworkUtil.getDtTypeInfo(context).wifiIp}"))
            add(getInfoItem("周边Wi-Fi数量：${WifiManagerWrapper.getScanResultList().size}"))
            add(getInfoItem("移动网络基站信息：${MobileUtil.getPhoneCellInfo(context)}"))
            add(getInfoItem("移动网络运营商：${DeviceInfoManager.getInstance().getMobileOperatorType()}"))
            add(getInfoItem("移动网络信号强度：${MobileUtil.getSignalLevel()}"))
            add(getInfoItem("移动网络 IP：${NetworkUtil.getDtTypeInfo(context).mobileIp}"))
        }
    }

    fun startShowSimpleInfo() {
        TaskManager.getInstance().addTask(object : BaseTask() {
            override fun getMyInterval(): Int {
                return 2 * 5
            }

            override fun getNextEarlyRunTime(): Int {
                return 0
            }

            override fun run() {
                try {
                    DebugLogTips.show(getSimplaInfo())
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }

            }

            override fun getTaskName(): String {
                return TASK
            }

        })
    }

    fun stopAutoShowSimpleInfo() {
        TaskManager.getInstance().removeTask(TASK)
    }

    fun getSimplaInfo(): String {
        ZixieContext.applicationContext?.let { context ->
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val outInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(outInfo)

            val memInfo = activityManager.getProcessMemoryInfo(intArrayOf(Process.myPid()))
            StringBuffer().apply {
                append("系统内存：${Formatter.formatFileSize(context, outInfo.availMem)}/ ${Formatter.formatFileSize(context, outInfo.totalMem)}").append("<BR>")
                append("系统触发GC时内存临界值：${Formatter.formatFileSize(context, outInfo.threshold)}").append("系统是否处于低内存运行：${outInfo.lowMemory}").append("<BR>")
                append("当前应用占用内存：${Formatter.formatFileSize(context, (memInfo[0].totalPss * 1024).toLong())}")
            }.let {
                return it.toString()
            }
        }
        return ""
    }
}

fun getMobileInfo(context: Context?): List<String> {
    return mutableListOf<String>().apply {
        add("应用包名: ${context?.packageName}")
        add("设备ID: ${ZixieContext.deviceId}")
        add("厂商型号: ${ManufacturerUtil.MANUFACTURER}, ${ManufacturerUtil.MODEL}, ${ManufacturerUtil.BRAND}")
        var sdkVersion = "系统版本: Android ${BuildUtils.RELEASE}, API  ${BuildUtils.SDK_INT}" + if (ManufacturerUtil.isHarmonyOs()) {
            ", Harmony(${ManufacturerUtil.getHarmonyVersion()})"
        } else {
            ""
        }
        add("<font color ='#3AC8EF'>$sdkVersion</font>")

        add("系统指纹: ${ManufacturerUtil.FINGERPRINT}")

    }
}