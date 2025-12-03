package com.bihe0832.android.framework


import android.app.Application
import android.content.Context
import android.util.Log
import com.bihe0832.android.framework.constant.Constants
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.framework.privacy.AgreementPrivacy
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.language.MultiLanguageHelper
import com.bihe0832.android.lib.lifecycle.LifecycleHelper
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.utils.os.DisplayUtil
import com.bihe0832.android.lib.utils.time.DateUtil
import java.util.Locale


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2019-07-09.
 * Description: 基础模块
 *
 */

object ZixieCoreInit {
    // 全局变量的初始化
    var hasInit = false
    const val TAG = "ZixieCoreInit"

    //目前仅仅主进程和web进程需要初始化
    @Synchronized
    fun initCore(
        application: Application,
        appIsDebug: Boolean,
        appIsOfficial: Boolean,
        skipPrivacy: Boolean,
        appTag: String,
        supportMultiLanguage: Boolean
    ) {
        ZixieContext.init(application, appIsDebug, appIsOfficial, appTag, supportMultiLanguage)
        if (!hasInit) {
            hasInit = true
            initLog(application, !ZixieContext.isOfficial())
            // 初始化配置管理
            Config.init(
                ZixieContext.applicationContext,
                Constants.CONFIG_COMMON_FILE_NAME,
                !ZixieContext.isOfficial()
            )
            Config.loadLoaclFile(
                application,
                Constants.CONFIG_SPECIAL_FILE_NAME,
                !ZixieContext.isOfficial()
            )
            ThemeResourcesManager.init(ZixieContext.application!!)
            Log.e(TAG, "———————————————————————— 版本信息 ————————————————————————")
            Log.e(
                TAG,
                "isDebug: ${ZixieContext.isDebug()} ;isOfficial: ${ZixieContext.isOfficial()}"
            )
            Log.e(TAG, "tag: ${ZixieContext.getVersionTag()}")
            Log.e(TAG, "version: ${ZixieContext.getVersionNameAndCode()}")
            Log.e(
                TAG,
                "APPInstalledTime: ${DateUtil.getDateEN(ZixieContext.getAPPInstalledTime())} ;VersionInstalledTime: ${
                    DateUtil.getDateEN(ZixieContext.getAPPLastVersionInstalledTime())
                }"
            )
            Log.e(TAG, "———————————————————————— 版本信息 ————————————————————————")
            if (skipPrivacy) {
                AgreementPrivacy.doAgreedPrivacy()
            }
            initScreenWidthAndHeight()
            // 初始化渠道号
            initZixieLibs(application, !ZixieContext.isOfficial())
        }
    }

    private fun initLog(application: Application, openLog: Boolean) {

        val logFileEnabled = enableLogByFile()
        val logEnable = openLog || logFileEnabled
        Log.e(TAG, "log enable: $openLog  $logFileEnabled $logEnable ")
        ZLog.setDebug(logEnable)
        ZLog.setLogLineLength(1500)
        LoggerFile.init(application, logEnable)
    }

    fun enableLogByFile(): Boolean {
        Log.e(TAG, "log enable: isFileEnabled, $${ZixieContext.getZixieFolder()} ")
        return FileUtils.checkFileExist("${ZixieContext.getZixieFolder()}logFileEnabled")
    }

    private fun initScreenWidthAndHeight() {
        var width = DisplayUtil.getRealScreenSizeX(ZixieContext.applicationContext)
        var height = DisplayUtil.getRealScreenSizeY(ZixieContext.applicationContext)
        ZixieContext.screenWidth = Math.min(width, height)
        ZixieContext.screenHeight = Math.max(width, height)
    }

    private fun initZixieLibs(application: Application, isDebug: Boolean) {
        LifecycleHelper.init(application, object : LifecycleHelper.ZixieTimeInterface {
            override fun getCurrentTime(): Long {
                return System.currentTimeMillis()
            }
        })
    }

    fun initUserLoginRetBeforeGetUser(platform: Int, openid: String) {
        ZLog.d("initUserLoginRetBeforeGetUser in JYGame:$openid")
    }

    fun initAfterAgreePrivacy(application: Application) {

    }

    fun updateApplicationLocale(context: Context, locale: Locale) {
        MultiLanguageHelper.setLanguageConfig(context, locale)
        ZixieContext.updateApplicationContext(context, true)
    }
}