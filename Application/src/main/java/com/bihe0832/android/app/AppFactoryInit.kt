package com.bihe0832.android.app

import android.content.Context
import android.os.Bundle
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.common.network.NetworkChangeManager
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.download.wrapper.DownloadUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.network.MobileUtil
import com.bihe0832.android.lib.network.WifiManagerWrapper
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.os.ManufacturerUtil
import com.bihe0832.android.lib.web.WebViewHelper
import com.tencent.smtt.sdk.TbsPrivacyAccess

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2019-07-09.
 * Description: 加速器相关的初始化
 *
 */

object AppFactoryInit {
    // 全局变量的初始化
    var hasInit = false

    //目前仅仅主进程和web进程需要初始化
    @Synchronized
    fun initCore(ctx: Context, processName: String) {
        if (!hasInit) {
            hasInit = true
            RouterHelper.initRouter()
            initPermission()
            DownloadUtils.init(ctx, 3, null, ZixieContext.isDebug())
            ThreadManager.getInstance().start({
                ZLog.e("Application process initCore web start")
                WebViewHelper.init(ctx, null, Bundle().apply {
                    putString(
                        TbsPrivacyAccess.ConfigurablePrivacy.MODEL.name,
                        ManufacturerUtil.MODEL
                    )
                    putString(
                        TbsPrivacyAccess.ConfigurablePrivacy.ANDROID_ID.name,
                        ZixieContext.deviceId
                    )
                    putString(
                        TbsPrivacyAccess.ConfigurablePrivacy.SERIAL.name,
                        ZixieContext.deviceId
                    )
                }, true)
            }, 5)
            ZLog.d("Application process $processName initCore ManufacturerUtil:" + ManufacturerUtil.MODEL)
        }
    }

    @Synchronized
    fun initExtra(ctx: Context) {
        // 初始化网络变量和监听
        NetworkChangeManager.getInstance().init(ctx, true)
        WifiManagerWrapper.init(ctx, !ZixieContext.isOfficial())
        // 监听信号变化，统一到MobileUtil
        MobileUtil.registerMobileSignalListener(ctx)
    }

    fun initUserLoginRetBeforeGetUser(openid: String) {
    }

    fun initPermission() {

    }

}