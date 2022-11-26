package com.bihe0832.android.lib.block.task.sequence

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

class DependenceBlockTask(name: String, private val mTaskListAction: DependenceBlockTaskManager.SequenceTaskCallback, private val taskAction: () -> Unit) : BaseAAFBlockTask(name) {

    open class TaskDependence(val taskID: String, val maxWaitingTime: Long) {
        override fun toString(): String {
            return "taskID:$taskID, max delay times:$maxWaitingTime"
        }
    }

    companion object {

        const val TASK_CHECKED_PERIOD = 500L

        const val TASK_STATUS_NOT_EXIST = 0
        const val TASK_STATUS_WAITING = 1
        const val TASK_STATUS_RUNNING = 2
        const val TASK_STATUS_FINISHED = 3
    }

    private var mTaskIsWaiting = false

    fun unlock() {
        mTaskListAction.updateTaskStatus(taskName, TASK_STATUS_FINISHED)
        unLockBlock()
    }

    fun pause() {
        mTaskListAction.updateTaskStatus(taskName, TASK_STATUS_WAITING)
        unLockBlock()
    }

    override fun compareTo(another: BlockTask): Int {
        return if (another is DependenceBlockTask) {
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

    @Synchronized
    final override fun doTask() {
        ZLog.d(TAG, "start waiting task: ${this.taskName} ")
        mTaskIsWaiting = true
        mTaskListAction.updateTaskStatus(taskName, TASK_STATUS_WAITING)
        mTaskListAction.updateTaskWaitTime(taskName)
        Executors.newSingleThreadExecutor().execute {
            try {
                ZLog.w(TAG, "start task action: ${this.taskName} ")
                while (mTaskIsWaiting) {
                    mTaskListAction.logAllTask()
                    //死循环
                    var depIsOKOrTimeout = true
                    var depIsWaiting = false
                    mTaskListAction.getAllDependenceList(taskName).forEach { depID ->
                        val depInfo = mTaskListAction.getTaskInfo(depID)
                        if (depID.equals(taskName) || mTaskListAction.getAllDependenceList(taskName).contains(taskName)) {
                            ZLog.e(TAG, "\n\n\n !!!!!! $taskName can not depend on ${depID}  skip check!!!! \n\n\n")
                        } else {
                            depInfo.gettaskCurrentStatus().let {
                                ZLog.d(TAG, "  $taskName task dep : $depID and status $it")
                                if (TASK_STATUS_NOT_EXIST == it) {
                                    var startWait = depInfo.taskFirstStartTime
                                    if (startWait == 0L) {
                                        mTaskListAction.updateTaskWaitTime(depID)
                                        startWait = System.currentTimeMillis()
                                    }
                                    ZLog.d(TAG, "  $taskName task dep : $depID start wait: $startWait")
                                    depIsOKOrTimeout = depIsOKOrTimeout && (System.currentTimeMillis() - startWait > mTaskListAction.getTaskInfo(taskName).getDependInfo()?.find { it.taskID.equals(depID) }?.maxWaitingTime ?: 0)
                                } else if (TASK_STATUS_WAITING == it) {
                                    ZLog.w(TAG, "${this.taskName} will replace by ${depID}")
                                    // 切换任务
                                    depIsWaiting = true
                                    depIsOKOrTimeout = false
                                } else {
                                    depIsOKOrTimeout = depIsOKOrTimeout && (TASK_STATUS_FINISHED == it)
                                }
                            }
                        }
                    }
                    if (depIsWaiting) {
                        ZLog.d(TAG, "task reset: $taskName ")
                        mTaskIsWaiting = false
                        mTaskListAction.resetTaskManager(this)
                    } else if (depIsOKOrTimeout) {
                        mTaskListAction.updateTaskStatus(taskName, TASK_STATUS_RUNNING)
                        ZLog.w(TAG, "start do task: ${this.taskName}")
                        taskAction()
                        mTaskIsWaiting = false
                    } else {
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