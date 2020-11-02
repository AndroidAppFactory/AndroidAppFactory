package com.bihe0832.android.test.module

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import com.bihe0832.android.app.router.APPFactoryRouter
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.common.praise.UserPraiseManager
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ZixieContext.getDeviceId
import com.bihe0832.android.framework.ZixieContext.getVersionCode
import com.bihe0832.android.framework.ZixieContext.getVersionName
import com.bihe0832.android.framework.ZixieContext.isDebug
import com.bihe0832.android.framework.ZixieContext.isOfficial
import com.bihe0832.android.framework.ZixieContext.showDebug
import com.bihe0832.android.lib.text.InputDialogCompletedCallback
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.test.base.BaseTestFragment
import com.bihe0832.android.test.base.TestItem

open class TestDebugCommonFragment : BaseTestFragment() {
    private var lastUrl = "https://blog.bihe0832.com"

    companion object {
        @JvmStatic
        fun newInstance(): TestDebugCommonFragment {
            val args = Bundle()
            val fragment = TestDebugCommonFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getDataList(): List<TestItem> {
        return mutableListOf<TestItem>().apply {
            add(TestItem("查看应用版本及环境") { showAPPInfo() })
            add(TestItem("查看设备信息") { showMobileInfo() })
            add(TestItem("分享第三方应用信息") { showOtherAPPInfo() })
            add(TestItem("打开指定Web页面") {
                showInputDialog("打开指定Web页面", "请在输入框输入网页地址后点击“确定”", lastUrl, InputDialogCompletedCallback { result: String ->
                    try {
                        if (!TextUtils.isEmpty(result)) {
                            lastUrl = result
                            openWeb(result)
                        } else {
                            showDebug("请输入正确的网页地址")
                        }
                    } catch (e: Exception) {
                    }
                })
            })
            add(TestItem("打开JSbridge调试页面") { openWeb("https://microdemo.bihe0832.com/jsbridge/index.html") })
            add(TestItem("打开TBS调试页面") { openWeb("http://debugtbs.qq.com/") })
            add(TestItem("弹出评分页面") {
                UserPraiseManager.showUserPraiseDialog(activity!!, APPFactoryRouter.getFinalURL(RouterConstants.MODULE_NAME_FEEDBACK))
            })
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
        version = if (isDebug) {
            "内测版"
        } else {
            if (isOfficial) {
                "外发版"
            } else {
                "预发布版"
            }
        }
        builder.append("PackageName: ${context!!.packageName}\n")
        builder.append("Version: ${getVersionName()}.${getVersionCode()}\n")
        builder.append("Tag: ${ZixieContext.tag}\n")
        showInfo("${APKUtils.getAppName(context)} $version 信息", builder.toString())
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