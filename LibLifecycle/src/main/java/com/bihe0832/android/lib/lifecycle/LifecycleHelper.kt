package com.bihe0832.android.lib.lifecycle

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import com.bihe0832.android.lib.aaf.tools.AAFException
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.time.DateUtil

/**
 * 应用生命周期管理助手
 *
 * 提供应用生命周期相关的统一管理功能，包括：
 * - 应用安装时间、版本安装时间记录
 * - 应用启动次数、使用天数统计
 * - 首次启动类型判断（首次安装/版本更新/普通启动）
 * - 自定义时间接口支持
 *
 * 使用方式：
 * ```kotlin
 * // 在 Application.onCreate 中初始化
 * LifecycleHelper.init(application)
 *
 * // 或使用自定义时间接口（如服务器时间）
 * LifecycleHelper.init(application, object : ZixieTimeInterface {
 *     override fun getCurrentTime() = serverTime
 * })
 *
 * // 获取应用信息
 * val installTime = LifecycleHelper.getAPPInstalledTime()
 * val usedDays = LifecycleHelper.getAPPUsedDays()
 * val isFirst = LifecycleHelper.isFirstStart
 * ```
 *
 * 注意：初始化前需要先初始化 Config 模块
 *
 * @author zixie code@bihe0832.com
 * Created on 2020/10/30.
 */

/** 应用首次安装时间的存储 Key */
const val KEY_APP_INSTALLED_TIME = "zixieAppInstalledTime"

/** 当前版本安装时间的存储 Key */
const val KEY_VERSION_INSTALL_TIME = "zixieVersionInstalledTime"

/** 上次启动的版本号存储 Key */
const val KEY_LAST_STARTED_VERSION = "zixieLastStartedVersion"

/** 上次启动时间的存储 Key */
const val KEY_LAST_START_TIME = "zixieAppLastStartTime"

/** 应用使用天数的存储 Key */
const val KEY_USED_DAYS = "zixieUsedDays"

/** 应用启动次数的存储 Key */
const val KEY_USED_TIMES = "zixieUsedTimes"


/** 启动类型：非首次启动（普通启动） */
const val INSTALL_TYPE_NOT_FIRST = 1

/** 启动类型：版本首次启动（版本更新后首次启动） */
const val INSTALL_TYPE_VERSION_FIRST = 2

/** 启动类型：应用首次启动（首次安装后启动） */
const val INSTALL_TYPE_APP_FIRST = 3

object LifecycleHelper {

    /** 应用上下文 */
    private var applicationContext: Context? = null

    /** 是否已初始化 */
    private var hasInit = false

    /** 上次启动的版本号 */
    private var lastStartVersion = 0L

    /** 上次启动时间戳 */
    private var lastStartTime = 0L

    /** 本次启动时间戳 */
    private var currentStartTime = 0L

    /** 是否记录使用信息 */
    private var recordUsedInfo = true

    /** 当前应用版本号，延迟初始化 */
    private val currentVersion by lazy {
        APKUtils.getAppVersionCode(applicationContext)
    }

    /** 自定义时间接口 */
    private var currentTimeInterface: ZixieTimeInterface? = null

    /**
     * 自定义时间接口
     *
     * 用于提供自定义的时间获取方式，如使用服务器时间而非本地时间
     */
    interface ZixieTimeInterface {
        /**
         * 获取当前时间戳
         *
         * @return 当前时间戳（毫秒）
         */
        fun getCurrentTime(): Long
    }

    /**
     * 初始化生命周期助手（使用默认配置）
     *
     * 使用系统时间，并记录使用信息
     *
     * @param application Application 实例
     * @throws AAFException 如果 Config 模块未初始化
     */
    @Synchronized
    fun init(application: Application) {
        init(application, true, null)
    }

    /**
     * 初始化生命周期助手（使用自定义时间接口）
     *
     * @param application Application 实例
     * @param timeInterface 自定义时间接口，用于获取时间（如服务器时间）
     * @throws AAFException 如果 Config 模块未初始化
     */
    @Synchronized
    fun init(application: Application, timeInterface: ZixieTimeInterface?) {
        init(application, true, timeInterface)
    }

    /**
     * 初始化生命周期助手（完整配置）
     *
     * 注册 ProcessLifecycleOwner 和 ActivityLifecycleCallbacks，
     * 初始化应用使用统计信息
     *
     * @param application Application 实例
     * @param needRecord 是否记录使用信息（启动次数、使用天数等）
     * @param timeInterface 自定义时间接口，传 null 使用系统时间
     * @throws AAFException 如果 needRecord 为 true 但 Config 模块未初始化
     */
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

    /**
     * 获取当前时间
     *
     * 如果设置了自定义时间接口则使用自定义时间，否则使用系统时间
     *
     * @return 当前时间戳（毫秒）
     */
    fun getCurrentTime(): Long {
        return currentTimeInterface?.getCurrentTime() ?: System.currentTimeMillis()
    }

    /**
     * 获取本次应用启动时间
     *
     * @return 本次启动时的时间戳（毫秒）
     */
    fun getAPPCurrentStartTime(): Long {
        return currentStartTime
    }

    /** 是否已更新过启动时间 */
    private var hasUpdatedStartTime = false

    /**
     * 更新应用启动时间偏移量
     *
     * 用于校正启动时间（如根据服务器时间校正），只能调用一次
     *
     * @param offset 时间偏移量（毫秒）
     * @throws AAFException 如果已经更新过启动时间
     */
    fun updateAPPCurrentStartTime(offset: Long) {
        currentStartTime += offset
        if (hasUpdatedStartTime) {
            throw AAFException("please check start time has updated more once")
        }
        hasUpdatedStartTime = true
    }

    /**
     * 获取应用首次安装时间
     *
     * @return 应用首次安装时的时间戳（毫秒），如果未记录则返回 0
     * @throws AAFException 如果未开启使用信息记录
     */
    fun getAPPInstalledTime(): Long {
        return doActionWithCheckReturnLong {
            Config.readConfig(KEY_APP_INSTALLED_TIME, 0L)
        }
    }

    /**
     * 获取当前版本安装时间
     *
     * @return 当前版本安装时的时间戳（毫秒）
     * @throws AAFException 如果未开启使用信息记录
     */
    fun getVersionInstalledTime(): Long {
        return doActionWithCheckReturnLong {
            Config.readConfig(KEY_VERSION_INSTALL_TIME, getCurrentTime())
        }
    }

    /**
     * 获取应用上次启动时间
     *
     * @return 上次启动时的时间戳（毫秒），首次启动返回 0
     * @throws AAFException 如果未开启使用信息记录
     */
    fun getAPPLastStartTime(): Long {
        return doActionWithCheckReturnLong {
            lastStartTime
        }
    }

    /**
     * 获取应用上次启动的版本号
     *
     * @return 上次启动时的版本号，首次启动返回 0
     * @throws AAFException 如果未开启使用信息记录
     */
    fun getAPPLastVersion(): Long {
        return doActionWithCheckReturnLong {
            lastStartVersion
        }
    }

    /**
     * 获取应用使用天数
     *
     * @return 应用使用的天数（不同日期启动计为不同天）
     * @throws AAFException 如果未开启使用信息记录
     */
    fun getAPPUsedDays(): Int {
        return doActionWithCheckReturnLong {
            Config.readConfig(KEY_USED_DAYS, 0L)
        }.toInt()
    }

    /**
     * 获取应用总启动次数
     *
     * @return 应用累计启动次数
     * @throws AAFException 如果未开启使用信息记录
     */
    fun getAPPUsedTimes(): Int {
        return doActionWithCheckReturnLong {
            Config.readConfig(KEY_USED_TIMES, 0L)
        }.toInt()
    }

    /**
     * 获取当前版本启动次数
     *
     * @return 当前版本累计启动次数
     * @throws AAFException 如果未开启使用信息记录
     */
    fun getCurrentVersionUsedTimes(): Int {
        return doActionWithCheckReturnLong {
            Config.readConfig(KEY_USED_TIMES + currentVersion, 0L)
        }.toInt()
    }

    /**
     * 更新版本信息
     *
     * 如果版本号变化，记录新版本安装时间；如果是首次安装，记录应用安装时间
     */
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

    /**
     * 更新使用信息
     *
     * 记录启动时间，增加启动次数，如果是新的一天则增加使用天数
     */
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

    /**
     * 判断本次启动类型
     *
     * 延迟计算，返回值为以下常量之一：
     * - [INSTALL_TYPE_APP_FIRST]: 应用首次安装后启动
     * - [INSTALL_TYPE_VERSION_FIRST]: 版本更新后首次启动
     * - [INSTALL_TYPE_NOT_FIRST]: 普通启动（非首次）
     */
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


    /**
     * 将指定 Key 的值加 1
     *
     * @param key 存储 Key
     */
    private fun addValueOnce(key: String) {
        Config.writeConfig(key, Config.readConfig(key, 0) + 1)
    }

    /**
     * 检查是否开启使用信息记录后执行操作
     *
     * @param action 要执行的操作
     * @throws AAFException 如果未开启使用信息记录
     */
    private fun doActionWithCheck(action: () -> Unit) {
        if (recordUsedInfo) {
            action()
        } else {
            throw AAFException("LifecycleHelper has closed record used info")
        }
    }

    /**
     * 检查是否开启使用信息记录后执行操作并返回 Long 值
     *
     * @param action 要执行的操作
     * @return 操作返回的 Long 值
     * @throws AAFException 如果未开启使用信息记录
     */
    private fun doActionWithCheckReturnLong(action: () -> Long): Long {
        if (recordUsedInfo) {
            return action()
        } else {
            throw AAFException("LifecycleHelper has closed record used info")
        }
    }
}