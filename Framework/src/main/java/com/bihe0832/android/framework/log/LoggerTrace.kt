package com.bihe0832.android.framework.log

import android.text.TextUtils
import com.bihe0832.android.lib.log.ZLog
import java.util.concurrent.CopyOnWriteArrayList


/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2021-06-25.
 * Description: 耗时打点跟踪记录
 *
 */
object LoggerTrace {

    private const val TRACE_MODULE_NAME = "Trace"

    private class LoggerInfo(
            var tag: String = "",
            var msg: String = ""
    ) {

        var logTime = 0L

        init {
            logTime = System.currentTimeMillis()
        }
    }

    private val mTraceList = CopyOnWriteArrayList<LoggerInfo>()

    private fun showResult(currentInfo: LoggerInfo, needLog: Boolean, needDuration: Boolean, needFile: Boolean) {
        val index = mTraceList.indexOf(currentInfo)
        var duration = if (index > 0) {
            currentInfo.logTime - mTraceList.get(index - 1).logTime
        } else {
            0
        }

        if (needFile) {
            logToFile("Action at ${currentInfo.logTime} used $duration for ${currentInfo.tag} ,msg is : ${currentInfo.msg}")
        } else {
            if (needDuration) {
                ZLog.d(TRACE_MODULE_NAME, "Action at ${currentInfo.logTime} used $duration for ${currentInfo.tag} ,msg is : ${currentInfo.msg}")
            } else if (needLog) {
                ZLog.d(TRACE_MODULE_NAME, "Action at ${currentInfo.logTime} for ${currentInfo.tag} ,msg is : ${currentInfo.msg}")
            }
        }
    }

    @Synchronized
    fun tracePoint(tag: String, info: String, needLog: Boolean, needDuration: Boolean, needFile: Boolean) {
        LoggerInfo(tag, info).let {
            mTraceList.add(it)
            showResult(it, needLog, needDuration, needFile)
        }
    }

    fun showResult(tag: String, needLog: Boolean, needDuration: Boolean, needFile: Boolean) {
        ZLog.d(TRACE_MODULE_NAME, "------------------ Action showResult for $tag start ------------------")
        mTraceList.forEach {
            if (TextUtils.isEmpty(tag)) {
                showResult(it, needLog, needDuration, needFile)
            } else if (tag == it.tag) {
                showResult(it, needLog, needDuration, needFile)
            }
        }
        ZLog.d(TRACE_MODULE_NAME, "------------------ Action showResult for $tag end ------------------")
    }

    fun tracePoint(tag: String, info: String) {
        tracePoint(tag, info, needLog = false, needDuration = true, needFile = false)
    }

    fun tracePoint(info: String) {
        tracePoint("Default", info, needLog = false, needDuration = true, needFile = false)
    }


    fun showResult(tag: String) {
        showResult(tag, needLog = false, needDuration = false, needFile = true)
    }

    fun showResult() {
        showResult("", needLog = false, needDuration = false, needFile = true)
    }

    @Synchronized
    fun reset(tag: String) {
        showResult(tag, needLog = false, needDuration = false, needFile = true)
        for (num in mTraceList) {
            if (num.tag == tag) {
                mTraceList.remove(num)
            }
        }
    }

    private fun logToFile(msg: String) {
        LoggerFile.log(LoggerFile.getZixieFileLogPathByModule(TRACE_MODULE_NAME), msg)
    }

    fun openLog() {
        LoggerFile.openLog(LoggerFile.getZixieFileLogPathByModule(TRACE_MODULE_NAME))
    }

    fun sendLog() {
        LoggerFile.sendLog(LoggerFile.getZixieFileLogPathByModule(TRACE_MODULE_NAME))
    }
}