package com.bihe0832.android.app

import android.content.Context
import com.bihe0832.android.lib.network.MobileUtil
import com.bihe0832.android.app.router.APPFactoryRouter
import com.bihe0832.android.app.update.UpdateManager
import com.bihe0832.android.common.network.NetworkChangeManager

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
    fun initCore(ctx: Context) {
        if (!hasInit) {
            hasInit = true
            APPFactoryRouter.initRouter()
        }
    }

    @Synchronized
    fun initExtra(ctx: Context, processName:String) {
        // 初始化网络变量和监听
        NetworkChangeManager.getInstance().init(ctx)
        // 监听信号变化，统一到MobileUtil
        MobileUtil.registerMobileSignalListener(ctx)
    }

    fun initUserLoginRetBeforeGetUser(openid: String) {
    }


}