package com.bihe0832.android.common.compose.debug.module.device

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.os.Process
import android.text.format.Formatter
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.bihe0832.android.common.compose.debug.DebugBaseComposeActivity
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.compose.debug.DebugUtilsV2
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.debug.item.Debuginfo
import com.bihe0832.android.common.compose.debug.module.device.storage.DebugStorageActivity
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.RouterConstants
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
import com.bihe0832.android.lib.utils.apk.AppStorageUtil
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.utils.os.DisplayUtil
import com.bihe0832.android.lib.utils.os.ManufacturerUtil
import com.bihe0832.android.lib.utils.time.DateUtil

open class DebugDeviceActivity : DebugBaseComposeActivity() {

    val statusChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            ZixieContext.showToast(BatteryHelper.getBatteryStatus(context).toString())
        }
    }

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                GetDeviceInfoView()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WifiManagerWrapper.init(
            this,
            debug = true,
            notifyRSSI = true,
            canScanWifi = true,
            canWifiConfiguration = false
        )
    }

    override fun onResume() {
        super.onResume()
        BatteryHelper.startReceiveBatteryChanged(this, statusChangeReceiver)
    }

    override fun onPause() {
        super.onPause()
        BatteryHelper.stopReceiveBatteryChanged(this, statusChangeReceiver)
    }

}

@Composable
fun GetDeviceInfoView() {

    DebugContent {
        val context = LocalContext.current
        DebugTips("设备信息")
        Debuginfo("设备名： ${getDeviceName(context)}")
        Debuginfo("Android ID： ${ZixieContext.deviceId}")
        Debuginfo("Build ID： ${Build.ID}")
        Debuginfo("显示ID： ${Build.DISPLAY}")

        DebugTips("Android 系统信息")
        Debuginfo("系统版本： Android ${BuildUtils.RELEASE}")
        Debuginfo("系统 API：  ${BuildUtils.SDK_INT}")
        Debuginfo("系统指纹： ${ManufacturerUtil.FINGERPRINT}")
        Debuginfo("系统安全patch 时间：  ${Build.VERSION.SECURITY_PATCH}")
        Debuginfo("系统发布时间：  ${DateUtil.getDateEN(Build.TIME)}")
        Debuginfo("系统版本类型：  ${Build.TYPE}")
        Debuginfo("系统CodeName： ${Build.VERSION.CODENAME}")
        if (ManufacturerUtil.isHarmonyOs()) {
            Debuginfo("特殊系统：Harmony  ${ManufacturerUtil.getHarmonyVersion()}")
        }

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val outInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(outInfo)
        DebugTips("应用内存配置")
        Debuginfo("单应用堆内存的初始大小：${ManufacturerUtil.getValueByKey("dalvik.vm.heapstartsize") { "" }}")
        Debuginfo("单应用(标准应用)最大堆内存大小：${ManufacturerUtil.getValueByKey("dalvik.vm.heapgrowthlimit") { "" }}")
        Debuginfo("单应用(largeHeap应用)最大堆内存大小：${ManufacturerUtil.getValueByKey("dalvik.vm.heapsize") { "" }}")
        Debuginfo(
            "Dalvik 虚拟机最大内存：${
                Formatter.formatFileSize(
                    context, Runtime.getRuntime().maxMemory()
                )
            }"
        )
        Debuginfo("系统内存大小：${Formatter.formatFileSize(context, outInfo.totalMem)}")
        Debuginfo("系统触发GC时内存临界值：${Formatter.formatFileSize(context, outInfo.threshold)}")


        DebugTips("当前运行信息")
        Debuginfo(
            "系统剩余内存：${
                Formatter.formatFileSize(
                    context, outInfo.availMem
                )
            }, 是否低内存运行：${outInfo.lowMemory}"
        )

        // 通过这种方法可以传入当前进程的 pid 获取到当前进程的总内存占用情况，其中不仅包括了虚拟机的内存占用情况，还包括原生层和其它内存占用,AndroidQ 版本对这个 API 增加了限制，当采样率较高时，会一直返回一个相同的值。
        val memInfo = activityManager.getProcessMemoryInfo(intArrayOf(Process.myPid()))
        if (memInfo.isNotEmpty()) {
            Debuginfo(
                "当前应用占用内存（精度高，限频：5min）：${
                    Formatter.formatFileSize(
                        context, (memInfo[0].totalPss * 1024).toLong()
                    )
                }"
            )
            Debuginfo(
                "&nbsp;&nbsp;&nbsp;&nbsp;其中栈内存：${
                    Formatter.formatFileSize(
                        context, (memInfo[0].dalvikPss * 1024).toLong()
                    )
                }"
            )
            Debuginfo(
                "&nbsp;&nbsp;&nbsp;&nbsp;其中堆内存：${
                    Formatter.formatFileSize(
                        context,
                        ((memInfo[0].totalPss - memInfo[0].dalvikPss - memInfo[0].otherPss) * 1024).toLong()
                    )
                }"
            )
            Debuginfo(
                "&nbsp;&nbsp;&nbsp;&nbsp;其他内存：${
                    Formatter.formatFileSize(
                        context, (memInfo[0].otherPss * 1024).toLong()
                    )
                }"
            )
        }

        val info: Debug.MemoryInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(info)
        if (memInfo.isNotEmpty()) {
            Debuginfo(
                "当前应用占用内存（精度低，实时获取）：${
                    Formatter.formatFileSize(
                        context, (info.totalPss * 1024).toLong()
                    )
                }"
            )
            Debuginfo(
                "&nbsp;&nbsp;&nbsp;&nbsp;其中栈内存：${
                    Formatter.formatFileSize(
                        context, (info.dalvikPss * 1024).toLong()
                    )
                }"
            )
            Debuginfo(
                "&nbsp;&nbsp;&nbsp;&nbsp;其中堆内存：${
                    Formatter.formatFileSize(
                        context, (info.nativePss * 1024).toLong()
                    )
                }"
            )
            Debuginfo(
                "&nbsp;&nbsp;&nbsp;&nbsp;其他内存：${
                    Formatter.formatFileSize(
                        context, (info.otherPss * 1024).toLong()
                    )
                }"
            )
        }

        Debuginfo(
            "Dalvik 单应用最大内存：${
                Formatter.formatFileSize(
                    context, Runtime.getRuntime().totalMemory()
                )
            }"
        )
        Debuginfo(
            "&nbsp;&nbsp;&nbsp;&nbsp;当前应用栈内存已用量：${
                Formatter.formatFileSize(
                    context, Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
                )
            }"
        )
        Debuginfo(
            "&nbsp;&nbsp;&nbsp;&nbsp;当前应用栈内存可用量：${
                Formatter.formatFileSize(
                    context, Runtime.getRuntime().freeMemory()
                )
            }"
        )

        DebugTips("硬件信息")
        Debuginfo("厂商、型号、品牌：${ManufacturerUtil.MANUFACTURER}, ${ManufacturerUtil.MODEL}, ${ManufacturerUtil.BRAND}")
        Debuginfo("硬件名： ${Build.HARDWARE}")
        Debuginfo("产品名：${Build.PRODUCT}")
        Debuginfo("主板名：${Build.BOARD}")
        Debuginfo("CPU 类型：${Build.CPU_ABI}")
        Debuginfo("CPU 核心数：${CPUHelper.getNumberOfCores()}")
        Debuginfo("CPU 温度：${CPUHelper.getCPUTemperature()}")
        Debuginfo("CPU 最大频率：${CPUHelper.getMaxCpuFreq()}")
        Debuginfo("CPU 当前系统使用率：${CPUHelper.getTotalCPURate()}")
        Debuginfo("CPU 当前系统使用时长：${CPUHelper.getTotalCPURate()}")
        Debuginfo("CPU 当前应用使用率：${CPUHelper.getProcessCPURate()}")
        Debuginfo("CPU 当前应用使用时长：${CPUHelper.getProcessCpuTime()}")
        CPUHelper.getCpuInfo().forEach {
            Debuginfo("&nbsp;&nbsp;&nbsp;&nbsp;CPU 信息：${it.key} - ${it.value}")
        }

        Debuginfo("设备宽度：${DisplayUtil.getRealScreenSizeX(context)}")
        Debuginfo("设备高度：${DisplayUtil.getRealScreenSizeY(context)}")
        Debuginfo("状态栏高度：${DisplayUtil.getStatusBarHeight(context)}")
        Debuginfo("虚拟按键高度：${DisplayUtil.getNavigationBarHeight(context)}")
        Debuginfo("存储空间（大小）：${FileUtils.getFileLength(FileUtils.getDirectoryTotalSpace(context.filesDir.absolutePath))}")
        Debuginfo(
            "存储空间（当前可用）：${
                FileUtils.getFileLength(
                    FileUtils.getDirectoryAvailableSpace(
                        context.filesDir.absolutePath
                    )
                )
            }"
        )

        DebugTips("网络信息")
        Debuginfo("网络类型：${NetworkUtil.getNetworkName(context)}")
        Debuginfo("是否联网：${NetworkUtil.isNetworkConnected(context)}")
        Debuginfo("网络是否可用：${NetworkUtil.isNetworkOnline()}")
        DtTypeInfo.getDtTypeInfo(context).let {
            Debuginfo("Wi-Fi IP：${it.wifiIp}")
            Debuginfo("移动网络 IP：${it.mobileIp}")
            Debuginfo("Mac 地址：${DeviceIDUtils.getMacAddress(context)}")
        }
        DebugTips("Wi-Fi")
        Debuginfo("路由器 SSID（位置权限）：${WifiManagerWrapper.getSSID()}")
        Debuginfo("路由器 BSSID（位置权限）：${WifiManagerWrapper.getBSSID()}")
        Debuginfo("路由器 Mac(BSSID)：${WifiManagerWrapper.getBSSID()}")
        Debuginfo("路由器 Mac(ARP)：${WifiManagerWrapper.getGatewayMac()}")
        Debuginfo("路由器 IP：${WifiManagerWrapper.getGatewayIpString()}")
        Debuginfo("Wi-Fi 强度：${WifiManagerWrapper.getSignalLevel()} (${WifiManagerWrapper.getRssi()})")
        Debuginfo("Wi-Fi 信道：${WifiChannelInfo.getWifiChannelByFrequency(WifiManagerWrapper.getFrequency())}")
        Debuginfo("Wi-Fi 连接速度：${WifiManagerWrapper.getLinkSpeed()} / ${WifiManagerWrapper.getLinkSpeedUnits()}")
        Debuginfo("Wi-Fi Frequency：${WifiManagerWrapper.getFrequency()}")
        Debuginfo("Wi-Fi 加密类型：${WifiUtil.getWifiCode(context)}")
        Debuginfo("周边Wi-Fi数量（扫描Wi-Fi）：${WifiManagerWrapper.getScanResultList().size}")
        DebugTips("移动网络")
        Debuginfo("移动网络基站信息（位置权限）：${MobileUtil.getPhoneCellInfo(context)}")
        Debuginfo("是否有SIM卡：${DeviceInfoManager.getInstance().hasSimCard()}")
        Debuginfo("数据开关是否打开：${DeviceInfoManager.getInstance().isMobileSwitchOpened}")
        Debuginfo("移动网络是否打开：${DeviceInfoManager.getInstance().isMobileOpened}")
        Debuginfo("运营商(系统接口)：${DeviceInfoManager.getInstance().getOperatorName(context)}")
        Debuginfo("运营商：${DeviceInfoManager.getInstance().operatorName}")
        Debuginfo("移动网络信号强度：${MobileUtil.getSignalLevel()}")

        DebugTips("电量信息")
        BatteryHelper.getBatteryStatus(ZixieContext.applicationContext!!)?.let {
            Debuginfo("充电状态：" + if (it.isCharging) "充电中" else "未充电")
            Debuginfo("充电类型：${it.getChargeTypeDesc()} — ${it.plugged}")
            Debuginfo("当前电量：${it.getBatteryPercentDesc()} - ${it.getBatteryPercent()}")
            Debuginfo("电池温度：${BatteryHelper.getBatteryTemperature(ZixieContext.applicationContext!!)}")
        }
        DebugTips("当前应用存储占用：点击对应项跳转可以查看文件详情")

        DebugItem("占用设备存储：" + FileUtils.getFileLength(AppStorageUtil.getCurrentAppSize(context))) { context ->
            getDebugStorageActivityAction(context, "")
        }

        DebugItem(
            "应用大小：" + FileUtils.getFileLength(
                AppStorageUtil.getCurrentApplicationSize(
                    context
                )
            )
        ) { context ->
            getDebugStorageActivityAction(context, "")
        }

        DebugItem(
            "私有数据目录：" + FileUtils.getFileLength(
                AppStorageUtil.getCurrentAppDataSize(
                    context
                )
            )
        ) { context ->
            getDebugStorageActivityAction(context, context.applicationInfo.dataDir)
        }
        DebugItem(
            "外部数据目录：" + FileUtils.getFileLength(
                AppStorageUtil.getCurrentAppExternalDirSize(
                    context
                )
            )
        ) { context ->
            getDebugStorageActivityAction(context, context.getExternalFilesDir("")?.absolutePath)
        }
    }


}

fun getDebugStorageActivityAction(context: Context, filePath: String?) {
    DebugUtilsV2.startActivityWithException(
        context,
        DebugStorageActivity::class.java,
        HashMap<String, String>().apply {
            put(RouterConstants.INTENT_EXTRA_KEY_WEB_URL, filePath ?: "")
        })
}