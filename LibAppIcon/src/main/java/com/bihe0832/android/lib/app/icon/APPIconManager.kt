package com.bihe0832.android.lib.app.icon

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.log.ZLog
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
    private var mAPPStatusChangeListener: ApplicationObserver.APPStatusChangeListener? = null

    init {
        ApplicationObserver.addDestoryListener(object : ApplicationObserver.APPDestroyListener {
            override fun onAllActivityDestroyed() {
                mAPPStatusChangeListener?.let {
                    ApplicationObserver.removeStatusChangeListener(it)
                }
                ApplicationObserver.removeDestoryListener(this)
            }

        })
    }

    fun changeAppIcon(context: Context, enableAlias: String, allAlias: List<String>) {
        changeAppIcon(context, listOf(enableAlias), allAlias)
    }

    fun changeAppIcon(context: Context, enableAlias: List<String>, allAlias: List<String>) {
        ZLog.d(TAG, "enableAlias:" + enableAlias.joinToString(","))
        ZLog.d(TAG, "allAlias:" + allAlias.joinToString(","))
        var needChange = false
        val data = getAliasState(context.applicationContext, allAlias)
        enableAlias.forEach {
            if (data.containsKey(it) && data[it] == COMPONENT_ENABLED_STATE_ENABLED) {
                ZLog.d(TAG, "enableAlias: $it has Enabled")
            } else {
                needChange = true
            }
        }
        if (needChange) {
            ZLog.d(TAG, "enableAlias add Listener")
            mAPPStatusChangeListener = object : ApplicationObserver.APPStatusChangeListener {
                override fun onBackground() {
                    ZLog.d(TAG, "onBackground")
                    ZLog.d(TAG, "enableAlias:" + enableAlias.joinToString(","))
                    ZLog.d(TAG, "allAlias:" + allAlias.joinToString(","))
                    if (enableAlias.isNotEmpty()) {
                        changeAppIconAction(context.applicationContext, enableAlias, allAlias)
                        getAliasState(context.applicationContext, allAlias).forEach {
                            ZLog.d(TAG, it.key + " state:" + it.value)
                        }
                    }
                }

                override fun onForeground() {
                    ZLog.d(TAG, "onForeground")
                }

            }
            mAPPStatusChangeListener?.let {
                ApplicationObserver.addStatusChangeListener(it)
            }
        }else{
            ZLog.d(TAG, "enableAlias skip")
        }


    }

    private fun changeAppIconAction(
        context: Context,
        enableAlias: List<String>,
        allAlias: List<String>
    ) {
        val pm = context.packageManager
        val packageName = context.packageName

        enableAlias.forEach { alias ->
            pm.setComponentEnabledSetting(
                ComponentName(packageName, alias),
                COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }

        allAlias.filter { !enableAlias.contains(it) }.forEach { alias ->
            pm.setComponentEnabledSetting(
                ComponentName(packageName, alias),
                COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
        refreshLauncher(context)
    }

    private fun refreshLauncher(context: Context) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
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