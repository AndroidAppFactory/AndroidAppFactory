package com.bihe0832.android.lib.lifecycle

import android.app.Application
import android.arch.lifecycle.ProcessLifecycleOwner
import android.content.Context
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.utils.DateUtil
import com.bihe0832.android.lib.utils.apk.APKUtils

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020/10/30.
 * Description: Description
 *
 */
const val KEY_APP_INSTALLED_TIME = "zixieAppInstalledTime"
const val KEY_LAST_STARTED_VERSION_INSTALL_TIME = "zixieLastStartedVersionInstalledTime"

const val KEY_LAST_STARTED_VERSION = "zixieLastStartedVersion"
const val KEY_LAST_START_TIME = "zixieAppLastStartTime"

const val KEY_USED_DAYS = "zixieUsedDays"
const val KEY_USED_TIMES = "zixieUsedTimes"


const val INSTALL_TYPE_NOT_FIRST = 1
const val INSTALL_TYPE_VERSION_FIRST = 2
const val INSTALL_TYPE_APP_FIRST = 3

object LifecycleHelper {

    var applicationContext: Context? = null
        private set

    private var lastStartVersion = 0L
    private var lastStartTime = 0L
    private var lastVersionInstallTime = 0L

    @Synchronized
    fun init(application: Application) {
        applicationContext = application.applicationContext
        ProcessLifecycleOwner.get().lifecycle.addObserver(ApplicationObserver)
        application.registerActivityLifecycleCallbacks(ActivityObserver)

        lastStartVersion = Config.readConfig(KEY_LAST_STARTED_VERSION, 0L)
        lastStartTime = Config.readConfig(KEY_LAST_START_TIME, 0L)
        lastVersionInstallTime = Config.readConfig(KEY_LAST_STARTED_VERSION_INSTALL_TIME, 0L)

        updateNewVersion()
        updateUsedInfo()
    }

    fun getAPPLastVersionInstalledTime(): Long {
        return lastVersionInstallTime
    }

    fun getAPPInstalledTime(): Long {
        return Config.readConfig(KEY_APP_INSTALLED_TIME, 0L)
    }

    fun getAPPLastStartTime(): Long {
        return lastStartTime
    }

    fun getAPPUsedDays(): Int {
        return Config.readConfig(KEY_USED_DAYS, 0)
    }

    fun getAPPUsedTimes(): Int {
        return Config.readConfig(KEY_USED_TIMES, 0)
    }

    private fun updateNewVersion() {
        if (APKUtils.getAppVersionCode(applicationContext) != lastStartVersion) {
            Config.writeConfig(KEY_LAST_STARTED_VERSION, APKUtils.getAppVersionCode(applicationContext))
            Config.writeConfig(KEY_LAST_STARTED_VERSION_INSTALL_TIME, System.currentTimeMillis())
            if (lastStartVersion < 1) {
                Config.writeConfig(KEY_APP_INSTALLED_TIME, System.currentTimeMillis())
            }
        }
    }


    private fun updateUsedInfo() {
        Config.writeConfig(KEY_LAST_START_TIME, System.currentTimeMillis())
        addValueOnce(KEY_USED_TIMES)
        if (DateUtil.getDateEN(lastStartTime, "yyyy-MM-dd") != DateUtil.getDateEN(System.currentTimeMillis(), "yyyy-MM-dd")) {
            addValueOnce(KEY_USED_DAYS)
        }
    }

    val isFirstStart by lazy {
        if (APKUtils.getAppVersionCode(applicationContext) != lastStartVersion) {
            if (lastStartVersion > 0) {
                INSTALL_TYPE_VERSION_FIRST
            } else {
                INSTALL_TYPE_APP_FIRST
            }
        } else {
            INSTALL_TYPE_NOT_FIRST
        }
    }


    private fun addValueOnce(key: String) {
        Config.writeConfig(key, Config.readConfig(key, 0) + 1)
    }
}