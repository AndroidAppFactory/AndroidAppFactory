package com.bihe0832.android.lib.block.task.sequence

import android.os.Handler
import android.os.Looper
import com.bihe0832.android.lib.block.task.BaseAAFBlockTask
import com.bihe0832.android.lib.block.task.BlockTask
import com.bihe0832.android.lib.log.ZLog
import java.util.concurrent.Executors

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2022/11/25.
 * Description: Description
 *
 */

class SequenceTask(name: String, private val mTaskListAction: SequenceTaskManager.SequenceTaskListCall, private val taskAction: () -> Unit, val mTaskDependenceList: List<TaskDependence>) : BaseAAFBlockTask(name) {

    open class TaskDependence(val dependOnTaskID: String, val maxWaitingTime: Long) {
        override fun toString(): String {
            return "taskID:$dependOnTaskID, max delay times$maxWaitingTime"
        }
    }

    companion object {

        const val TAG = "SequenceTaskManager"

        const val TASK_CHECKED_PERIOD = 500L

        const val TASK_STATUS_NOT_EXIST = 0
        const val TASK_STATUS_WAITING = 1
        const val TASK_STATUS_RUNNING = 2
        const val TASK_STATUS_FINISHED = 3
    }

    private var currentStatus = TASK_STATUS_WAITING
    private var taskStartTime = 0L
    private var mTaskIsWaiting = false

    fun getSequenceTaskStatus(): Int {
        return currentStatus
    }

    fun unlock() {
        unLockBlock()
        currentStatus = TASK_STATUS_FINISHED
    }

    override fun compareTo(another: BlockTask): Int {
        return if (another is SequenceTask) {
            val me = mTaskListAction.getAllDependenceList(taskName)
            val it = mTaskListAction.getAllDependenceList(another.taskName)
            if (me.contains(another.taskName)) {
                1
            } else if (it.contains(taskName)) {
                -1
            } else {
                super.compareTo(another)
            }
        } else {
            super.compareTo(another)
        }
    }

    override fun skipTask() {

    }

    open fun getCheckInterval(): Long {
        return TASK_CHECKED_PERIOD
    }

    override fun toString(): String {
        return super.toString() + "; currentStatus：$currentStatus"
    }

    final override fun doTask() {
        ZLog.d(TAG, "start waiting task: ${this.taskName} at $taskStartTime")
        mTaskIsWaiting = true
        currentStatus = TASK_STATUS_WAITING
        if (taskStartTime == 0L) {
            taskStartTime = System.currentTimeMillis()
        }
        Executors.newSingleThreadExecutor().execute {
            try {
                ZLog.d(TAG, "start task action: ${this.taskName} at $taskStartTime")
                while (mTaskIsWaiting) {
                    mTaskListAction.logAllTask()
                    //死循环
                    var depIsOKOrTimeout = true
                    var depIsWaiting = false
                    mTaskDependenceList.forEach { dep ->
                        mTaskListAction.getTaskStatus(dep.dependOnTaskID).let {
                            if (TASK_STATUS_NOT_EXIST == it) {
                                depIsOKOrTimeout = depIsOKOrTimeout && System.currentTimeMillis() - taskStartTime > dep.maxWaitingTime
                            } else if (TASK_STATUS_WAITING == it) {
                                ZLog.d(TAG, "${this.taskName} will replace by ${dep.dependOnTaskID}")

                                // 切换任务
                                depIsWaiting = true
                                depIsOKOrTimeout = false
                            } else {
                                depIsOKOrTimeout = depIsOKOrTimeout && TASK_STATUS_FINISHED == it
                            }
                        }
                    }
                    if (depIsWaiting) {
                        mTaskListAction.resetTaskManager()
                        mTaskIsWaiting = false
                    } else if (depIsOKOrTimeout) {
                        currentStatus = TASK_STATUS_RUNNING
                        ZLog.d(TAG, "start do task: ${this.taskName} at $taskStartTime")
                        Handler(Looper.getMainLooper()).post {
                            taskAction()
                        }
                        mTaskIsWaiting = false
                    } else if (currentStatus == TASK_STATUS_WAITING) {
                        Thread.sleep(getCheckInterval())
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}