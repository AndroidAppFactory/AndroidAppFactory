package com.bihe0832.android.framework

import android.content.Context
import android.util.Log
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.network.DeviceInfoManager
import com.bihe0832.android.lib.utils.os.DisplayUtil
import com.bihe0832.android.framework.constant.Constants
import com.bihe0832.android.framework.log.LoggerFile


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

    //目前仅仅主进程和web进程需要初始化
    @Synchronized
    fun initCore(ctx: Context) {
        if (!hasInit) {
            hasInit = true
            // 初始化配置管理
            Config.init(ZixieContext.applicationContext, Constants.CONFIG_COMMON_FILE_NAME, !ZixieContext.isOfficial)
            Config.loadLoaclFile(ctx, Constants.CONFIG_SPECIAL_FILE_NAME, !ZixieContext.isOfficial)
            if (!ZixieContext.isOfficial) {
                Log.e(ZixieContext.tag, "———————————————————————— 版本信息 ————————————————————————")
                Log.e(ZixieContext.tag, "isDebug: ${ZixieContext.isDebug} ;isOfficial: ${ZixieContext.isOfficial}")
                Log.e(ZixieContext.tag, "tag: ${ZixieContext.tag}")
                Log.e(ZixieContext.tag, "version: ${ZixieContext.getVersionNameAndCode()}")
                Log.e(ZixieContext.tag, "DeviceId: ${ZixieContext.getDeviceId()}")
                Log.e(ZixieContext.tag, "APPInstalledTime: ${ZixieContext.getAPPInstalledTime()} ;VersionInstalledTime: ${ZixieContext.getVersionInstalledTime()}")
                Log.e(ZixieContext.tag, "———————————————————————— 版本信息 ————————————————————————")
            }

            var width = DisplayUtil.getRealScreenSizeX(ZixieContext.applicationContext)
            var height = DisplayUtil.getRealScreenSizeY(ZixieContext.applicationContext)
            ZixieContext.screenWidth = Math.min(width, height)
            ZixieContext.screenHeight = Math.max(width, height)
            initZixieLibs(ctx, !ZixieContext.isOfficial)
        }
    }

    private fun initZixieLibs(ctx: Context, isDebug: Boolean) {
        DeviceInfoManager.getInstance().init(ctx)
        LoggerFile.init(ctx, isDebug)
    }

    fun initUserLoginRetBeforeGetUser(platform: Int, openid: String) {
        ZLog.d("initUserLoginRetBeforeGetUser in JYGame:$openid")
    }
}