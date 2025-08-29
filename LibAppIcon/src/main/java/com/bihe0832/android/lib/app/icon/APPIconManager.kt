package com.bihe0832.android.lib.app.icon

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
import android.text.TextUtils
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

    private const val TAG = "APPIconManager"

    fun changeAppIcon(context: Context, enableAlias: String, allAlias: List<String>, delay: Long) {
        if (TextUtils.isEmpty(enableAlias)) {
            ZLog.d(TAG, "changeAppIcon: skip, enableAlias is empty")
        } else {
            changeAppIconState(
                context,
                listOf(enableAlias),
                allAlias.filter { !enableAlias.contains(it) },
                delay
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
                        ZLog.d(TAG, "enableAlias:" + enableAlias.joinToString(","))
                        ZLog.d(TAG, "disableAlias:" + disableAlias.joinToString(","))
                        val needChangeWhenBack = needChange(context, enableAlias, disableAlias)
                        ZLog.d(TAG, "needChangeWhenBack:$needChangeWhenBack")
                        if (needChangeWhenBack) {
                            changeAppIconAction(
                                context.applicationContext,
                                enableAlias,
                                disableAlias
                            )
                            getAliasState(context.applicationContext, enableAlias).forEach {
                                ZLog.d(TAG, "enableAlias:" + it.key + " state:" + it.value)
                            }
                            getAliasState(context.applicationContext, disableAlias).forEach {
                                ZLog.d(TAG, "disableAlias:" + it.key + " state:" + it.value)
                            }
                            ZLog.d(TAG, "remove Listener after change")
                            ApplicationObserver.removeStatusChangeListener(this)
                        } else {
                            ZLog.d(TAG, "remove Listener no change")
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

    private fun needChange(
        context: Context,
        enableAlias: List<String>,
        disableAlias: List<String>
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
                    ZLog.d(TAG, "disableAlias needChange: $it is " + it.value)
                }
            }

        }
        return false
    }

    private fun changeAppIconAction(
        context: Context,
        enableAlias: List<String>,
        disableAlias: List<String>
    ) {
        val pm = context.packageManager
        val packageName = context.packageName

        val enableData = getAliasState(context.applicationContext, enableAlias)
        enableData.forEach {
            when (it.value) {
                COMPONENT_ENABLED_STATE_ENABLED -> {
                    ZLog.d(TAG, "enableAlias: $it has Enabled")
                }

                else -> {
                    pm.setComponentEnabledSetting(
                        ComponentName(packageName, it.key),
                        COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }
        }
        val disableData = getAliasState(context.applicationContext, disableAlias)
        disableData.forEach {
            when (it.value) {
                COMPONENT_ENABLED_STATE_ENABLED, COMPONENT_ENABLED_STATE_DEFAULT -> {
                    pm.setComponentEnabledSetting(
                        ComponentName(packageName, it.key),
                        COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }

                else -> {
                    ZLog.d(TAG, "disableAlias: $it is " + it.value)
                }
            }
        }
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