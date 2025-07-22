/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.task


import android.content.Context
import android.content.Intent
import android.view.View
import androidx.work.WorkerParameters
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.block.task.priority.PriorityBlockTaskManager
import com.bihe0832.android.lib.foreground.service.AAFForegroundServiceManager
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager
import com.bihe0832.android.lib.timer.TimerProcessManager
import com.bihe0832.android.lib.worker.AAFBaseWorker
import com.bihe0832.android.lib.worker.AAFWorkerManager

val scene = "fdsfsfds"

class DebugEnqueueFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName

    var num = 0

    private val mTaskQueue = PriorityBlockTaskManager()
    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugItem("定时任务测试", View.OnClickListener { testTask() }))
            add(getDebugItem("开启单次任务", View.OnClickListener { startOneTimeWork() }))
            add(getDebugItem("开启单次延迟任务", View.OnClickListener { startOneTimeDelayWork() }))
            add(getDebugItem("开启重复任务", View.OnClickListener { startRepeatUniqueWork() }))
            add(getDebugItem("取消任务", View.OnClickListener { cancelUniqueWork() }))
            add(getDebugItem("定时计数任务(自动结束)", View.OnClickListener { testTimerProcess(true) }))
            add(getDebugItem("定时计数任务(延迟结束)", View.OnClickListener { testTimerProcess(false) }))

            add(getDebugItem("添加中优先级任务", View.OnClickListener {
                num++
                mTaskQueue.add(LogTask("DEFAULT：$num").apply {
                    setPriority(0)
                })

            }))
            add(getDebugItem("添加低优先级任务", View.OnClickListener {
                num++
                mTaskQueue.add(LogTask("LOW：$num").apply {
                    setPriority(-1)
                })

            }))
            add(getDebugItem("添加高优先级任务", View.OnClickListener {
                num++
                mTaskQueue.add(LogTask("HIGH：$num").apply {
                    setPriority(5)
                })

            }))
            add(getDebugItem("添加递增高优先级任务", View.OnClickListener {
                num++
                mTaskQueue.add(LogTask("HIGH：$num").apply {
                    setPriority(num)
                })
            }))
        }
    }

    private fun testTimerProcess(autoEnd: Boolean) {
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

    private fun testTask() {
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

                    override fun run() {
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


    fun startOneTimeWork() {
        AAFWorkerManager.enqueueOneTimeWork(context!!, TestWork::class.java)
    }

    fun startOneTimeDelayWork() {
        ZLog.d(LOG_TAG, "startOneTimeUniqueWork")
        AAFWorkerManager.enqueueOneTimeUniqueWork(context!!, scene, 10, TestWork::class.java)
    }

    fun startRepeatUniqueWork() {
        ZLog.d(LOG_TAG, "startRepeatUniqueWork")
        AAFWorkerManager.enqueueRepeatUniqueWorker(context!!, scene, 15, TestWork::class.java)
    }


    fun cancelUniqueWork() {
        ZLog.d(LOG_TAG, "cancelUniqueWork")
        AAFWorkerManager.cancelUniqueWork(context!!, scene)
    }

    class TestWork(context: Context, workerParams: WorkerParameters) : AAFBaseWorker(context, workerParams) {
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
}