package com.bihe0832.android.lib.block.task.dependence;

import com.bihe0832.android.lib.block.task.BaseAAFBlockTask
import com.bihe0832.android.lib.block.task.BlockTask
import com.bihe0832.android.lib.block.task.priority.PriorityBlockTaskManager
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.time.DateUtil


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/10/24.
 * Description: 具有依赖性质的任务管理器
 *
 */

open class DependenceBlockTaskManager(private val autoStart: Boolean) {

    private val INNER_TASK_ID = "AAFInnerTaskForDependentBlockTaskManager"

    //处理排序任务的互斥
    private val mTaskManager = PriorityBlockTaskManager()

    //处理排序任务的状态
    private val mSequenceTaskUpdatedInfoList = DependenceBlockTaskExecutionInfo()

    interface SequenceTaskCallback {
        fun resetTaskManager(task: DependenceBlockTask)
        fun getTaskInfo(taskID: String): DependenceBlockTaskExecutionInfo.DependenceTaskInfo
        fun updateTaskStartTime(taskID: String)
        fun updateTaskStatus(taskID: String, status: Int)
        fun logAllTask()
        fun getAllDependenceList(taskID: String): List<String>
    }

    private var mCurrentTaskListAction = object : SequenceTaskCallback {

        override fun logAllTask() {
//            taskList.values.forEach {
//                ZLog.d(BlockTask.TAG, "current task: $it")
//            }
        }

        override fun getAllDependenceList(taskID: String): List<String> {
            return mSequenceTaskUpdatedInfoList.getTaskDependenceListInfo(taskID)
        }

        override fun updateTaskStartTime(taskID: String) {
            mSequenceTaskUpdatedInfoList.getTaskInfo(taskID).updateTaskStartTime()
        }

        override fun updateTaskStatus(taskID: String, status: Int) {
            var taskInfo = getTaskInfo(taskID)
            if (INNER_TASK_ID.equals(taskID) && taskInfo.getCurrentStatus() > DependenceBlockTask.TASK_STATUS_WAITING) {
                ZLog.w(BaseAAFBlockTask.TAG, "task: ${taskID} current :${taskInfo.getCurrentStatus()} can not reset to $status")
            } else {
                taskInfo.updateCurrentStatus(status)
            }
        }

        @Synchronized
        override fun resetTaskManager(task: DependenceBlockTask) {
            ZLog.d(BlockTask.TAG, "pause task: $task")
            // 停止当前，替换为新任务
            do {
                mTaskManager.setRunning(false)
                Thread.sleep(10)
            } while (mTaskManager.isRunning)
            ZLog.d(BlockTask.TAG, " task unlock : $task")
            task.unlock()
            ZLog.d(BlockTask.TAG, " add task : $task")
            mTaskManager.add(task)
        }

        override fun getTaskInfo(taskID: String): DependenceBlockTaskExecutionInfo.DependenceTaskInfo {
            return mSequenceTaskUpdatedInfoList.getTaskInfo(taskID)
        }
    }

    init {
        mTaskManager.add(DependenceBlockTask(INNER_TASK_ID, mCurrentTaskListAction) {
            ZLog.d(BlockTask.TAG, "$INNER_TASK_ID start : $autoStart ${mSequenceTaskUpdatedInfoList.getTaskInfo(INNER_TASK_ID).getCurrentStatus()}")
            if (autoStart || mSequenceTaskUpdatedInfoList.getTaskInfo(INNER_TASK_ID).getCurrentStatus() == DependenceBlockTask.TASK_STATUS_FINISHED) {
                finishTask(INNER_TASK_ID)
            }
        })
    }

    fun finishTask(taskID: String) {
        ZLog.d(BlockTask.TAG, "finishTask task: $taskID")
        mSequenceTaskUpdatedInfoList.getTaskInfo(taskID).updateCurrentStatus(DependenceBlockTask.TASK_STATUS_FINISHED)
        (mTaskManager.currentTask as DependenceBlockTask?)?.let { currentRunningTask ->
            if (currentRunningTask.taskName.equals(taskID)) {
                ZLog.d(BlockTask.TAG, "unlock task: $currentRunningTask")
                // 停止当前，替换为新任务
                currentRunningTask.unlock()
            } else {
                ZLog.e(BlockTask.TAG, "\n\n\n !!!!!! Finish $taskID but current is ${currentRunningTask.taskName}!!!! \n\n\n")
            }

        }
    }

    fun start() {
        finishTask(INNER_TASK_ID)
    }

    @Synchronized
    fun addTask(taskID: String, taskPriority: Int, action: () -> Unit, dependList: List<DependenceBlockTask.TaskDependence>) {
        ZLog.d(BlockTask.TAG, "Add task: $taskID - $dependList")
        var realDependList = mutableListOf<DependenceBlockTask.TaskDependence>().apply {
            add(DependenceBlockTask.TaskDependence(INNER_TASK_ID, DateUtil.MILLISECOND_OF_DAY))
            dependList.forEach {
                if (it.taskID.equals(taskID) || mSequenceTaskUpdatedInfoList.getTaskDependenceListInfo(it.taskID).contains(taskID)) {
                    ZLog.e(BlockTask.TAG, "\n\n\n !!!!!! $taskID can not depend on ${it.taskID} !!!! \n\n\n")
                } else {
                    add(it)
                }
            }
        }

        if (dependList.size != realDependList.size - 1) {
            ZLog.e(BlockTask.TAG, "\n\n\n !!!!!! add bad TaskDependence !!!! \n\n\n")
        }
        mSequenceTaskUpdatedInfoList.getTaskInfo(taskID).apply {
            addDependList(realDependList)
            updateCurrentStatus(DependenceBlockTask.TASK_STATUS_WAITING)
        }
        mTaskManager.add(DependenceBlockTask(taskID, mCurrentTaskListAction, action).apply {
            priority = taskPriority
        })
    }

    @Synchronized
    fun addTask(taskID: String, action: () -> Unit, dependList: List<DependenceBlockTask.TaskDependence>) {
        addTask(taskID, 0, action, dependList)
    }

    fun reset() {
        val taskID = mTaskManager.currentTask?.taskName ?: ""
        ZLog.w(BaseAAFBlockTask.TAG, "start reset, current task: $taskID  ${mSequenceTaskUpdatedInfoList.getTaskInfo(taskID).getCurrentStatus()}")
        mTaskManager.clearAll()
        mSequenceTaskUpdatedInfoList.getTaskInfo(taskID).updateCurrentStatus(DependenceBlockTask.TASK_STATUS_KILLED)
        (mTaskManager.currentTask as DependenceBlockTask?)?.let { currentRunningTask ->
            Thread.sleep(currentRunningTask.getCheckInterval() * 2)
            currentRunningTask.unlock()
        }
        mSequenceTaskUpdatedInfoList.clear()
    }
}