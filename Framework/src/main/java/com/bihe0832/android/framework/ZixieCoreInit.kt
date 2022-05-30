package com.bihe0832.android.framework

import android.app.Application
import android.util.Log
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.framework.constant.Constants;


import com.bihe0832.android.framework.privacy.AgreementPrivacy
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.lifecycle.LifecycleHelper
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.os.DisplayUtil


/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2019-07-09.
 * Description: 基础模块
 *
 */

object ZixieCoreInit {
    // 全局变量的初始化
    var hasInit = false
    private const val TAG = "ZixieCoreInit"

    //目前仅仅主进程和web进程需要初始化
    @Synchronized
    fun initCore(application: Application, appIsDebug: Boolean, appIsOfficial: Boolean, skipPrivacy: Boolean, appTag: String) {
        ZixieContext.init(application, appIsDebug, appIsOfficial, appTag)
        if (!hasInit) {
            hasInit = true
            ZLog.setDebug(!ZixieContext.isOfficial())
            LoggerFile.init(application, !ZixieContext.isOfficial())

            // 初始化配置管理
            Config.init(ZixieContext.applicationContext, Constants.CONFIG_COMMON_FILE_NAME, !ZixieContext.isOfficial())
            Config.loadLoaclFile(application, Constants.CONFIG_SPECIAL_FILE_NAME, !ZixieContext.isOfficial())
            Log.e(TAG, "———————————————————————— 版本信息 ————————————————————————")
            Log.e(TAG, "isDebug: ${ZixieContext.isDebug()} ;isOfficial: ${ZixieContext.isOfficial()}")
            Log.e(TAG, "tag: ${ZixieContext.getVersionTag()}")
            Log.e(TAG, "version: ${ZixieContext.getVersionNameAndCode()}")
            Log.e(TAG, "APPInstalledTime: ${ZixieContext.getAPPInstalledTime()} ;VersionInstalledTime: ${ZixieContext.getAPPLastVersionInstalledTime()}")
            Log.e(TAG, "———————————————————————— 版本信息 ————————————————————————")
            if (skipPrivacy) {
                AgreementPrivacy.doAgreedPrivacy()
            }
            initScreenWidthAndHeight()
            // 初始化渠道号
            initZixieLibs(application, !ZixieContext.isOfficial())
        }
    }

    private fun initScreenWidthAndHeight() {
        var width = DisplayUtil.getRealScreenSizeX(ZixieContext.applicationContext)
        var height = DisplayUtil.getRealScreenSizeY(ZixieContext.applicationContext)
        ZixieContext.screenWidth = Math.min(width, height)
        ZixieContext.screenHeight = Math.max(width, height)
    }

    private fun initZixieLibs(application: Application, isDebug: Boolean) {
        LifecycleHelper.init(application)
    }


    fun initUserLoginRetBeforeGetUser(platform: Int, openid: String) {
        ZLog.d("initUserLoginRetBeforeGetUser in JYGame:$openid")
    }

    fun initAfterAgreePrivacy(application: Application) {

    }
}