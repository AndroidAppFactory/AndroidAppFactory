package com.bihe0832.android.app

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.webkit.WebView
import com.bihe0832.android.app.message.AAFMessageManager
import com.bihe0832.android.app.permission.AAFPermissionManager
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.common.network.NetworkChangeManager
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ZixieCoreInit
import com.bihe0832.android.framework.privacy.AgreementPrivacy
import com.bihe0832.android.lib.adapter.CardInfoHelper
import com.bihe0832.android.lib.device.shake.ShakeManager
import com.bihe0832.android.lib.download.wrapper.DownloadUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.network.MobileUtil
import com.bihe0832.android.lib.theme.ThemeManager
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.utils.os.ManufacturerUtil
import com.bihe0832.android.lib.webview.tbs.WebViewHelper
import com.bihe0832.android.lib.widget.WidgetUpdateManager
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.TbsPrivacyAccess

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2019-07-09.
 * Description: 加速器相关的初始化
 *
 */

object AppFactoryInit {
    // 全局变量的初始化
    var hasInit = false

    //目前仅仅主进程和web进程需要初始化
    @Synchronized
    private fun initCore(application: android.app.Application, processName: String) {
        val ctx = application.applicationContext
        if (!hasInit) {
            hasInit = true
            ZixieCoreInit.initAfterAgreePrivacy(application)
            Log.e(ZixieCoreInit.TAG, "———————————————————————— 设备信息 ————————————————————————")
            Log.e(ZixieCoreInit.TAG, "设备ID: ${ZixieContext.deviceId}")
            Log.e(ZixieCoreInit.TAG, "厂商型号: ${ManufacturerUtil.MANUFACTURER}, ${ManufacturerUtil.MODEL}, ${ManufacturerUtil.BRAND}")
            Log.e(ZixieCoreInit.TAG, "系统版本: Android ${BuildUtils.RELEASE}, API  ${BuildUtils.SDK_INT}" + if (ManufacturerUtil.isHarmonyOs()) {
                ", Harmony(${ManufacturerUtil.getHarmonyVersion()})"
            } else {
                ""
            })
            Log.e(ZixieCoreInit.TAG, "———————————————————————— 设备信息 ————————————————————————")

            RouterHelper.initRouter()
            AAFPermissionManager.initPermission()
            DownloadUtils.init(ctx, ZixieContext.isDebug())
            AAFMessageManager.initModule(ctx)

            ZLog.d("Application process $processName initCore ManufacturerUtil:" + ManufacturerUtil.MODEL)
        }
    }

    private fun initWebview(application: android.app.Application, processInfo: ActivityManager.RunningAppProcessInfo) {
        if (processInfo.processName.equals(application.packageName, ignoreCase = true)) {
            ThreadManager.getInstance().start({
                ZLog.e("Application process initCore web start")
                ZLog.d("" + QbSdk.getTbsVersion(application.applicationContext))

                WebViewHelper.init(application.applicationContext, null, Bundle().apply {
                    putString(
                            TbsPrivacyAccess.ConfigurablePrivacy.MODEL.name, ManufacturerUtil.MODEL)
                    putString(TbsPrivacyAccess.ConfigurablePrivacy.ANDROID_ID.name, ZixieContext.deviceId)
                    putString(TbsPrivacyAccess.ConfigurablePrivacy.SERIAL.name, ZixieContext.deviceId)
                }, false)
            }, 5)
        } else {
            if (BuildUtils.SDK_INT >= Build.VERSION_CODES.P) {
                WebView.setDataDirectorySuffix(processInfo.processName)
            }
        }
    }

    @Synchronized
    private fun initExtra(application: android.app.Application) {
        // 初始化网络变量和监听
        NetworkChangeManager.init(application.applicationContext, true)
        // 监听信号变化，统一到MobileUtil
        MobileUtil.registerMobileSignalListener(application.applicationContext)
        CardInfoHelper.getInstance().enableDebug(!ZixieContext.isOfficial())
        ShakeManager.init(application.applicationContext)
        ThemeManager.init(application, !ZixieContext.isOfficial())
    }

    fun initAll(application: android.app.Application) {
        if (AgreementPrivacy.hasAgreedPrivacy()) {
            val am = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningApps = am.runningAppProcesses
            for (it in runningApps) {
                if (it.pid == Process.myPid() && it.processName != null &&
                        it.processName.contains(application.getPackageName())) {
                    ZLog.e("Application initCore process: name:" + it.processName + " and id:" + it.pid)
                    val processName = it.processName
                    initCore(application, processName)
                    if (processName.equals(application.packageName, ignoreCase = true)) {
                        initExtra(application)
                    } else if (processName.equals(application.packageName + application.applicationContext.getString(R.string.com_bihe0832_widgets_process_name), ignoreCase = true)) {
                        WidgetUpdateManager.initModule(application.applicationContext)
                    }
                    initWebview(application, it)
                }
            }
        }
    }

    fun initUserLoginRetBeforeGetUser(openid: String) {
    }



}