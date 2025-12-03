/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.compose.debug.task

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.work.WorkerParameters
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.lib.block.task.priority.PriorityBlockTaskManager
import com.bihe0832.android.lib.foreground.service.AAFForegroundServiceManager
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.lib.timer.TimerProcessManager
import com.bihe0832.android.lib.worker.AAFBaseWorker
import com.bihe0832.android.lib.worker.AAFWorkerManager

internal const val LOG_TAG = "DebugEnqueueView"
internal const val scene = "fdsfsfds"

@Composable
fun DebugEnqueueView() {
    DebugContent {
        var num = 0
        val mTaskQueue = PriorityBlockTaskManager()

        DebugItem("定时任务测试") { testTask() }
        DebugItem("开启单次任务") { startOneTimeWork(it) }
        DebugItem("开启单次延迟任务") { startOneTimeDelayWork(it) }
        DebugItem("开启重复任务") { startRepeatUniqueWork(it) }
        DebugItem("取消任务") { cancelUniqueWork(it) }
        DebugItem("定时计数任务(自动结束)") { testTimerProcess(true) }
        DebugItem("定时计数任务(延迟结束)") { testTimerProcess(false) }

        DebugItem("添加中优先级任务") {
            num++
            mTaskQueue.add(LogTask("DEFAULT：$num").apply {
                setPriority(0)
            })

        }
        DebugItem("添加低优先级任务") {
            num++
            mTaskQueue.add(LogTask("LOW：$num").apply {
                setPriority(-1)
            })

        }
        DebugItem("添加高优先级任务") {
            num++
            mTaskQueue.add(LogTask("HIGH：$num").apply {
                setPriority(5)
            })

        }
        DebugItem("添加递增高优先级任务") {
            num++
            mTaskQueue.add(LogTask("HIGH：$num").apply {
                setPriority(num)
            })
        }
    }
}


internal fun testTimerProcess(autoEnd: Boolean) {
    TimerProcessManager.startProcessWithDuration(1,
            20,
            5,
            1,
            autoEnd,
            object : TimerProcessManager.ProgressCallback {
                override fun onProgress(name: String, progress: Int) {
                    ZLog.d("TimerProcessManager", "TASK_NAME $name  and progress $progress")
                }
            }).let {
        ThreadManager.getInstance().start({ TimerProcessManager.stopProcess(it) }, 7)
    }
}

internal fun testTask() {
    val TASK_NAME = "NetworkApi"
    for (i in 0..20) {
        ThreadManager.getInstance().start({
            TaskManager.getInstance().removeTask(TASK_NAME)
            TaskManager.getInstance().addTask(object : BaseTask() {
                override fun getMyInterval(): Int {
                    return 2
                }

                override fun getNextEarlyRunTime(): Int {
                    return 0
                }

                override fun runAfterAdd(): Boolean {
                    return false
                }

                override fun doTask() {
                    ZLog.d("TaskManager", "TASK_NAME $i ${this.hashCode()}")
                }

                override fun getTaskName(): String {
                    return TASK_NAME
                }

            })
        }, i * 2)

        ThreadManager.getInstance().start({
            TaskManager.getInstance().removeTask(TASK_NAME)
        }, i * 2 + 2700L)
    }

    ThreadManager.getInstance().start({
        TaskManager.getInstance().removeTask(TASK_NAME)
    }, 60)
}


internal fun startOneTimeWork(context: Context) {
    AAFWorkerManager.enqueueOneTimeWork(context!!, TestWork::class.java)
}

internal fun startOneTimeDelayWork(context: Context) {
    ZLog.d(LOG_TAG, "startOneTimeUniqueWork")
    AAFWorkerManager.enqueueOneTimeUniqueWork(context!!, scene, 10, TestWork::class.java)
}

internal fun startRepeatUniqueWork(context: Context) {
    ZLog.d(LOG_TAG, "startRepeatUniqueWork")
    AAFWorkerManager.enqueueRepeatUniqueWorker(context!!, scene, 15, TestWork::class.java)
}


internal fun cancelUniqueWork(context: Context) {
    ZLog.d(LOG_TAG, "cancelUniqueWork")
    AAFWorkerManager.cancelUniqueWork(context!!, scene)
}

internal class TestWork(context: Context, workerParams: WorkerParameters) : AAFBaseWorker(context, workerParams) {
    override fun doAction(context: Context?) {
        ZLog.d(AAFWorkerManager.TAG, "TestWork doAction")
        AAFForegroundServiceManager.sendToForegroundService(context!!, Intent().apply {
            putExtra(scene, "Fsdfsf1")
        }, object : AAFForegroundServiceManager.ForegroundServiceAction {
            override fun getScene(): String {
                return scene
            }

            override fun getNotifyContent(): String {
                return scene
            }

            override fun onStartCommand(context: Context, intent: Intent, flags: Int, startId: Int) {
                ZLog.d(AAFWorkerManager.TAG, "TestWork onStartCommand")
                if (intent.action.equals(scene)) {
                    ThreadManager.getInstance().start({
                        ZLog.d(AAFWorkerManager.TAG, "TestWork onStartCommand deleteFromForegroundService")
                        AAFForegroundServiceManager.deleteFromForegroundService(context, scene)
                    }, 5)
                }
            }
        })
    }
}