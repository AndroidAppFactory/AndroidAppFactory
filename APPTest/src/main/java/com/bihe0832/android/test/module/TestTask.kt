package com.bihe0832.android.test.module

import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.timer.BaseTask
import com.bihe0832.android.lib.timer.TaskManager

/**
 * @author zixie code@bihe0832.com
 * Created on 2020-03-05.
 * Description: Description
 */
class TestTask : BaseTask() {
    private var a = 0
    private val TASK_ANME = "TestTask"

    override fun getTaskName(): String {
        return TASK_ANME
    }

    override fun getMyInterval(): Int {
        return 4
    }

    override fun getNextEarlyRunTime(): Int {
        return 6
    }

    override fun run() {
        ZLog.d("TestTask", "TestTask")
        //        TaskManager.getInstance().letTaskRunEarly("TestTask");
        a++
        if (a > 6) {
            TaskManager.getInstance().removeTask(TASK_ANME)
        }
    }

}