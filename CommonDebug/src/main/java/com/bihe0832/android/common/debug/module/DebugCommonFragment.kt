package com.bihe0832.android.common.debug.module

import android.content.Intent
import android.provider.Settings
import android.view.View
import com.bihe0832.android.common.debug.device.DebugDeviceFragment
import com.bihe0832.android.common.debug.device.getMobileInfo
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.item.getTipsItem
import com.bihe0832.android.common.debug.log.DebugLogListActivity
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.framework.privacy.AgreementPrivacy
import com.bihe0832.android.lib.adapter.CardBaseModule
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

open class DebugCommonFragment : DebugEnvFragment() {

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getTipsItem("APPFactory的通用组件和工具"))
            add(getDebugItem("查看应用版本及环境", View.OnClickListener { showAPPInfo() }))
            add(getDebugItem("查看使用情况", View.OnClickListener { showUsedInfo() }))
            add(
                getDebugItem(
                    "查看设备概要信息",
                    View.OnClickListener { showInfoWithHTML("设备概要信息", getMobileInfo(context)) },
                ),
            )
            add(getDebugFragmentItemData("查看设备详细信息", DebugDeviceFragment::class.java))
            add(getDebugItem("查看第三方应用信息", View.OnClickListener { showOtherAPPInfo() }))
            add(
                getDebugItem(
                    "<font color ='#3AC8EF'><b>日志管理</b></font>",
                    View.OnClickListener {
                        showLog()
                    },
                ),
            )
            add(
                getDebugItem("打开开发者模式") {
                    IntentUtils.startSettings(context, Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                },
            )
            add(
                getDebugItem("打开应用设置") {
                    IntentUtils.startAppDetailSettings(context)
                },
            )
            add(
                getDebugItem("清除缓存") {
                    AAFFileWrapper.clear()
                    FileUtils.deleteDirectory(context!!.cacheDir)
                    ZixieContext.restartApp()
                },
            )
            add(
                getDebugItem("清除用户信息授权") {
                    AgreementPrivacy.resetPrivacy()
                    ZixieContext.restartApp()
                },
            )
        }
    }

    protected fun startActivity(activityName: String) {
        try {
            startActivityWithException(activityName)
        } catch (e: Exception) {
            e.printStackTrace()
            ZixieContext.showToast("请确认当前运行的测试模块是否包含该应用")
        }
    }

    protected open fun showLog() {
        startActivityWithException(DebugLogListActivity::class.java)
    }

    protected fun showAPPInfo() {
        showInfoWithHTML("应用信息", getAPPInfo())
    }

    protected fun getAPPInfo(): List<String> {
        return mutableListOf<String>().apply {
            var version = if (ZixieContext.isDebug()) {
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

    protected fun showUsedInfo() {
        showInfo("应用使用情况", getUsedInfo())
    }

    protected fun getUsedInfo(): List<String> {
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

    protected fun showOtherAPPInfo() {
        val builder = StringBuilder()
        addPackageInfo("com.tencent.mobileqq", builder)
        addPackageInfo("com.tencent.mm", builder)
        addPackageInfo("com.tencent.qqlite", builder)
        addPackageInfo("com.tencent.mobileqqi", builder)
        addPackageInfo("com.tencent.tim", builder)
        showInfo("第三方应用信息", builder.toString())
    }

    protected fun addPackageInfo(packageName: String, builder: StringBuilder) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FileSelectTools.FILE_CHOOSER && resultCode == RESULT_OK) {
            data?.extras?.getString(FileSelectTools.INTENT_EXTRA_KEY_WEB_URL, "")?.let { filePath ->
                FileUtils.sendFile(activity!!, filePath).let {
                    if (!it) {
                        ZixieContext.showToast("分享文件:$filePath 失败")
                    }
                }
            }
        }
    }
}
