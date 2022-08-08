package com.bihe0832.android.lib.lifecycle

import android.app.Application
import android.arch.lifecycle.ProcessLifecycleOwner
import android.content.Context
import com.bihe0832.android.lib.aaf.tools.AAFException
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.time.DateUtil

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020/10/30.
 * Description: Description
 *
 */
const val KEY_APP_INSTALLED_TIME = "zixieAppInstalledTime"
const val KEY_VERSION_INSTALL_TIME = "zixieVersionInstalledTime"

const val KEY_LAST_STARTED_VERSION = "zixieLastStartedVersion"
const val KEY_LAST_START_TIME = "zixieAppLastStartTime"

const val KEY_USED_DAYS = "zixieUsedDays"
const val KEY_USED_TIMES = "zixieUsedTimes"


const val INSTALL_TYPE_NOT_FIRST = 1
const val INSTALL_TYPE_VERSION_FIRST = 2
const val INSTALL_TYPE_APP_FIRST = 3

object LifecycleHelper {

    private var applicationContext: Context? = null
    private var hasInit = false

    private var lastStartVersion = 0L
    private var lastStartTime = 0L
    private var currentStartTime = 0L
    private var recordUsedInfo = true
    private val currentVersion by lazy {
        APKUtils.getAppVersionCode(applicationContext)
    }

    private var currentTimeInterface: ZixieTimeInterface? = null

    interface ZixieTimeInterface {
        fun getCurrentTime(): Long
    }

    @Synchronized
    fun init(application: Application) {
        init(application, true, null)
    }

    @Synchronized
    fun init(application: Application, timeInterface: ZixieTimeInterface?) {
        init(application, true, timeInterface)
    }

    @Synchronized
    fun init(application: Application, needRecord: Boolean, timeInterface: ZixieTimeInterface?) {
        applicationContext = application.applicationContext
        currentTimeInterface = timeInterface
        recordUsedInfo = needRecord
        if (!hasInit) {
            currentStartTime = getCurrentTime()
            ProcessLifecycleOwner.get().lifecycle.addObserver(ApplicationObserver)
            application.registerActivityLifecycleCallbacks(ActivityObserver)
            if (this.recordUsedInfo) {
                if (Config.hasInit()) {
                    lastStartVersion = Config.readConfig(KEY_LAST_STARTED_VERSION, 0L)
                    lastStartTime = Config.readConfig(KEY_LAST_START_TIME, 0L)
                    updateNewVersion()
                    updateUsedInfo()
                } else {
                    throw AAFException("please please init Config module first")
                }
            }
        }
    }

    fun getCurrentTime(): Long {
        return currentTimeInterface?.getCurrentTime() ?: System.currentTimeMillis()
    }

    fun getAPPCurrentStartTime(): Long {
        return currentStartTime
    }

    private var hasUpdatedStartTime = false
    fun updateAPPCurrentStartTime(offset: Long) {
        currentStartTime += offset
        if (hasUpdatedStartTime) {
            throw AAFException("please check start time has updated more once")
        }
        hasUpdatedStartTime = true
    }

    fun getAPPInstalledTime(): Long {
        return doActionWithCheckReturnLong {
            Config.readConfig(KEY_APP_INSTALLED_TIME, 0L)
        }
    }

    fun getVersionInstalledTime(): Long {
        return doActionWithCheckReturnLong {
            Config.readConfig(KEY_VERSION_INSTALL_TIME, getCurrentTime())
        }
    }

    fun getAPPLastStartTime(): Long {
        return doActionWithCheckReturnLong {
            lastStartTime
        }
    }

    fun getAPPLastVersion(): Long {
        return doActionWithCheckReturnLong {
            lastStartVersion
        }
    }

    fun getAPPUsedDays(): Int {
        return doActionWithCheckReturnLong {
            Config.readConfig(KEY_USED_DAYS, 0L)
        }.toInt()
    }

    fun getAPPUsedTimes(): Int {
        return doActionWithCheckReturnLong {
            Config.readConfig(KEY_USED_TIMES, 0L)
        }.toInt()
    }

    fun getCurrentVersionUsedTimes(): Int {
        return doActionWithCheckReturnLong {
            Config.readConfig(KEY_USED_TIMES + currentVersion, 0L)
        }.toInt()
    }

    private fun updateNewVersion() {
        doActionWithCheck {
            if (currentVersion != lastStartVersion) {
                Config.writeConfig(KEY_LAST_STARTED_VERSION, currentVersion)
                Config.writeConfig(KEY_VERSION_INSTALL_TIME, getCurrentTime())
                if (lastStartVersion < 1) {
                    Config.writeConfig(KEY_APP_INSTALLED_TIME, getCurrentTime())
                }
            }
        }
    }

    private fun updateUsedInfo() {
        doActionWithCheck {
            Config.writeConfig(KEY_LAST_START_TIME, currentStartTime)
            addValueOnce(KEY_USED_TIMES)
            addValueOnce(KEY_USED_TIMES + currentVersion)
            if (DateUtil.getDateEN(lastStartTime, "yyyy-MM-dd") != DateUtil.getDateEN(getCurrentTime(), "yyyy-MM-dd")) {
                addValueOnce(KEY_USED_DAYS)
            }
        }
    }

    val isFirstStart by lazy {
        if (currentVersion != lastStartVersion) {
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

    private fun doActionWithCheck(action: () -> Unit) {
        if (recordUsedInfo) {
            action()
        } else {
            throw AAFException("LifecycleHelper has closed record used info")
        }
    }

    private fun doActionWithCheckReturnLong(action: () -> Long): Long {
        if (recordUsedInfo) {
            return action()
        } else {
            throw AAFException("LifecycleHelper has closed record used info")
        }
    }
}