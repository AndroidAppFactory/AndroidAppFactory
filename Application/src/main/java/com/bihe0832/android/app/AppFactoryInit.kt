package com.bihe0832.android.app

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.webkit.WebView
import com.bihe0832.android.app.leakcanary.LeakCanaryManager
import com.bihe0832.android.app.message.AAFMessageManager
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.common.network.NetworkChangeManager
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ZixieCoreInit
import com.bihe0832.android.framework.privacy.AgreementPrivacy.hasAgreedPrivacy
import com.bihe0832.android.lib.adapter.CardInfoHelper
import com.bihe0832.android.lib.device.shake.ShakeManager
import com.bihe0832.android.lib.download.wrapper.DownloadUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.network.MobileUtil
import com.bihe0832.android.lib.network.wifi.WifiManagerWrapper
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.utils.os.ManufacturerUtil
import com.bihe0832.android.lib.web.WebViewHelper
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

            if (ZixieContext.isDebug()) {
                LeakCanaryManager.init(application)
            }

            RouterHelper.initRouter()
            initPermission()
            DownloadUtils.init(ctx, ZixieContext.isDebug())
            AAFMessageManager.initModule(ctx)
            ThreadManager.getInstance().start({
                ZLog.e("Application process initCore web start")
                WebViewHelper.init(ctx, null, Bundle().apply {
                    putString(
                            TbsPrivacyAccess.ConfigurablePrivacy.MODEL.name, ManufacturerUtil.MODEL)
                    putString(TbsPrivacyAccess.ConfigurablePrivacy.ANDROID_ID.name, ZixieContext.deviceId)
                    putString(TbsPrivacyAccess.ConfigurablePrivacy.SERIAL.name, ZixieContext.deviceId)
                }, false)
            }, 5)
            ZLog.d("Application process $processName initCore ManufacturerUtil:" + ManufacturerUtil.MODEL)
        }
    }

    @Synchronized
    private fun initExtra(application: android.app.Application) {
        // 初始化网络变量和监听
        NetworkChangeManager.init(application.applicationContext, true)
        WifiManagerWrapper.init(application.applicationContext, !ZixieContext.isOfficial())
        // 监听信号变化，统一到MobileUtil
        MobileUtil.registerMobileSignalListener(application.applicationContext)
        CardInfoHelper.getInstance().enableDebug(!ZixieContext.isOfficial())
        ShakeManager.init(application.applicationContext)
    }

    fun initAll(application: android.app.Application) {
        if (hasAgreedPrivacy()) {
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
                    } else {
                        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.P) {
                            WebView.setDataDirectorySuffix(it.processName)
                        }
                    }
                }
            }
        }
    }

    fun initUserLoginRetBeforeGetUser(openid: String) {
    }

    fun initPermission() {

        PermissionManager.addPermissionGroup(
                "",
                Manifest.permission.ACCESS_COARSE_LOCATION,
                mutableListOf<String>().apply {
                    add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                }
        )

        PermissionManager.addPermissionGroupDesc(
                HashMap<String, String>().apply {
                    put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "访问存储卡")
                    put(Manifest.permission.RECORD_AUDIO, "麦克风")
                    put(Manifest.permission.ACCESS_COARSE_LOCATION, "位置")
                    put(Manifest.permission.READ_PHONE_STATE, "电话信息")
                    put(Manifest.permission.CAMERA, "相机")
                }
        )

        PermissionManager.addPermissionGroupScene(
                HashMap<String, String>().apply {
                    put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "动态发布、个人/群头像设置、性别认证、聊天发送图片消息等")
                    put(Manifest.permission.RECORD_AUDIO, "发送语音动态、个人语音介绍、开黑房间语音互动、聊天语音消息等")
                    put(Manifest.permission.ACCESS_COARSE_LOCATION, "附近的人、开黑房间、个人主页等")
                    put(Manifest.permission.CAMERA, "动态发布、个人/群头像设置、性别认证、聊天发送图片消息等")
                }
        )

    }

}