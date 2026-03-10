package com.bihe0832.android.lib.app.icon

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
import android.text.TextUtils
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import java.util.concurrent.ConcurrentHashMap


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/8/19.
 * Description: Description
 *
 */
object APPIconManager {

    const val TAG = "APPIconManager"
    private const val KEY_PENDING_ENABLE = "app_icon_pending_enable_alias"
    private const val KEY_PENDING_DISABLE = "app_icon_pending_disable_alias"

    fun init(context: Context) {
        applyPendingChange(context)
    }

    fun changeAppIcon(context: Context, enableAlias: String, allAlias: List<String>, delay: Long) {
        if (TextUtils.isEmpty(enableAlias)) {
            ZLog.d(TAG, "changeAppIcon: skip, enableAlias is empty")
        } else {
            changeAppIconState(
                context, listOf(enableAlias), allAlias.filter { !enableAlias.contains(it) }, delay
            )
        }
    }

    fun changeAppIconState(
        context: Context,
        enableAlias: List<String>,
        disableAlias: List<String>,
        times: Long
    ) {
        ZLog.d(TAG, "enableAlias:" + enableAlias.joinToString(","))
        ZLog.d(TAG, "disableAlias:" + disableAlias.joinToString(","))
        val needChange = needChange(context, enableAlias, disableAlias)
        ZLog.d(TAG, "needChange:$needChange")
        if (needChange) {
            ZLog.d(TAG, "enableAlias add Listener")
            ApplicationObserver.addStatusChangeListener(object :
                ApplicationObserver.APPStatusChangeListener {
                override fun onBackground() {
                    ZLog.d(TAG, "onBackground")
                    ThreadManager.getInstance().start({
                        val result = doChangeAppIconState(context, enableAlias, disableAlias)
                        if (result) {
                            ApplicationObserver.removeStatusChangeListener(this)
                        }
                    }, times)
                }

                override fun onForeground() {
                    ZLog.d(TAG, "onForeground")
                }
            })
        } else {
            ZLog.d(TAG, "enableAlias skip")
        }
    }


    fun doChangeAppIconState(
        context: Context,
        enableAlias: List<String>,
        disableAlias: List<String>
    ): Boolean {
        ZLog.d(TAG, "enableAlias:" + enableAlias.joinToString(","))
        ZLog.d(TAG, "disableAlias:" + disableAlias.joinToString(","))
        val needChangeWhenBack = needChange(context, enableAlias, disableAlias)
        ZLog.d(TAG, "needChangeWhenBack:$needChangeWhenBack")
        val currentIsBackground = ApplicationObserver.isAPPBackground()
        if (!currentIsBackground) {
            ZLog.d(TAG, "app go front when change icon")
            return false
        }

        if (!needChangeWhenBack) {
            ZLog.d(TAG, "remove Listener no change")
            return true
        }

        // 有活跃 Task 时，记录待切换状态，延迟到下次 init() 再执行，避免 cleanupDisabledPackageComponents 杀掉 Task
        if (disableAlias.isNotEmpty() && hasActiveTasks(context)) {
            ZLog.d(TAG, "hasActiveTasks, save pending icon change to next init")
            savePendingChange(context, enableAlias, disableAlias)
            return true
        }

        changeAppIconAction(
            context.applicationContext, enableAlias, disableAlias
        )
        clearPendingChange(context)
        getAliasState(context.applicationContext, enableAlias).forEach {
            ZLog.d(TAG, "enableAlias:" + it)
        }
        getAliasState(context.applicationContext, disableAlias).forEach {
            ZLog.d(TAG, "disableAlias:" + it)
        }
        ZLog.d(TAG, "remove Listener after change")
        return true
    }

    /**
     * 检查当前应用是否有活跃的 Task
     * 用于避免在有活跃 Task 时执行 alias disable 操作，
     * 防止系统 cleanupDisabledPackageComponents 移除 Task 导致 Activity 被销毁
     */
    private fun hasActiveTasks(context: Context): Boolean {
        return try {
            val am =
                context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val hasTask = am?.appTasks?.isNotEmpty() == true
            ZLog.d(TAG, "hasActiveTasks: $hasTask, taskCount: ${am?.appTasks?.size ?: 0}")
            hasTask
        } catch (e: Exception) {
            ZLog.e(TAG, "hasActiveTasks error: ${e.message}")
            true // 出错时保守处理，认为有活跃 Task
        }
    }

    private fun needChange(
        context: Context, enableAlias: List<String>, disableAlias: List<String>
    ): Boolean {
        val enableData = getAliasState(context.applicationContext, enableAlias)
        enableData.forEach {
            when (it.value) {
                COMPONENT_ENABLED_STATE_ENABLED -> {
                    ZLog.d(TAG, "enableAlias needChange: $it has Enabled")
                }

                else -> {
                    return true
                }
            }
        }
        val disableData = getAliasState(context.applicationContext, disableAlias)
        disableData.forEach {
            when (it.value) {
                COMPONENT_ENABLED_STATE_ENABLED, COMPONENT_ENABLED_STATE_DEFAULT -> {
                    ZLog.d(TAG, "disableAlias needChange: $it has Enabled")
                    return true
                }

                else -> {
                    ZLog.d(TAG, "disableAlias needChange: $it")
                }
            }

        }
        return false
    }

    private fun changeAppIconAction(
        context: Context, enableAlias: List<String>, disableAlias: List<String>
    ) {
        ZLog.d(TAG, "changeAppIconAction start")
        val pm = context.packageManager
        val packageName = context.packageName

        val enableData = getAliasState(context.applicationContext, enableAlias)
        enableData.forEach {
            when (it.value) {
                COMPONENT_ENABLED_STATE_ENABLED -> {
                    ZLog.d(TAG, "enableAlias: $it has Enabled")
                }

                else -> {
                    try {
                        pm.setComponentEnabledSetting(
                            ComponentName(packageName, it.key),
                            COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        ZLog.e(TAG, "enable Alias $it catch exception $e")
                    }

                }
            }
        }
        val disableData = getAliasState(context.applicationContext, disableAlias)
        disableData.forEach {
            when (it.value) {
                COMPONENT_ENABLED_STATE_ENABLED, COMPONENT_ENABLED_STATE_DEFAULT -> {
                    try {
                        pm.setComponentEnabledSetting(
                            ComponentName(packageName, it.key),
                            COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        ZLog.e(TAG, "disable Alias $it catch exception $e")
                    }
                }

                else -> {
                    ZLog.d(TAG, "disableAlias: $it")
                }
            }
        }
        ZLog.d(TAG, "changeAppIconAction end")
    }

    private fun ensureConfigInit(context: Context): Boolean {
        return try {
            if (!Config.hasInit()) {
                Config.init(context.applicationContext, "", false)
            }
            true
        } catch (e: Exception) {
            ZLog.e(TAG, "ensureConfigInit error: ${e.message}")
            false
        }
    }

    private fun savePendingChange(
        context: Context,
        enableAlias: List<String>,
        disableAlias: List<String>
    ) {
        if (!ensureConfigInit(context)) {
            return
        }
        val enableValue = enableAlias.joinToString(",")
        val disableValue = disableAlias.joinToString(",")
        Config.writeConfig(KEY_PENDING_ENABLE, enableValue, true)
        Config.writeConfig(KEY_PENDING_DISABLE, disableValue, true)
        ZLog.d(TAG, "savePendingChange enable:$enableValue disable:$disableValue")
    }

    private fun clearPendingChange(context: Context) {
        if (!ensureConfigInit(context)) {
            return
        }
        Config.writeConfig(KEY_PENDING_ENABLE, "", true)
        Config.writeConfig(KEY_PENDING_DISABLE, "", true)
    }

    private fun applyPendingChange(context: Context) {
        if (!ensureConfigInit(context)) {
            return
        }
        val enableValue = Config.readConfig(KEY_PENDING_ENABLE, "")
        val disableValue = Config.readConfig(KEY_PENDING_DISABLE, "")
        val enableList = enableValue.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val disableList = disableValue.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (enableList.isEmpty() && disableList.isEmpty()) {
            ZLog.d(TAG, "applyPendingChange: no pending data")
            return
        }
        ZLog.d(TAG, "applyPendingChange enable:${enableList.joinToString(",")} disable:${disableList.joinToString(",")}")
        changeAppIconAction(context.applicationContext, enableList, disableList)
        clearPendingChange(context)
    }

    fun getAliasState(context: Context, aliases: List<String>): ConcurrentHashMap<String, Int> {
        val pm = context.packageManager
        val data = ConcurrentHashMap<String, Int>()
        aliases.forEach {
            val state = pm.getComponentEnabledSetting(ComponentName(context, it))
            data[it] = state
        }
        return data
    }
}