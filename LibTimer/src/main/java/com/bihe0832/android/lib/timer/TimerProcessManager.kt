package com.bihe0832.android.lib.timer

import com.bihe0832.android.lib.utils.IdGenerator
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object TimerProcessManager {

    interface ProgressCallback {
        fun onProgress(name: String, progress: Int)
    }

    private const val TASK_NAME_PRE = "TimerProcessManager"
    private val mIntentID = IdGenerator(1)
    private val mProcessTimer = Timer()

    private val aaa = ConcurrentHashMap<String, TimerTask>()

    fun startProcessWithDuration(start: Int, end: Int, duration: Int, step: Int, autoEnd: Boolean, progressCallback: ProgressCallback): String {
        var name = TASK_NAME_PRE + mIntentID.generate()
        val period = duration * 1000L * step / ((end - start))
        val task = object : TimerTask() {
            private var current = start
            override fun run() {
                if (current >= end) {
                    if (autoEnd) {
                        progressCallback.onProgress(name, end)
                    } else {
                        progressCallback.onProgress(name, end - step)
                    }
                } else {
                    progressCallback.onProgress(name, current)
                }

                if (autoEnd && current >= end) {
                    this.cancel()
                    mProcessTimer.purge()
                } else {
                    current += step
                }
            }
        }
        aaa.put(name, task)
        mProcessTimer.schedule(task, 0, period)
        return name
    }

    fun stopProcess(name: String) {
        aaa[name]?.cancel()
    }
}