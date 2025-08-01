package com.bihe0832.android.common.compose.debug.module

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.bihe0832.android.common.compose.debug.DebugComposeActivity
import com.bihe0832.android.common.compose.debug.DebugComposeItemManager
import com.bihe0832.android.common.compose.debug.DebugComposeRootActivity
import com.bihe0832.android.common.compose.debug.DebugContent
import com.bihe0832.android.common.compose.debug.DebugUtils
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.debug.log.DebugLogComposeActivity
import com.bihe0832.android.common.compose.debug.module.device.GetDeviceInfoView
import com.bihe0832.android.common.compose.debug.module.device.getMobileInfo
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.framework.privacy.AgreementPrivacy
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.select.FileSelectTools
import com.bihe0832.android.lib.lifecycle.ActivityObserver
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.lifecycle.INSTALL_TYPE_APP_FIRST
import com.bihe0832.android.lib.lifecycle.INSTALL_TYPE_NOT_FIRST
import com.bihe0832.android.lib.lifecycle.INSTALL_TYPE_VERSION_FIRST
import com.bihe0832.android.lib.lifecycle.LifecycleHelper
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.bihe0832.android.lib.utils.time.DateUtil

@Composable
fun GetDebugCommonModuleView() {
    val context = LocalContext.current
    GetDebugCommonModuleView { showLog(context) }
}

@Composable
fun GetDebugCommonModuleView(showLog: () -> Unit) {
    DebugContent {
        DebugTips("APPFactory的通用组件和工具")
        DebugItem("查看应用版本及环境") { context -> showAPPInfo(context) }
        DebugItem("查看使用情况") { context -> showUsedInfo(context) }
        DebugItem("查看设备概要信息") { context ->
            DebugUtils.showInfoWithHTML(context, "设备概要信息", getMobileInfo(context))
        }
        DebugItem("查看设备详细信息") { context ->
            DebugComposeItemManager.register("GetDeviceInfoView") { GetDeviceInfoView() }
            DebugComposeRootActivity.startComposeActivity(
                ZixieContext.applicationContext!!,
                "设备详细信息",
                "GetDeviceInfoView"
            )
        }
        DebugItem("查看第三方应用信息") { context -> showOtherAPPInfo(context) }
        DebugItem("<font color ='#3AC8EF'><b>日志管理</b></font>") { context -> showLog() }
        DebugItem("打开开发者模式") { context ->
            IntentUtils.startSettings(
                context, Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
            )
        }
        DebugItem("打开应用设置") { context -> IntentUtils.startAppDetailSettings(context) }
        DebugItem("清除缓存") { context ->
            AAFFileWrapper.clear()
            FileUtils.deleteDirectory(context.cacheDir)
            ZixieContext.restartApp()
        }
        DebugItem("清除用户信息授权") {
            AgreementPrivacy.resetPrivacy()
            ZixieContext.restartApp()
        }
    }
}

fun showAPPInfo(context: Context) {
    DebugUtils.showInfoWithHTML(context, "应用信息", getAPPInfo(context))
}

fun getAPPInfo(context: Context): List<String> {
    return mutableListOf<String>().apply {
        val version = if (ZixieContext.isDebug()) {
            "内测版"
        } else {
            if (ZixieContext.isOfficial()) {
                "外发版"
            } else {
                "预发布版"
            }
        }
        add("应用名称: ${APKUtils.getAppName(context)}")
        add("应用包名: ${ZixieContext.applicationContext!!.packageName}")
        add("安装时间: ${DateUtil.getDateEN(LifecycleHelper.getVersionInstalledTime())}")
        add("版本类型: $version")
        add("<font color ='#3AC8EF'><b>应用版本: ${ZixieContext.getVersionName()}.${ZixieContext.getVersionCode()}</b></font>")
        add("版本标识: ${ZixieContext.getVersionTag()}")
        add(
            "签名MD5: ${
                APKUtils.getSigMd5ByPkgName(
                    ZixieContext.applicationContext,
                    ZixieContext.applicationContext?.packageName,
                )
            }",
        )
    }
}

fun showUsedInfo(context: Context) {
    DebugUtils.showInfo(context, "应用使用情况", getUsedInfo(context))
}

fun getUsedInfo(context: Context): List<String> {
    return mutableListOf<String>().apply {
        add("应用安装时间: ${DateUtil.getDateEN(LifecycleHelper.getAPPInstalledTime())}")
        add("应用安装时间: ${DateUtil.getDateEN(LifecycleHelper.getAPPInstalledTime())}")
        add("当前版本安装时间: ${DateUtil.getDateEN(LifecycleHelper.getVersionInstalledTime())}")

        add("上次启动版本号: ${LifecycleHelper.getAPPLastVersion()}")
        add("上次启动时间: ${DateUtil.getDateEN(LifecycleHelper.getAPPLastStartTime())}")

        add(
            "本次启动类型: ${
                LifecycleHelper.isFirstStart.let {
                    when (it) {
                        INSTALL_TYPE_NOT_FIRST -> "非首次启动"
                        INSTALL_TYPE_VERSION_FIRST -> "版本首次启动"
                        INSTALL_TYPE_APP_FIRST -> "应用首次启动"
                        else -> "类型错误（$it）"
                    }
                }
            }",
        )
        add("本次启动时间: ${DateUtil.getDateEN(ApplicationObserver.getAPPStartTime())}")
        add("累积使用天数: ${LifecycleHelper.getAPPUsedDays()}")
        add("累积使用次数: ${LifecycleHelper.getAPPUsedTimes()}")
        add("当前版本使用次数: ${LifecycleHelper.getCurrentVersionUsedTimes()}")

        add("最后一次退后台: ${DateUtil.getDateEN(ApplicationObserver.getLastPauseTime())}")
        add("最后一次回前台: ${DateUtil.getDateEN(ApplicationObserver.getLastResumedTime())}")

        add("当前页面: ${ActivityObserver.getCurrentActivity()?.javaClass?.name}")
    }
}

fun showOtherAPPInfo(context: Context) {
    val builder = StringBuilder()
    addPackageInfo(context, "com.tencent.mobileqq", builder)
    addPackageInfo(context, "com.tencent.mm", builder)
    addPackageInfo(context, "com.tencent.qqlite", builder)
    addPackageInfo(context, "com.tencent.mobileqqi", builder)
    addPackageInfo(context, "com.tencent.tim", builder)
    DebugUtils.showInfo(context, "第三方应用信息", builder.toString())
}

fun addPackageInfo(context: Context, packageName: String, builder: StringBuilder) {
    val info = APKUtils.getInstalledPackage(context, packageName)
    builder.append("\n$packageName: ")
    if (null == info) {
        builder.append("未安装")
    } else {
        builder.append("\n\tname: ${APKUtils.getAppName(context, packageName)}\n")
        builder.append("	versionName: ${info.versionName}\n")
        builder.append("	versionCode: ${info.versionCode}\n")
    }
}

fun showLog(context: Context) {
    DebugUtils.startActivityWithException(context, DebugLogComposeActivity::class.java)
}