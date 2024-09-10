package com.bihe0832.android.lib.widget

import android.content.Context
import android.text.TextUtils
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.widget.tools.WidgetTools
import com.bihe0832.android.lib.widget.worker.BaseWidgetWorker
import com.bihe0832.android.lib.worker.AAFWorkerManager

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/6/8.
 * Description: 所有 AppWidgetProvider 相关的更新处理逻辑
 *
 */
object WidgetUpdateManager {

    const val TAG = "WidgetUpdateManager"

    private const val WIDGET_WORK_NAME = "WidgetUpdaterWorkaround"
    private const val WIDGET_AUTO_UPDATE_KEY = "WidgetAutoUpdateManager"

    private var mlastUpdateAllTime = 0L

    fun initModuleWithMainProcess(context: Context) {
        updateAllWidgets(context)
    }

    fun enqueueAutoStart(context: Context) {
        cancelAutoStart(context)
        AAFWorkerManager.enqueueOneTimeUniqueWork(context, WIDGET_WORK_NAME, 5, UpdateAllWork::class.java)
    }

    fun cancelAutoStart(context: Context) {
        AAFWorkerManager.cancelUniqueWork(context, WIDGET_WORK_NAME)
    }

    private fun addToAutoUpdateList(clazzName: String) {
        Config.readConfig(WIDGET_AUTO_UPDATE_KEY, "").let {
            if (!it.contains(clazzName)) {
                Config.writeConfig(WIDGET_AUTO_UPDATE_KEY, "$clazzName $it")
            }
        }
    }

    private fun removeFromAutoUpdateList(clazzName: String) {
        Config.readConfig(WIDGET_AUTO_UPDATE_KEY, "").let {
            if (it.contains(clazzName)) {
                it.replace("$clazzName ", "").let { result ->
                    Config.writeConfig(WIDGET_AUTO_UPDATE_KEY, result)
                    if (TextUtils.isEmpty(result)) {
                        WorkManager.getInstance().cancelUniqueWork(WIDGET_WORK_NAME)
                    }
                }
            }
        }
    }

    private fun updateAllWidgets(context: Context, sourceClass: Class<out BaseWidgetWorker>?) {
        ZLog.e(TAG, "updateAll: durtaion is :${System.currentTimeMillis() - mlastUpdateAllTime}")
        // 执行一次任务
        if (System.currentTimeMillis() - mlastUpdateAllTime > 60 * 1000) {
            mlastUpdateAllTime = System.currentTimeMillis()
            AAFWorkerManager.enqueueOneTimeUniqueWork(context, WIDGET_WORK_NAME, 0, UpdateAllWork::class.java)
        } else {
            ZLog.e(TAG, "updateAll: durtaion is less than 60000")
            sourceClass?.let { clazz ->
                AAFWorkerManager.enqueueOneTimeWork(context, clazz)
            }
        }
    }

    class UpdateAllWork(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

        private fun updateByName(name: String) {
            ZLog.d(TAG, "updateByName by $name")
            try {
                if (!name.contains("$")) {
                    (Class.forName(name) as? Class<out Worker>)?.let {
                        AAFWorkerManager.enqueueOneTimeWork(applicationContext, it)
                    }
                } else {
                    ZLog.e(TAG, "!!!!! updateByName by $name error, Bad name !!!!!")
                    removeFromAutoUpdateList(name)
                }
            } catch (e: java.lang.Exception) {
                removeFromAutoUpdateList(name)
                e.printStackTrace()
                ZLog.e(TAG, "updateByName by $name error")
            }
        }

        override fun doWork(): Result {
            ZLog.d(TAG, "do update all work")
            try {
                Config.readConfig(WIDGET_AUTO_UPDATE_KEY, "").split(" ").distinct().forEach {
                    if (!TextUtils.isEmpty(it)) {
                        updateByName(it)
                    }
                }
                return Result.success()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return Result.failure()
        }
    }

    fun updateAllWidgets(context: Context) {
        updateAllWidgets(context, null)
    }

    fun updateWidget(
        context: Context, clazz: Class<out BaseWidgetWorker>, canAutoUpdateByOthers: Boolean, updateAll: Boolean,
    ) {
        ZLog.d(
            TAG, "updateWidget:" + clazz.name + ",canAutoUpdateByOthers: $canAutoUpdateByOthers ; updateAll: $updateAll"
        )
        if (canAutoUpdateByOthers) {
            addToAutoUpdateList(clazz.name)
        } else {
            removeFromAutoUpdateList(clazz.name)
        }
        // 执行一次任务
        if (updateAll) {
            updateAllWidgets(context, clazz)
        } else {
            AAFWorkerManager.enqueueOneTimeWork(context, clazz)
        }
    }

    fun enableWidget(context: Context, clazz: Class<out BaseWidgetWorker>, canAutoUpdateByOthers: Boolean) {
        enqueueAutoStart(context)
        AAFWorkerManager.enqueueRepeatWork(context, 15 * 60L, clazz)
        updateWidget(context, clazz, canAutoUpdateByOthers, true)
    }

    fun disableWidget(context: Context, clazz: Class<out Worker>) {
        removeFromAutoUpdateList(clazz.name)
        if (!WidgetTools.hasAddWidget(context)) {
            cancelAutoStart(context)
        } else {
            updateAllWidgets(context)
        }
    }
}
