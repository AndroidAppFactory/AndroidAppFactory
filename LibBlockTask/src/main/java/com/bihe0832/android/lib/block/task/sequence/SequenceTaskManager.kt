package com.bihe0832.android.lib.block.task.sequence;

import com.bihe0832.android.lib.block.task.priority.BlockTaskManager
import com.bihe0832.android.lib.log.ZLog
import java.util.concurrent.ConcurrentHashMap


/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2022/10/24.
 * Description: Description
 *
 */


open class SequenceTaskManager {

    private val mTaskManager = BlockTaskManager()
    private val taskList = ConcurrentHashMap<String, SequenceTask>()

    interface SequenceTaskListCall {
        fun getTaskStatus(id: String): Int
        fun logAllTask()
        fun resetTaskManager()
        fun getAllDependenceList(taskID: String): List<String>
        fun getTaskStartWaitTime(id: String): Long
        fun updateTaskWaitTime(id: String)
    }

    private var mCurrentTaskListAction = object : SequenceTaskListCall {
        private val mTaskStartWaitTime = ConcurrentHashMap<String, Long>()

        override fun getTaskStatus(name: String): Int {
            return taskList[name]?.getSequenceTaskStatus() ?: SequenceTask.TASK_STATUS_NOT_EXIST
        }

        override fun logAllTask() {
//            taskList.values.forEach {
//                ZLog.d(SequenceTask.TAG, "current task: $it")
//            }
        }

        override fun getAllDependenceList(taskID: String): List<String> {
            var list = mutableListOf<String>()
            taskList.get(taskID)?.mTaskDependenceList?.forEach {
                list.addAll(getAllDependenceList(it.taskID))
                list.add(it.taskID)
            }
            return list
        }

        override fun getTaskStartWaitTime(id: String): Long {
            return mTaskStartWaitTime[id] ?: 0
        }

        override fun updateTaskWaitTime(id: String) {
            if (!mTaskStartWaitTime.containsKey(id)) {
                mTaskStartWaitTime[id] = System.currentTimeMillis()
            }
        }

        @Synchronized
        override fun resetTaskManager() {
            if (mTaskManager.isRunning()) {
                (mTaskManager.currentTask as SequenceTask?)?.let { currentRunningTask ->
                    ZLog.d(SequenceTask.TAG, "pause task: $currentRunningTask")
                    // 停止当前，替换为新任务
                    mTaskManager.setRunning(false)
                    currentRunningTask.pause()
                    mTaskManager.setRunning(true)
                    mTaskManager.restart()
                    mTaskManager.add(currentRunningTask)
                }
            }
        }

        fun resetTask(taskID: String) {
            mTaskStartWaitTime.remove(taskID)
        }
    }

    final fun addTask(taskID: String, task: SequenceTask) {
        taskList.put(taskID, task)
        mTaskManager.add(task)
        mCurrentTaskListAction.resetTask(taskID)
    }

    @Synchronized
    fun addTask(taskID: String, action: () -> Unit, dependList: List<SequenceTask.TaskDependence>) {
        ZLog.d(SequenceTask.TAG, "Add task: $taskID - $dependList")
        var realDependList = mutableListOf<SequenceTask.TaskDependence>()
        dependList.forEach {
            if (it.taskID.equals(taskID) || mCurrentTaskListAction.getAllDependenceList(it.taskID).contains(taskID)) {
                ZLog.e(SequenceTask.TAG, "\n\n\n !!!!!! $taskID can not depend on ${it.taskID} !!!! \n\n\n")
            } else {
                realDependList.add(it)
            }
        }
        if (dependList.size != realDependList.size) {
            ZLog.e(SequenceTask.TAG, "\n\n\n !!!!!! add bad TaskDependence !!!! \n\n\n")
        }
        addTask(taskID, SequenceTask(taskID, mCurrentTaskListAction, action, realDependList))
    }

    fun finishCurrentTask() {
        (mTaskManager.currentTask as SequenceTask?)?.let { currentRunningTask ->
            ZLog.d(SequenceTask.TAG, "pause task: $currentRunningTask")
            // 停止当前，替换为新任务
            currentRunningTask.unlock()
        }
    }
}