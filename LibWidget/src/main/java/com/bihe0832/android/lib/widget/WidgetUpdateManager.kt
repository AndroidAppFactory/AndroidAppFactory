package com.bihe0832.android.lib.widget

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import androidx.work.BackoffPolicy
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.lock.screen.service.LockScreenService
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.widget.tools.WidgetTools
import com.bihe0832.android.lib.widget.worker.BaseWidgetWorker
import java.time.Duration
import java.util.concurrent.TimeUnit

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
    private var mlastUpdateAllTime = 0L
    const val WIDGET_HOST_ID = 0x100
    const val ADD_WIDGET = 1

    fun initModuleWithOtherProcess(context: Context) {
        // provide custom configuration
        Configuration.Builder().setMinimumLoggingLevel(android.util.Log.INFO).build().let { myConfig ->
            // initialize WorkManager
            WorkManager.initialize(context, myConfig)
        }
        initModuleWithMainProcess(context)
    }

    fun initModuleWithMainProcess(context: Context) {
        updateAllWidgets(context)
    }

    fun enqueueAutoStart(context: Context) {
        cancelAutoStart(context)
        OneTimeWorkRequest.Builder(UpdateAllWork::class.java).apply {
            if (BuildUtils.SDK_INT >= Build.VERSION_CODES.O) {
                setBackoffCriteria(BackoffPolicy.EXPONENTIAL, Duration.ofSeconds(10))
            }
            setInitialDelay((100 * 356).toLong(), TimeUnit.DAYS)
        }.build().let { workRequest ->
            ZLog.w(TAG, "enqueueAutoStart")
            WorkManager.getInstance(context).enqueueUniqueWork(WIDGET_WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest)
        }
    }

    fun cancelAutoStart(context: Context) {
        ZLog.w(TAG, "cancelAutoStart")
        WorkManager.getInstance(context).cancelUniqueWork(WIDGET_WORK_NAME)
    }

    private fun addToAutoUpdateList(clazzName: String) {
        Config.readConfig(TAG, "").let {
            if (!it.contains(clazzName)) {
                Config.writeConfig(TAG, "$clazzName $it")
            }
        }
    }

    private fun removeFromAutoUpdateList(clazzName: String) {
        Config.readConfig(TAG, "").let {
            if (it.contains(clazzName)) {
                it.replace("$clazzName ", "").let { result ->
                    Config.writeConfig(TAG, result)
                    if (TextUtils.isEmpty(result)) {
                        WorkManager.getInstance().cancelUniqueWork(WIDGET_WORK_NAME)
                    }
                }
            }
        }
    }

    // 通过widget 唤起锁屏的前台服务
    private fun startLockScreen(context: Context) {
        ZLog.d(TAG, "startLockScreen by worker")
        Config.readConfig(LockScreenService.SERVICE_NAME_KEY, "").let {
            if (TextUtils.isEmpty("")) {
                ZLog.d(TAG, "startLockScreen by worker : $it")
                val intent = Intent()
                intent.setComponent(ComponentName(context.packageName, it))
                if (BuildUtils.SDK_INT >= Build.VERSION_CODES.O) {
                    context!!.startForegroundService(intent)
                } else {
                    context!!.startService(intent)
                }
            }
        }
    }

    private fun updateAllWidgets(context: Context, sourceClass: Class<out BaseWidgetWorker>?) {
        ZLog.e(TAG, "updateAll: durtaion is :${System.currentTimeMillis() - mlastUpdateAllTime}")
        // 执行一次任务
        if (System.currentTimeMillis() - mlastUpdateAllTime > 20 * 1000) {
            mlastUpdateAllTime = System.currentTimeMillis()
            WorkManager.getInstance(context).enqueue(OneTimeWorkRequest.from(UpdateAllWork::class.java))
        } else {
            ZLog.e(TAG, "updateAll: durtaion is less than 200000")
            sourceClass?.let { clazz ->
                WorkManager.getInstance(context).enqueue(OneTimeWorkRequest.from(clazz))
            }
        }
    }

    class UpdateAllWork(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

        private fun updateByName(name: String) {
            ZLog.d(TAG, "updateByName by $name")
            try {
                if (!name.contains("$")) {
                    (Class.forName(name) as? Class<out Worker>)?.let {
                        WorkManager.getInstance().enqueue(OneTimeWorkRequest.from(it))
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
                startLockScreen(applicationContext)
                Config.readConfig(TAG, "").split(" ").distinct().forEach {
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

    fun updateWidget(context: Context, clazz: Class<out BaseWidgetWorker>, canAutoUpdateByOthers: Boolean, updateAll: Boolean) {
        ZLog.d(TAG, "updateWidget:" + clazz.name + ",canAutoUpdateByOthers: $canAutoUpdateByOthers ; updateAll: $updateAll")
        if (canAutoUpdateByOthers) {
            addToAutoUpdateList(clazz.name)
        } else {
            removeFromAutoUpdateList(clazz.name)
        }
        // 执行一次任务
        if (updateAll) {
            updateAllWidgets(context, clazz)
        } else {
            WorkManager.getInstance(context).enqueue(OneTimeWorkRequest.from(clazz))
            startLockScreen(context)
        }
    }

    fun enableWidget(context: Context, clazz: Class<out BaseWidgetWorker>, canAutoUpdateByOthers: Boolean) {
        enqueueAutoStart(context)
        WorkManager.getInstance(context).enqueue(PeriodicWorkRequest.Builder(clazz, 15, TimeUnit.MINUTES).build())
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
