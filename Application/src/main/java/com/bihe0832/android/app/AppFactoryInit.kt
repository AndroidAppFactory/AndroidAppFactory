package com.bihe0832.android.app

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.webkit.WebView
import com.bihe0832.android.app.compose.AAFComposeStateManager
import com.bihe0832.android.app.message.AAFMessageManager
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.app.shortcut.AAFShortcutManager
import com.bihe0832.android.common.network.NetworkChangeManager
import com.bihe0832.android.common.permission.AAFPermissionManager
import com.bihe0832.android.common.webview.tbs.WebViewHelper
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ZixieCoreInit
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.framework.privacy.AgreementPrivacy
import com.bihe0832.android.lib.adapter.CardInfoHelper
import com.bihe0832.android.lib.download.wrapper.DownloadFileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.network.MobileUtil
import com.bihe0832.android.lib.theme.ThemeManager
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.utils.os.ManufacturerUtil
import com.bihe0832.android.lib.utils.time.DateUtil
import com.bihe0832.android.lib.widget.WidgetUpdateManager
import com.bihe0832.android.services.google.ad.AAFGoogleAD
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
    var hasInitCore = false
    var hasInitExtra = false

    // 目前仅仅主进程和web进程需要初始化
    @Synchronized
    private fun initCore(application: android.app.Application, processName: String) {
        ZLog.d(ZixieCoreInit.TAG, "Application process $processName initCore ")
        val ctx = application.applicationContext
        if (!hasInitCore) {
            hasInitCore = true
            ZixieCoreInit.initAfterAgreePrivacy(application)
            Log.e(ZixieCoreInit.TAG, "———————————————————————— 设备信息 ————————————————————————")
            Log.e(ZixieCoreInit.TAG, "设备ID: ${ZixieContext.deviceId}")
            Log.e(
                ZixieCoreInit.TAG,
                "厂商型号: ${ManufacturerUtil.MANUFACTURER}, ${ManufacturerUtil.MODEL}, ${ManufacturerUtil.BRAND}",
            )
            Log.e(
                ZixieCoreInit.TAG,
                "系统版本: Android ${BuildUtils.RELEASE}, API  ${BuildUtils.SDK_INT}" + if (ManufacturerUtil.isHarmonyOs()) {
                    ", Harmony(${ManufacturerUtil.getHarmonyVersion()})"
                } else {
                    ""
                },
            )
            Log.e(ZixieCoreInit.TAG, "———————————————————————— 设备信息 ————————————————————————")

            RouterHelper.initRouter()
            AAFPermissionManager.initPermission(ctx)
            ThreadManager.getInstance().start {
                DownloadFileUtils.init(ctx, ZixieContext.isDebug())
            }
            ThreadManager.getInstance().start {
                AAFFileWrapper.autoClear(DateUtil.MILLISECOND_OF_MONTH)
            }
            AAFMessageManager.initModule(ctx)
            AAFComposeStateManager.init(ctx)
            AAFShortcutManager.init(ctx)
            ZLog.d(
                ZixieCoreInit.TAG,
                "Application process $processName initCore ManufacturerUtil:" + ManufacturerUtil.MODEL,
            )
        }
    }

    private fun initWebview(
        application: android.app.Application,
        processInfo: ActivityManager.RunningAppProcessInfo
    ) {
        if (processInfo.processName.equals(application.packageName, ignoreCase = true)) {
            ThreadManager.getInstance().start({
                ZLog.e(
                    ZixieCoreInit.TAG,
                    "Application process initWebview：" + processInfo.processName
                )
                ZLog.d(ZixieCoreInit.TAG, "" + QbSdk.getTbsVersion(application.applicationContext))

                WebViewHelper.init(
                    application.applicationContext,
                    null,
                    Bundle().apply {
                        putString(
                            TbsPrivacyAccess.ConfigurablePrivacy.MODEL.name,
                            ManufacturerUtil.MODEL,
                        )
                        putString(
                            TbsPrivacyAccess.ConfigurablePrivacy.ANDROID_ID.name,
                            ZixieContext.deviceId
                        )
                        putString(
                            TbsPrivacyAccess.ConfigurablePrivacy.SERIAL.name,
                            ZixieContext.deviceId
                        )
                    },
                    false,
                )
            }, 5)
        } else {
            if (BuildUtils.SDK_INT >= Build.VERSION_CODES.P) {
                ZLog.d(
                    ZixieCoreInit.TAG,
                    "Application setDataDirectorySuffix " + processInfo.processName
                )
                WebView.setDataDirectorySuffix(processInfo.processName)
            }
        }
    }

    @Synchronized
    private fun initExtra(application: android.app.Application) {
        ZLog.d(ZixieCoreInit.TAG, "Application initExtra ")
        if (!hasInitExtra) {
            hasInitExtra = true
            // 初始化网络变量和监听
            NetworkChangeManager.init(
                application.applicationContext,
                getNetType = true,
                getSSID = true,
                getBssID = true,
                curCellId = true,
            )
            // 监听信号变化，统一到MobileUtil
            MobileUtil.registerMobileSignalListener(application.applicationContext)
            CardInfoHelper.getInstance().enableDebug(!ZixieContext.isOfficial())
            ThemeManager.init(application, !ZixieContext.isOfficial())
            WidgetUpdateManager.initModuleWithMainProcess(application.applicationContext)
            AAFGoogleAD.initModule(application.applicationContext)
        }
    }

    fun initAll(application: android.app.Application) {
        ZLog.d(ZixieCoreInit.TAG, "Application initAll ")
        if (AgreementPrivacy.hasAgreedPrivacy()) {
            val am = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningApps = am.runningAppProcesses
            for (it in runningApps) {
                if (it.pid == Process.myPid() && it.processName != null &&
                    it.processName.contains(application.packageName)
                ) {
                    ZLog.e(
                        ZixieCoreInit.TAG,
                        "Application initCore process: name:" + it.processName + " and id:" + it.pid,
                    )
                    val processName = it.processName
                    if (!processName.equals(
                            application.packageName + application.applicationContext.getString(R.string.process_name_domain),
                            ignoreCase = true,
                        )
                    ) {
                        initCore(application, processName)
                        if (processName.equals(application.packageName, ignoreCase = true)) {
                            initExtra(application)
                        } else if (processName.equals(
                                application.packageName + application.applicationContext.getString(R.string.com_bihe0832_lock_screen_process_name),
                                ignoreCase = true,
                            )
                        ) {
                            ZLog.e(
                                ZixieCoreInit.TAG,
                                "Application WidgetUpdateManager initModuleWithOtherProcess  process: name:" + it.processName + " and id:" + it.pid,
                            )
                        }
                        initWebview(application, it)
                    }
                }
            }
        }
    }

    fun initUserLoginRetBeforeGetUser(openid: String) {
    }
}
