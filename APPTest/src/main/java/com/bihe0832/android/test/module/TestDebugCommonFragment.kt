package com.bihe0832.android.test.module

import android.net.Uri
import android.os.Build
import android.view.View
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.common.praise.UserPraiseManager
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ZixieContext.getDeviceId
import com.bihe0832.android.framework.ZixieContext.getVersionCode
import com.bihe0832.android.framework.ZixieContext.getVersionName
import com.bihe0832.android.framework.ZixieContext.isDebug
import com.bihe0832.android.framework.ZixieContext.isOfficial
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.lifecycle.*
import com.bihe0832.android.lib.utils.DateUtil
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.bihe0832.android.test.base.BaseTestFragment
import com.bihe0832.android.test.base.item.TestItemData
import com.bihe0832.android.test.base.item.TestTipsData
import com.bihe0832.android.test.module.network.TestNetworkActivity
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

open class TestDebugCommonFragment : BaseTestFragment() {

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(TestTipsData("APPFactory的通用组件和工具"))
            add(TestItemData("查看应用版本及环境", View.OnClickListener { showAPPInfo() }))
            add(TestItemData("查看使用情况", View.OnClickListener { showUsedInfo() }))
            add(TestItemData("查看设备信息", View.OnClickListener { showMobileInfo() }))
            add(TestItemData("查看第三方应用信息", View.OnClickListener { showOtherAPPInfo() }))
            add(TestItemData("弹出评分页面", View.OnClickListener {
                UserPraiseManager.showUserPraiseDialog(activity!!, RouterHelper.getFinalURL(RouterConstants.MODULE_NAME_FEEDBACK))
            }))
            add(TestItemData("打开反馈页面", View.OnClickListener {
                val map = HashMap<String, String>()
                map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = Uri.encode("https://support.qq.com/product/290858")
                RouterHelper.openPageRouter(RouterConstants.MODULE_NAME_FEEDBACK, map)
            }))
            add(TestItemData("网络切换监控", View.OnClickListener { startActivity(TestNetworkActivity::class.java) }))
            add(TestItemData("打开应用设置", { IntentUtils.startAppDetailSettings(context) }))
            add(TestItemData("打开开发者模式", View.OnClickListener {
                IntentUtils.startSettings(context, android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            }))

        }
    }

    private fun showMobileInfo() {
        val builder = StringBuilder()
        builder.append("PackageName: ${context!!.packageName}\n")
        builder.append("deviceId: ${getDeviceId()}\n")
        builder.append("厂商&型号: ${Build.MANUFACTURER}, ${Build.MODEL}, ${Build.BRAND}\n")
        builder.append("系统版本: ${Build.VERSION.RELEASE}, ${Build.VERSION.SDK_INT}\n")
        builder.append("系统指纹: ${Build.FINGERPRINT}\n")

        showInfo("分享设备信息给开发者", builder.toString())
    }

    private fun showAPPInfo() {
        val builder = StringBuilder()
        var version = ""
        version = if (isDebug()) {
            "内测版"
        } else {
            if (isOfficial()) {
                "外发版"
            } else {
                "预发布版"
            }
        }
        builder.append("PackageName: ${context!!.packageName}\n")
        builder.append("Version: ${getVersionName()}.${getVersionCode()}\n")
        builder.append("Tag: ${ZixieContext.getVersionTag()}\n")
        showInfo("${APKUtils.getAppName(context)} $version 信息", builder.toString())
    }

    private fun showUsedInfo() {
        val builder = StringBuilder()

        builder.append("应用安装时间: ${DateUtil.getDateEN(LifecycleHelper.getAPPInstalledTime())}\n")
        builder.append("当前版本安装时间: ${DateUtil.getDateEN(LifecycleHelper.getVersionInstalledTime())}\n")

        builder.append("上次启动版本号: ${LifecycleHelper.getAPPLastVersion()}\n")
        builder.append("上次启动时间: ${DateUtil.getDateEN(LifecycleHelper.getAPPLastStartTime())}\n")

        builder.append("本次启动类型: ${
            LifecycleHelper.isFirstStart.let {
                when (it) {
                    INSTALL_TYPE_NOT_FIRST -> "非首次启动"
                    INSTALL_TYPE_VERSION_FIRST -> "版本首次启动"
                    INSTALL_TYPE_APP_FIRST -> "应用首次启动"
                    else -> "类型错误（$it）"
                }
            }
        }\n")
        builder.append("本次启动时间: ${DateUtil.getDateEN(ApplicationObserver.getAPPStartTime())}\n")
        builder.append("累积使用天数: ${LifecycleHelper.getAPPUsedDays()}\n")
        builder.append("累积使用次数: ${LifecycleHelper.getAPPUsedTimes()}\n")

        builder.append("最后一次退后台: ${DateUtil.getDateEN(ApplicationObserver.getLastPauseTime())}\n")
        builder.append("最后一次回前台: ${DateUtil.getDateEN(ApplicationObserver.getLastResumedTime())}\n")

        builder.append("当前页面: ${ActivityObserver.getCurrentActivity()?.javaClass?.name}\n")
        showInfo("应用使用情况", builder.toString())
    }

    private fun showOtherAPPInfo() {
        val builder = StringBuilder()
        builder.append("第三方应用信息:\n\n")
        addPackageInfo("com.tencent.mobileqq", builder)
        addPackageInfo("com.tencent.mm", builder)
        addPackageInfo("com.tencent.qqlite", builder)
        addPackageInfo("com.tencent.mobileqqi", builder)
        addPackageInfo("com.tencent.tim", builder)
        sendInfo("分享第三方应用信息给开发者", builder.toString())
    }

    private fun addPackageInfo(packageName: String, builder: StringBuilder) {
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
}