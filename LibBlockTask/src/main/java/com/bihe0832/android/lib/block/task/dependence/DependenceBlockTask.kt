package com.bihe0832.android.lib.block.task.dependence

import com.bihe0832.android.lib.block.task.BaseAAFBlockTask
import com.bihe0832.android.lib.block.task.BlockTask
import com.bihe0832.android.lib.log.ZLog
import java.util.concurrent.Executors

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/11/25.
 * Description: 带依赖的阻塞任务
 *
 */

class DependenceBlockTask(name: String, private val mTaskListAction: DependenceBlockTaskManager.SequenceTaskCallback, private val taskAction: () -> Unit) : BaseAAFBlockTask(name) {

    open class TaskDependence(val taskID: String, val maxWaitingTime: Long) {
        override fun toString(): String {
            return "taskID:$taskID, max delay times:$maxWaitingTime"
        }
    }

    companion object {

        const val TASK_CHECKED_PERIOD = 200L

        const val TASK_STATUS_NOT_EXIST = 0
        const val TASK_STATUS_WAITING = 1
        const val TASK_STATUS_RUNNING = 2
        const val TASK_STATUS_FINISHED = 3
        const val TASK_STATUS_KILLED = 4
    }

    private var mTaskIsWaiting = false

    //任务已完成
    fun unlock() {
        mTaskListAction.updateTaskStatus(taskName, TASK_STATUS_FINISHED)
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
        mTaskListAction.updateTaskStartTime(taskName)
        Executors.newSingleThreadExecutor().execute {
            try {
                ZLog.w(TAG, "start task action: ${this.taskName} ")
                while (mTaskIsWaiting) {
                    mTaskListAction.logAllTask()
                    //死循环
                    var depIsOKOrTimeout = true
                    var depIsWaiting = false
                    if (mTaskListAction.getTaskInfo(taskName).getCurrentStatus() == TASK_STATUS_KILLED) {
                        ZLog.w(TAG, "$taskName has killed, force exist and do nothing")
                        break
                    }
                    mTaskListAction.getAllDependenceList(taskName).forEach { depID ->
                        val depInfo = mTaskListAction.getTaskInfo(depID)
                        if (depID.equals(taskName) || mTaskListAction.getAllDependenceList(taskName).contains(taskName)) {
                            ZLog.e(TAG, "\n\n\n !!!!!! $taskName can not depend on ${depID}  skip check!!!! \n\n\n")
                        } else {
                            depInfo.getCurrentStatus().let {
                                if (TASK_STATUS_NOT_EXIST == it) {
                                    var startWait = depInfo.getFirstStartTime()
                                    if (startWait == 0L) {
                                        mTaskListAction.updateTaskStartTime(depID)
                                        startWait = System.currentTimeMillis()
                                    }
                                    var hasWait = System.currentTimeMillis() - startWait
                                    var needWait = mTaskListAction.getTaskInfo(taskName).getDependenceInfo()?.find { it.taskID.equals(depID) }?.maxWaitingTime
                                            ?: 0
                                    ZLog.d(TAG, "  $taskName task dep : $depID  hasWait $hasWait needWait $needWait ")
                                    depIsOKOrTimeout = depIsOKOrTimeout && (hasWait > needWait)
                                } else if (TASK_STATUS_WAITING == it) {
                                    ZLog.w(TAG, "${this.taskName} task dep : $depID , and  will replace by $depID")
                                    // 切换任务
                                    depIsWaiting = true
                                    depIsOKOrTimeout = false
                                } else {
                                    ZLog.d(TAG, "  $taskName task dep : $depID and status $it")
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
                        ZLog.w(TAG, "task start do: ${this.taskName} ${mTaskListAction.getTaskInfo(taskName).getCurrentStatus()}")

                        mTaskListAction.updateTaskStatus(taskName, TASK_STATUS_RUNNING)
                        taskAction()
                        mTaskIsWaiting = false
                    } else {
                        ZLog.d(TAG, "task $taskName sleep: ${getCheckInterval()} ")
                        Thread.sleep(getCheckInterval())
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}