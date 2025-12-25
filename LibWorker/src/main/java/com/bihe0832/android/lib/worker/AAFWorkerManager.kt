package com.bihe0832.android.lib.worker

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import androidx.work.BackoffPolicy
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.bihe0832.android.lib.foreground.service.AAFForegroundServiceManager
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.os.BuildUtils
import java.time.Duration
import java.util.UUID
import java.util.concurrent.TimeUnit


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2024/9/9.
 * Description: 所有 WorkerManager 相关的更新处理逻辑
 *
 */
object AAFWorkerManager {

    const val TAG = "AAFWorkerManager"

    fun initWorkManager(context: Context) {
        if (!isWorkManagerInitialized(context)) {
            // provide custom configuration
            Configuration.Builder().apply {
                setMinimumLoggingLevel(android.util.Log.INFO)
            }.build().let { myConfig ->
                // initialize WorkManager
                WorkManager.initialize(context, myConfig)
            }
        }
    }

    fun isWorkManagerInitialized(context: Context): Boolean {
        return try {
            WorkManager.getInstance(context)
            true
        } catch (e: IllegalStateException) {
            false
        }
    }

    // 唤起前台服务
    fun startForegroundService(
        context: Context,
        clazzName: String,
        action: String,
        intent: Intent,
        startAgain: Boolean,
    ) {
        ZLog.d(TAG, "startServiceByWidget by worker: $clazzName")
        if (!TextUtils.isEmpty(clazzName)) {
            if (isServiceRunning(context, clazzName)) {
                ZLog.e(
                    TAG, "startServiceByWidget by worker: service is running $clazzName, need start again:$startAgain"
                )
                if (!startAgain) {
                    return
                }
            }
            AAFForegroundServiceManager.startForegroundService(
                context, context.packageName, clazzName, action, intent
            )
        }
    }

    // 通过widget 唤起前台服务
    fun startForegroundService(context: Context, clazzName: String, startAgain: Boolean) {
        ZLog.d(TAG, "startServiceByWidget by worker: $clazzName")
        if (!TextUtils.isEmpty(clazzName)) {
            if (isServiceRunning(context, clazzName)) {
                ZLog.e(
                    TAG, "startServiceByWidget by worker: service is running $clazzName, need start again:$startAgain"
                )
                if (!startAgain) {
                    return
                }
            }
            AAFForegroundServiceManager.startForegroundService(
                context, context.packageName, clazzName, "", Intent()
            )
        }
    }

    fun isServiceRunning(context: Context, serviceClass: String): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        if (activityManager != null) {
            val runningServices = activityManager.getRunningServices(Int.MAX_VALUE)
            for (serviceInfo in runningServices) {
                if (serviceClass == serviceInfo.service.className) {
                    return true
                }
            }
        }
        return false
    }

    fun cancelUniqueWork(context: Context, workName: String) {
        initWorkManager(context)
        ZLog.w(TAG, "cancelUniqueWork")
        WorkManager.getInstance(context).cancelUniqueWork(workName)
    }

    @SuppressLint("NewApi")
    fun enqueueOneTimeUniqueWork(
        context: Context,
        name: String,
        delay: Long,
        clazz: Class<out ListenableWorker>,
    ): UUID {
        initWorkManager(context)
        OneTimeWorkRequest.Builder(clazz).apply {
            if (BuildUtils.SDK_INT >= Build.VERSION_CODES.O) {
                //设置指数退避策略，例如当Worker线程的执行出现了异常，如服务器宕机，你希望一段时间后重试该任务
                setBackoffCriteria(BackoffPolicy.EXPONENTIAL, Duration.ofSeconds(delay))
            } else {
                setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            }
            //触发条件满足后，延迟10s执行任务
            setInitialDelay(delay, TimeUnit.SECONDS)
        }.build().let { workRequest ->
            ZLog.w(TAG, "enqueueOneTimeUniqueWork")
            WorkManager.getInstance(context).enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, workRequest)
            return workRequest.id
        }
    }

    fun enqueueRepeatUniqueWorker(
        context: Context,
        name: String,
        repeatIntervalSeconds: Long,
        clazz: Class<out ListenableWorker>,
    ): UUID {
        initWorkManager(context)
        val finalDelay = if (repeatIntervalSeconds / 60 >= 15) {
            repeatIntervalSeconds
        } else {
            15 * 60
        }
        ZLog.w(TAG, "enqueueRepeatUniqueWorker finalDelay：$finalDelay")
        val workRequest = PeriodicWorkRequest.Builder(clazz, finalDelay, TimeUnit.SECONDS).build()
        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(name, ExistingPeriodicWorkPolicy.REPLACE, workRequest)
        return workRequest.id
    }

    fun enqueueOneTimeWork(context: Context, clazz: Class<out ListenableWorker>): UUID {
        initWorkManager(context)
        val request = OneTimeWorkRequest.from(clazz)
        WorkManager.getInstance(context).enqueue(request)
        return request.id
    }

    fun enqueueRepeatWork(context: Context, repeatIntervalSeconds: Long, clazz: Class<out ListenableWorker>): UUID {
        initWorkManager(context)
        val request = PeriodicWorkRequest.Builder(clazz, repeatIntervalSeconds, TimeUnit.SECONDS).build()
        WorkManager.getInstance(context).enqueue(request)
        return request.id
    }
}
