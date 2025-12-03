package com.bihe0832.android.common.debug.device

import android.app.ActivityManager
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Debug
import android.os.Process
import android.text.format.Formatter
import android.view.View
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.item.getTipsItem
import com.bihe0832.android.lib.utils.apk.AppStorageUtil
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.debug.icon.DebugLogTips
import com.bihe0832.android.lib.device.DeviceIDUtils
import com.bihe0832.android.lib.device.battery.BatteryHelper
import com.bihe0832.android.lib.device.cpu.CPUHelper
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.network.DeviceInfoManager
import com.bihe0832.android.lib.network.DtTypeInfo
import com.bihe0832.android.lib.network.MobileUtil
import com.bihe0832.android.lib.network.NetworkUtil
import com.bihe0832.android.lib.network.wifi.WifiChannelInfo
import com.bihe0832.android.lib.network.wifi.WifiManagerWrapper
import com.bihe0832.android.lib.network.wifi.WifiUtil
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.utils.os.DisplayUtil
import com.bihe0832.android.lib.utils.os.ManufacturerUtil
import com.bihe0832.android.lib.utils.time.DateUtil

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/3/20.
 * Description: Description
 *
 */
class DebugDeviceFragment : DebugEnvFragment() {

    val TASK = "DebugDeviceFragment"
    val statusChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            getDataLiveData().initData()
            ZixieContext.showToast(BatteryHelper.getBatteryStatus(context).toString())
        }
    }

    override fun initView(view: View) {
        super.initView(view)
        WifiManagerWrapper.init(view.context, debug = true, notifyRSSI = true, canScanWifi = true, canWifiConfiguration = false)
        showResult("点击信息内容可以复制和分享")
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean, hasCreateView: Boolean) {
        super.setUserVisibleHint(isVisibleToUser, hasCreateView)
        if (hasCreateView && isVisibleToUser){
            BatteryHelper.startReceiveBatteryChanged(context, statusChangeReceiver)
        }else{
            BatteryHelper.stopReceiveBatteryChanged(context, statusChangeReceiver)
        }
    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getTipsItem("常用工具"))
            add(getDebugItem("开启悬浮常驻内存展示", { startShowSimpleInfo() }))
            add(getDebugItem("关闭悬浮常驻内存展示", { stopAutoShowSimpleInfo() }))
            add(getTipsItem("设备信息"))
            add(getInfoItem("设备名： ${getDeivceName(context)}"))
            add(getInfoItem("Android ID： ${ZixieContext.deviceId}"))
            add(getInfoItem("Build ID： ${Build.ID}"))
            add(getInfoItem("显示ID： ${Build.DISPLAY}"))

            add(getTipsItem("Android 系统信息"))
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

            val activityManager =
                context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val outInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(outInfo)
            add(getTipsItem("应用内存配置"))
            add(
                getInfoItem(
                    "单应用堆内存的初始大小：${
                        ManufacturerUtil.getValueByKey(
                            "dalvik.vm.heapstartsize"
                        ) { "" }
                    }"
                )
            )
            add(
                getInfoItem(
                    "单应用(标准应用)最大堆内存大小：${
                        ManufacturerUtil.getValueByKey(
                            "dalvik.vm.heapgrowthlimit"
                        ) { "" }
                    }"
                )
            )
            add(
                getInfoItem(
                    "单应用(largeHeap应用)最大堆内存大小：${
                        ManufacturerUtil.getValueByKey(
                            "dalvik.vm.heapsize"
                        ) { "" }
                    }"
                )
            )
            add(
                getInfoItem(
                    "Dalvik 虚拟机最大内存：${
                        Formatter.formatFileSize(
                            context,
                            Runtime.getRuntime().maxMemory()
                        )
                    }"
                )
            )
            add(getInfoItem("系统内存大小：${Formatter.formatFileSize(context, outInfo.totalMem)}"))
            add(
                getInfoItem(
                    "系统触发GC时内存临界值：${
                        Formatter.formatFileSize(
                            context,
                            outInfo.threshold
                        )
                    }"
                )
            )

            add(getTipsItem("当前运行信息"))
            add(
                getInfoItem(
                    "系统剩余内存：${
                        Formatter.formatFileSize(
                            context,
                            outInfo.availMem
                        )
                    }, 是否低内存运行：${outInfo.lowMemory}"
                )
            )
            // 通过这种方法可以传入当前进程的 pid 获取到当前进程的总内存占用情况，其中不仅包括了虚拟机的内存占用情况，还包括原生层和其它内存占用,AndroidQ 版本对这个 API 增加了限制，当采样率较高时，会一直返回一个相同的值。
            val memInfo = activityManager.getProcessMemoryInfo(intArrayOf(Process.myPid()))
            if (memInfo.isNotEmpty()) {
                add(
                    getInfoItem(
                        "当前应用占用内存（精度高，限频：5min）：${
                            Formatter.formatFileSize(
                                context,
                                (memInfo[0].totalPss * 1024).toLong()
                            )
                        }"
                    )
                )
                add(
                    getInfoItem(
                        "&nbsp;&nbsp;&nbsp;&nbsp;其中栈内存：${
                            Formatter.formatFileSize(
                                context,
                                (memInfo[0].dalvikPss * 1024).toLong()
                            )
                        }"
                    )
                )
                add(
                    getInfoItem(
                        "&nbsp;&nbsp;&nbsp;&nbsp;其中堆内存：${
                            Formatter.formatFileSize(
                                context,
                                ((memInfo[0].totalPss - memInfo[0].dalvikPss - memInfo[0].otherPss) * 1024).toLong()
                            )
                        }"
                    )
                )
                add(
                    getInfoItem(
                        "&nbsp;&nbsp;&nbsp;&nbsp;其他内存：${
                            Formatter.formatFileSize(
                                context,
                                (memInfo[0].otherPss * 1024).toLong()
                            )
                        }"
                    )
                )
            }
            val info: Debug.MemoryInfo = Debug.MemoryInfo()
            Debug.getMemoryInfo(info)
            if (memInfo.isNotEmpty()) {
                add(
                    getInfoItem(
                        "当前应用占用内存（精度低，实时获取）：${
                            Formatter.formatFileSize(
                                context,
                                (info.totalPss * 1024).toLong()
                            )
                        }"
                    )
                )
                add(
                    getInfoItem(
                        "&nbsp;&nbsp;&nbsp;&nbsp;其中栈内存：${
                            Formatter.formatFileSize(
                                context,
                                (info.dalvikPss * 1024).toLong()
                            )
                        }"
                    )
                )
                add(
                    getInfoItem(
                        "&nbsp;&nbsp;&nbsp;&nbsp;其中堆内存：${
                            Formatter.formatFileSize(
                                context,
                                (info.nativePss * 1024).toLong()
                            )
                        }"
                    )
                )
                add(
                    getInfoItem(
                        "&nbsp;&nbsp;&nbsp;&nbsp;其他内存：${
                            Formatter.formatFileSize(
                                context,
                                (info.otherPss * 1024).toLong()
                            )
                        }"
                    )
                )
            }

            add(
                getInfoItem(
                    "Dalvik 单应用最大内存：${
                        Formatter.formatFileSize(
                            context,
                            Runtime.getRuntime().totalMemory()
                        )
                    }"
                )
            )
            add(
                getInfoItem(
                    "&nbsp;&nbsp;&nbsp;&nbsp;当前应用栈内存已用量：${
                        Formatter.formatFileSize(
                            context,
                            Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
                        )
                    }"
                )
            )
            add(
                getInfoItem(
                    "&nbsp;&nbsp;&nbsp;&nbsp;当前应用栈内存可用量：${
                        Formatter.formatFileSize(
                            context,
                            Runtime.getRuntime().freeMemory()
                        )
                    }"
                )
            )

            add(getTipsItem("硬件信息"))
            add(getInfoItem("厂商、型号、品牌：${ManufacturerUtil.MANUFACTURER}, ${ManufacturerUtil.MODEL}, ${ManufacturerUtil.BRAND}"))
            add(getInfoItem("硬件名： ${Build.HARDWARE}"))
            add(getInfoItem("产品名：${Build.PRODUCT}"))
            add(getInfoItem("主板名：${Build.BOARD}"))
            add(getInfoItem("CPU 类型：${Build.CPU_ABI}"))
            add(getInfoItem("CPU 核心数：${CPUHelper.getNumberOfCores()}"))
            add(getInfoItem("CPU 温度：${CPUHelper.getCPUTemperature()}"))
            add(getInfoItem("CPU 最大频率：${CPUHelper.getMaxCpuFreq()}"))
            add(getInfoItem("CPU 当前系统使用率：${CPUHelper.getTotalCPURate()}"))
            add(getInfoItem("CPU 当前系统使用时长：${CPUHelper.getTotalCPURate()}"))
            add(getInfoItem("CPU 当前应用使用率：${CPUHelper.getProcessCPURate()}"))
            add(getInfoItem("CPU 当前应用使用时长：${CPUHelper.getProcessCpuTime()}"))
            CPUHelper.getCpuInfo().forEach {
                add(getInfoItem("&nbsp;&nbsp;&nbsp;&nbsp;CPU 信息：${it.key} - ${it.value}"))
            }

            add(getInfoItem("设备宽度：${DisplayUtil.getRealScreenSizeX(context)}"))
            add(getInfoItem("设备高度：${DisplayUtil.getRealScreenSizeY(context)}"))
            add(getInfoItem("状态栏高度：${DisplayUtil.getStatusBarHeight(context)}"))
            add(getInfoItem("虚拟按键高度：${DisplayUtil.getNavigationBarHeight(context)}"))
            add(
                getInfoItem(
                    "存储空间（大小）：${
                        FileUtils.getFileLength(
                            FileUtils.getDirectoryTotalSpace(
                                context!!.filesDir.absolutePath
                            )
                        )
                    }"
                )
            )
            add(
                getInfoItem(
                    "存储空间（当前可用）：${
                        FileUtils.getFileLength(
                            FileUtils.getDirectoryAvailableSpace(
                                context!!.filesDir.absolutePath
                            )
                        )
                    }"
                )
            )

            add(getTipsItem("网络信息"))
            add(getInfoItem("网络类型：${NetworkUtil.getNetworkName(context)}"))
            add(getInfoItem("是否联网：${NetworkUtil.isNetworkConnected(context)}"))
            add(getInfoItem("网络是否可用：${NetworkUtil.isNetworkOnline()}"))
            DtTypeInfo.getDtTypeInfo(context).let {
                add(getInfoItem("Wi-Fi IP：${it.wifiIp}"))
                add(getInfoItem("移动网络 IP：${it.mobileIp}"))
                add(getInfoItem("Mac 地址：${DeviceIDUtils.getMacAddress(context)}"))
            }
            add(getTipsItem("Wi-Fi"))
            add(getInfoItem("路由器 SSID（位置权限）：${WifiManagerWrapper.getSSID()}"))
            add(getInfoItem("路由器 BSSID（位置权限）：${WifiManagerWrapper.getBSSID()}"))
            add(getInfoItem("路由器 Mac(BSSID)：${WifiManagerWrapper.getBSSID()}"))
            add(getInfoItem("路由器 Mac(ARP)：${WifiManagerWrapper.getGatewayMac()}"))
            add(getInfoItem("路由器 IP：${WifiManagerWrapper.getGatewayIpString()}"))
            add(getInfoItem("Wi-Fi 强度：${WifiManagerWrapper.getSignalLevel()} (${WifiManagerWrapper.getRssi()})"))
            add(
                getInfoItem(
                    "Wi-Fi 信道：${
                        WifiChannelInfo.getWifiChannelByFrequency(
                            WifiManagerWrapper.getFrequency()
                        )
                    }"
                )
            )
            add(getInfoItem("Wi-Fi 连接速度：${WifiManagerWrapper.getLinkSpeed()} / ${WifiManagerWrapper.getLinkSpeedUnits()}"))
            add(getInfoItem("Wi-Fi Frequency：${WifiManagerWrapper.getFrequency()}"))
            add(getInfoItem("Wi-Fi 加密类型：${WifiUtil.getWifiCode(context)}"))
            add(getInfoItem("周边Wi-Fi数量（扫描Wi-Fi）：${WifiManagerWrapper.getScanResultList().size}"))
            add(getTipsItem("移动网络"))
            add(getInfoItem("移动网络基站信息（位置权限）：${MobileUtil.getPhoneCellInfo(context)}"))
            add(getInfoItem("是否有SIM卡：${DeviceInfoManager.getInstance().hasSimCard()}"))
            add(getInfoItem("数据开关是否打开：${DeviceInfoManager.getInstance().isMobileSwitchOpened}"))
            add(getInfoItem("移动网络是否打开：${DeviceInfoManager.getInstance().isMobileOpened}"))
            add(
                getInfoItem(
                    "运营商(系统接口)：${
                        DeviceInfoManager.getInstance().getOperatorName(context!!)
                    }"
                )
            )
            add(getInfoItem("运营商：${DeviceInfoManager.getInstance().operatorName}"))
            add(getInfoItem("移动网络信号强度：${MobileUtil.getSignalLevel()}"))

            add(getTipsItem("电量信息"))
            BatteryHelper.getBatteryStatus(ZixieContext.applicationContext!!)?.let {
                add(getInfoItem("充电状态：" + if (it.isCharging) "充电中" else "未充电"))
                add(getInfoItem("充电类型：${it.getChargeTypeDesc()} — ${it.plugged}"))
                add(getInfoItem("当前电量：${it.getBatteryPercentDesc()} - ${it.getBatteryPercent()}"))
                add(getInfoItem("电池温度：${BatteryHelper.getBatteryTemperature(ZixieContext.applicationContext!!)}"))
            }
            add(getTipsItem("当前应用存储占用"))
            add(
                getDebugFragmentItemData(
                    "占用设备存储：" + FileUtils.getFileLength(
                        AppStorageUtil.getCurrentAppSize(
                            context!!
                        )
                    ),
                    DebugStorageFragment::class.java
                )
            )
            add(
                getDebugFragmentItemData(
                    "应用大小：" + FileUtils.getFileLength(
                        AppStorageUtil.getCurrentApplicationSize(
                            context!!
                        )
                    ),
                    DebugStorageFragment::class.java
                )
            )
            add(
                getDebugFragmentItemData(
                    "私有数据目录：" + FileUtils.getFileLength(
                        AppStorageUtil.getCurrentAppDataSize(
                            context!!
                        )
                    ),
                    DebugStorageFragment::class.java
                )
            )
            add(
                getDebugFragmentItemData(
                    "外部数据目录：" + FileUtils.getFileLength(
                        AppStorageUtil.getCurrentAppExternalDirSize(
                            context!!
                        )
                    ),
                    DebugStorageFragment::class.java
                )
            )
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

            override fun doTask() {
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
        DebugLogTips.hide()
        TaskManager.getInstance().removeTask(TASK)
    }

    fun getSimplaInfo(): String {
        ZixieContext.applicationContext?.let { context ->
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
        return ""
    }
}

fun getDeivceName(context: Context?): String {
    try {
        val bluetoothManager =
            context?.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothManager?.adapter?.name ?: ""
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}

fun getMobileInfo(context: Context?): List<String> {
    return mutableListOf<String>().apply {
        add("应用包名: ${context?.packageName}")
        add("设备ID: ${ZixieContext.deviceId}")
        add("设备名称: ${getDeivceName(context)}")
        add("厂商型号: ${ManufacturerUtil.MANUFACTURER}, ${ManufacturerUtil.MODEL}, ${ManufacturerUtil.BRAND}")
        var sdkVersion =
            "系统版本: Android ${BuildUtils.RELEASE}, API  ${BuildUtils.SDK_INT}" + if (ManufacturerUtil.isHarmonyOs()) {
                ", Harmony(${ManufacturerUtil.getHarmonyVersion()})"
            } else {
                ""
            }
        add("<font color ='#3AC8EF'>$sdkVersion</font>")

        add("系统指纹: ${ManufacturerUtil.FINGERPRINT}")
    }
}
