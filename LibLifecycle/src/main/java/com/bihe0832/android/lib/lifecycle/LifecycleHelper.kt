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

    var applicationContext: Context? = null
        private set

    private var lastStartVersion = 0L
    private var lastStartTime = 0L
    private var mRecordUsedInfo = true
    private val mCurrentVersion by lazy {
        APKUtils.getAppVersionCode(applicationContext)
    }

    @Synchronized
    fun init(application: Application) {
        init(application, true)
    }

    @Synchronized
    fun init(application: Application, recordUsedInfo: Boolean) {
        applicationContext = application.applicationContext
        ProcessLifecycleOwner.get().lifecycle.addObserver(ApplicationObserver)
        application.registerActivityLifecycleCallbacks(ActivityObserver)
        mRecordUsedInfo = recordUsedInfo
        if (mRecordUsedInfo) {
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

    fun getAPPInstalledTime(): Long {
        return doActionWithCheckReturnLong {
            Config.readConfig(KEY_APP_INSTALLED_TIME, 0L)
        }
    }

    fun getVersionInstalledTime(): Long {
        return doActionWithCheckReturnLong {
            Config.readConfig(KEY_VERSION_INSTALL_TIME, System.currentTimeMillis())
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
            Config.readConfig(KEY_USED_TIMES + mCurrentVersion, 0L)
        }.toInt()
    }

    private fun updateNewVersion() {
        doActionWithCheck {
            if (mCurrentVersion != lastStartVersion) {
                Config.writeConfig(KEY_LAST_STARTED_VERSION, mCurrentVersion)
                Config.writeConfig(KEY_VERSION_INSTALL_TIME, System.currentTimeMillis())
                if (lastStartVersion < 1) {
                    Config.writeConfig(KEY_APP_INSTALLED_TIME, System.currentTimeMillis())
                }
            }
        }
    }

    private fun updateUsedInfo() {
        doActionWithCheck {
            Config.writeConfig(KEY_LAST_START_TIME, System.currentTimeMillis())
            addValueOnce(KEY_USED_TIMES)
            addValueOnce(KEY_USED_TIMES + mCurrentVersion)
            if (DateUtil.getDateEN(lastStartTime, "yyyy-MM-dd") != DateUtil.getDateEN(System.currentTimeMillis(), "yyyy-MM-dd")) {
                addValueOnce(KEY_USED_DAYS)
            }
        }
    }

    val isFirstStart by lazy {
        if (mCurrentVersion != lastStartVersion) {
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
        if (mRecordUsedInfo) {
            action()
        } else {
            throw AAFException("LifecycleHelper has closed record used info")
        }
    }

    private fun doActionWithCheckReturnLong(action: () -> Long): Long {
        if (mRecordUsedInfo) {
            return action()
        } else {
            throw AAFException("LifecycleHelper has closed record used info")
        }
    }
}