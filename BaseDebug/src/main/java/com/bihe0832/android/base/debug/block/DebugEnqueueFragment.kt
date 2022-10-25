/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.block


import android.view.View
import com.bihe0832.android.lib.block.task.BlockTaskManager
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager


class DebugEnqueueFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName

    var num = 0

    private val mTaskQueue = BlockTaskManager()
    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("定时任务测试", View.OnClickListener { testTask() }))

            add(DebugItemData("添加中优先级任务", View.OnClickListener {
                num++
                mTaskQueue.add(LogTask("DEFAULT：$num").apply {
                    setPriority(0)
                })

            }))
            add(DebugItemData("添加低优先级任务", View.OnClickListener {
                num++
                mTaskQueue.add(LogTask("LOW：$num").apply {
                    setPriority(-1)
                })

            }))
            add(DebugItemData("添加高优先级任务", View.OnClickListener {
                num++
                mTaskQueue.add(LogTask("HIGH：$num").apply {
                    setPriority(5)
                })

            }))
            add(DebugItemData("添加递增高优先级任务", View.OnClickListener {
                num++
                mTaskQueue.add(LogTask("HIGH：$num").apply {
                    setPriority(num)
                })

            }))
        }
    }

    private fun testTask() {
        val TASK_NAME = "AAA"
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
}