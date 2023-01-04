package com.bihe0832.android.lib.timer

import com.bihe0832.android.lib.utils.IdGenerator

object TimerProcessManager {

    interface ProgressCallback {
        fun onProgress(name: String, progress: Int)
    }

    private const val TASK_NAME_PRE = "TimerProcessManager"
    private val mIntentID = IdGenerator(1)

    fun startTimerProcess(start: Int, end: Int, timerPeriod: Int, step: Int, autoEnd: Boolean, progressCallback: ProgressCallback): String {
        var name = TASK_NAME_PRE + mIntentID.generate()
        TaskManager.getInstance().addTask(object : BaseTask() {
            private var current = start
            override fun getMyInterval(): Int {
                return timerPeriod * 2
            }

            override fun getNextEarlyRunTime(): Int {
                return 0
            }

            override fun runAfterAdd(): Boolean {
                return true
            }

            override fun run() {
                if (autoEnd) {
                    progressCallback.onProgress(name, current)
                } else {
                    if (current >= end) {
                        progressCallback.onProgress(name, end - step)
                    } else {
                        progressCallback.onProgress(name, current)
                    }
                }
                if (autoEnd && current >= end) {
                    stopTimerProcess(name)
                } else {
                    current += step
                }
            }

            override fun getTaskName(): String {
                return name
            }
        })
        return name
    }

    fun stopTimerProcess(name: String) {
        TaskManager.getInstance().removeTask(name)
    }
}