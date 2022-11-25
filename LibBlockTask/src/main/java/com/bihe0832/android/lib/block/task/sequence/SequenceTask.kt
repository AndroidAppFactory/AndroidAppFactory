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

    open class TaskDependence(val taskID: String, val maxWaitingTime: Long) {
        override fun toString(): String {
            return "taskID:$taskID, max delay times:$maxWaitingTime"
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
        currentStatus = TASK_STATUS_FINISHED
        unLockBlock()
    }

    fun pause() {
        currentStatus = TASK_STATUS_WAITING
        unLockBlock()
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

    @Synchronized
    final override fun doTask() {
        ZLog.d(TAG, "start waiting task: ${this.taskName} at $taskStartTime")
        mTaskIsWaiting = true
        currentStatus = TASK_STATUS_WAITING
        mTaskListAction.updateTaskWaitTime(taskName)
        Executors.newSingleThreadExecutor().execute {
            try {
                ZLog.d(TAG, "start task action: ${this.taskName} at $taskStartTime")
                while (mTaskIsWaiting) {
                    mTaskListAction.logAllTask()
                    //死循环
                    var depIsOKOrTimeout = true
                    var depIsWaiting = false
                    mTaskDependenceList.forEach { dep ->
                        if (dep.taskID.equals(taskName) || mTaskListAction.getAllDependenceList(dep.taskID).contains(taskName)) {
                            ZLog.e(TAG, "\n\n\n !!!!!! $taskName can not depend on ${dep.taskID}  skip check!!!! \n\n\n")
                        } else {
                            mTaskListAction.getTaskStatus(dep.taskID).let {
                                ZLog.d(TAG, "  task dep : $dep and status $it")
                                if (TASK_STATUS_NOT_EXIST == it) {
                                    val startWait = mTaskListAction.getTaskStartWaitTime(dep.taskID)
                                    if (startWait == 0L) {
                                        mTaskListAction.updateTaskWaitTime(dep.taskID)
                                    }
                                    ZLog.d(TAG, "  task dep : $dep start wait: $startWait")
                                    depIsOKOrTimeout = depIsOKOrTimeout && (System.currentTimeMillis() - mTaskListAction.getTaskStartWaitTime(dep.taskID) > dep.maxWaitingTime)
                                } else if (TASK_STATUS_WAITING == it) {
                                    ZLog.d(TAG, "${this.taskName} will replace by ${dep.taskID}")
                                    // 切换任务
                                    depIsWaiting = true
                                    depIsOKOrTimeout = false
                                } else {
                                    depIsOKOrTimeout = depIsOKOrTimeout && TASK_STATUS_FINISHED == it
                                }
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
                        ZLog.d(TAG, "task sleep: ${getCheckInterval()} ")
                        Thread.sleep(getCheckInterval())
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}