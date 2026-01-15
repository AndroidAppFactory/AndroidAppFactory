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
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.framework.privacy.AgreementPrivacy
import com.bihe0832.android.lib.adapter.CardInfoHelper
import com.bihe0832.android.lib.download.wrapper.DownloadFileUtils
import com.bihe0832.android.lib.download.wrapper.DownloadRangeUtils
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
 * AAF 框架初始化管理器
 *
 * 负责 AAF 框架各模块的初始化工作，包括：
 * - 核心模块初始化（路由、权限、下载、消息等）
 * - WebView 初始化（TBS 内核）
 * - 扩展模块初始化（网络监听、主题、Widget 等）
 *
 * 初始化流程会根据进程类型和隐私协议状态进行差异化处理
 *
 * @author zixie code@bihe0832.com
 * Created on 2019-07-09.
 */
object AppFactoryInit {

    /** 核心模块是否已初始化 */
    var hasInitCore = false

    /** 扩展模块是否已初始化 */
    var hasInitExtra = false

    /**
     * 初始化核心模块
     *
     * 仅主进程和 Web 进程需要初始化，包括：
     * - 隐私协议后的框架初始化
     * - 路由系统初始化
     * - 权限管理初始化
     * - 下载工具初始化
     * - 消息模块初始化
     * - Compose 状态管理初始化
     * - 快捷方式管理初始化
     *
     * @param application Application 实例
     * @param processName 当前进程名称
     */
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
            Log.e(ZixieCoreInit.TAG, "文件日志开关: ${ZixieContext.enableLog()}")
            Log.e(ZixieCoreInit.TAG, "———————————————————————— 设备信息 ————————————————————————")

            RouterHelper.initRouter()
            AAFPermissionManager.initPermission(ctx)
            ThreadManager.getInstance().start {
                DownloadFileUtils.init(ctx, ZixieContext.enableLog())
                DownloadRangeUtils.init(ctx, ZixieContext.enableLog())
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

    /**
     * 初始化 WebView
     *
     * 主进程初始化 TBS WebView 内核，其他进程设置数据目录后缀以避免冲突
     *
     * @param application Application 实例
     * @param processInfo 当前进程信息
     */
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

    /**
     * 初始化扩展模块
     *
     * 包括网络状态监听、信号监听、主题管理、Widget 管理、Google 广告等
     *
     * @param application Application 实例
     */
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
                needChange = false
            )
            // 监听信号变化，统一到MobileUtil
            MobileUtil.registerMobileSignalListener(application.applicationContext)
            CardInfoHelper.getInstance().enableDebug(!ZixieContext.isOfficial())
            ThemeManager.init(application, !ZixieContext.isOfficial())
            WidgetUpdateManager.initModuleWithMainProcess(application.applicationContext)
            AAFGoogleAD.initModule(application.applicationContext)
        }
    }

    /**
     * 执行完整的初始化流程
     *
     * 根据隐私协议状态和进程类型，执行相应的初始化操作：
     * - 主进程：初始化核心模块 + 扩展模块 + WebView
     * - 锁屏进程：仅初始化核心模块
     * - 其他进程：根据需要初始化核心模块和 WebView
     *
     * @param application Application 实例
     */
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
                                application.packageName + application.applicationContext.getString(com.bihe0832.android.lib.lock.screen.R.string.com_bihe0832_lock_screen_process_name),
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

    /**
     * 用户登录成功后的初始化回调
     *
     * 在获取用户信息之前调用，用于执行登录后的初始化操作
     *
     * @param openid 用户的 OpenID
     */
    fun initUserLoginRetBeforeGetUser(openid: String) {
    }
}
