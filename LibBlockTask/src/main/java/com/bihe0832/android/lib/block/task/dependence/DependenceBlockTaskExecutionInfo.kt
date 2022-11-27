package com.bihe0832.android.lib.block.task.dependence

import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020/9/30.
 * Description: Description
 *
 */
class DependenceBlockTaskExecutionInfo {

    class DependenceTaskInfo(private var taskID: String = "") {
        private var taskFirstStartTime = 0L
        private var taskCurrentStatus = DependenceBlockTask.TASK_STATUS_NOT_EXIST
        private var dependList: MutableList<DependenceBlockTask.TaskDependence> = mutableListOf()

        fun getDependenceInfo(): List<DependenceBlockTask.TaskDependence> {
            return dependList
        }

        fun updateCurrentStatus(status: Int) {
            taskCurrentStatus = status
        }

        fun updateTaskStartTime() {
            if (taskFirstStartTime == 0L) {
                taskFirstStartTime = System.currentTimeMillis()
            }
        }

        fun getFirstStartTime(): Long {
            return taskFirstStartTime
        }

        fun getCurrentStatus(): Int {
            return taskCurrentStatus
        }

        override fun toString(): String {
            return "DependenceTaskInfo(taskID='$taskID', taskFirstStartTime=$taskFirstStartTime, taskCurrentStatus=$taskCurrentStatus)"
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

    private val taskList = ConcurrentHashMap<String, DependenceTaskInfo>()

    @Synchronized
    fun getTaskInfo(taskID: String): DependenceTaskInfo {
        if (!taskList.containsKey(taskID)) {
            taskList.put(taskID, DependenceTaskInfo(taskID))
        }
        return taskList.get(taskID)!!
    }

    fun getTaskDependenceListInfo(taskID: String): List<String> {
        var result = mutableListOf<String>()
        getTaskInfo(taskID).getDependenceInfo().forEach {
            result.addAll(getTaskDependenceListInfo(it.taskID))
            result.add(it.taskID)
        }
        return result.distinct()
    }
}