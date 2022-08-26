package com.bihe0832.android.base.debug.log


import android.view.View
import com.bihe0832.android.app.log.AAFLoggerFile
import com.bihe0832.android.common.debug.base.BaseDebugListFragment
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.log.DebugLogActivity
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.framework.log.LoggerTrace
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.log.LogImplForLogcat
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager

class DebugLogFragment : BaseDebugListFragment() {
    val LOG_TAG = "Test"

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {

            add(DebugItemData("自定义日志管理", View.OnClickListener {
                startActivityWithException(DebugLogActivity::class.java)
            }))

            add(DebugItemData("简单日志", View.OnClickListener {
                ZLog.i(LOG_TAG, "testi")
                ZLog.d(LOG_TAG, "testd")
                ZLog.w(LOG_TAG, "testw")
                ZLog.e(LOG_TAG, "teste")
                ZLog.info(LOG_TAG, "testinfo")
            }))
            add(DebugItemData("关闭Logcat", View.OnClickListener { ZLog.closeLogcat() }))
            add(DebugItemData("开启Logcat", View.OnClickListener { ZLog.addLogImpl(LogImplForLogcat) }))
            add(DebugItemData("耗时打点", View.OnClickListener { testTrace() }))
            add(DebugItemData("文件日志", View.OnClickListener { testLogFile() }))
            add(DebugItemData("打开文件日志", View.OnClickListener { AAFLoggerFile.openLog(LOG_TAG) }))
            add(DebugItemData("发送文件日志", View.OnClickListener { AAFLoggerFile.sendLog(LOG_TAG) }))
            add(DebugItemData("跨线程文件日志", View.OnClickListener { testLog() }))
        }
    }

    private fun testTrace() {
        LoggerTrace.tracePoint("Test 2")
        LoggerTrace.tracePoint(LOG_TAG, "Test 1")
        LoggerTrace.tracePoint(LOG_TAG, "Test 3", needLog = false, needDuration = false, needFile = false)
        LoggerTrace.tracePoint(LOG_TAG, "Test 4", needLog = false, needDuration = true, needFile = false)
        LoggerTrace.tracePoint(LOG_TAG, "Test 5", true, needDuration = false, needFile = false)
        LoggerTrace.tracePoint(LOG_TAG, "Test 5", true, needDuration = true, needFile = true)
        for (i in 0..20) {
            ThreadManager.getInstance().start({
                LoggerTrace.tracePoint(LOG_TAG, "Test $i")
            }, i * 333L + i)
        }
        LoggerTrace.showResult()
        LoggerTrace.showResult(LOG_TAG)
    }

    private fun testLogFile() {
        logToFile("test ")
    }

    private fun testLog() {
        for (i in 0..20) {
            ThreadManager.getInstance().start({
                logToFile("test by auto  $i")
            }, i * 22L + i)
        }
    }

    fun logToFile(msg: String) {
        LoggerFile.log(LoggerFile.getZixieFileLogPathByModule(LOG_TAG), msg)
    }
}