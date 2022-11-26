package com.bihe0832.android.lib.block.task.sequence

import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020/9/30.
 * Description: Description
 *
 */
class DependenceBlockTaskExecutionInfo {

    class SequenceTaskInfo(private var taskID: String = "") {
        var taskFirstStartTime = 0L
        private var taskCurrentStatus = DependenceBlockTask.TASK_STATUS_NOT_EXIST
        private var dependList: MutableList<DependenceBlockTask.TaskDependence> = mutableListOf()

        fun getDependInfo(): List<DependenceBlockTask.TaskDependence>? {
            return dependList
        }

        fun updateTtaskCurrentStatus(status: Int) {
            taskCurrentStatus = status
        }

        fun gettaskCurrentStatus(): Int {
            return taskCurrentStatus
        }

        override fun toString(): String {
            return "SequenceTaskInfo(taskID='$taskID', taskFirstStartTime=$taskFirstStartTime, taskCurrentStatus=$taskCurrentStatus)"
        }

        fun addDependList(list: List<DependenceBlockTask.TaskDependence>) {
            mutableListOf<DependenceBlockTask.TaskDependence>().apply {
                addAll(list)
                addAll(dependList)
            }.distinctBy { it.taskID }.toMutableList().let {
                dependList = it
            }
        }
    }

    private val taskList = ConcurrentHashMap<String, SequenceTaskInfo>()

    @Synchronized
    fun getTaskInfo(taskID: String): SequenceTaskInfo {
        if (!taskList.containsKey(taskID)) {
            taskList.put(taskID, SequenceTaskInfo(taskID))
        }
        return taskList.get(taskID)!!
    }

    fun getTaskDependListInfo(taskID: String): List<String> {
        var result = mutableListOf<String>()
        getTaskInfo(taskID).getDependInfo()?.forEach {
            result.addAll(getTaskDependListInfo(it.taskID))
            result.add(it.taskID)
        }
        return result.distinct()
    }
}