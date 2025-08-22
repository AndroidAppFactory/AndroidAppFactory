package com.bihe0832.android.base.compose.debug.log


import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.bihe0832.android.app.log.AAFLoggerFile
import com.bihe0832.android.common.compose.debug.item.DebugComposeActivityItem
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.log.DebugLogComposeActivity
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileTools
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.framework.log.LoggerTrace
import com.bihe0832.android.framework.router.showH5File
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.LogImplForLogcat
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.time.DateUtil

private const val LOG_TAG = "DebugComposeLogView"

@Composable
fun DebugLogComposeView() {
    val activity = LocalContext.current as? Activity

    DebugContent {
        DebugComposeActivityItem("自定义日志管理", DebugLogComposeActivity::class.java)
        DebugItem("将日志按H5记录并查看") { context -> logToH5File(context) }

        DebugItem("简单日志") { context ->
            ZLog.i(LOG_TAG, "testi")
            ZLog.d(LOG_TAG, "testd")
            ZLog.w(LOG_TAG, "testw")
            ZLog.e(LOG_TAG, "teste")
            ZLog.info(LOG_TAG, "testinfo")
            ZLog.info(LOG_TAG, TextFactoryUtils.getRandomString(3000))
        }
        DebugItem("关闭Logcat") { context -> ZLog.closeLogcat() }
        DebugItem(
            "开启Logcat"
        ) { ZLog.addLogImpl(LogImplForLogcat) }

        DebugItem("跨线程文件日志") { context -> testLog() }
        DebugItem("耗时打点") { context -> testTrace() }
        DebugItem("文件日志") { context -> testLogFile() }
        DebugItem(
            "打开文件日志"
        ) { context -> AAFLoggerFile.showLogByModule(LOG_TAG) }
        DebugItem(
            "发送文件日志"
        ) { context -> AAFLoggerFile.sendLogByModule(LOG_TAG) }

        DebugItem(
            "ACE打开文件日志"
        ) {
            activity?.let {
                AAFFileTools.openFileWithTips(
                    activity, AAFLoggerFile.getLogPathByModuleName(LOG_TAG)
                )
            }
        }
    }
}

private fun testTrace() {
    LoggerTrace.tracePoint("Test 2")
    LoggerTrace.tracePoint(LOG_TAG, "Test 1")
    LoggerTrace.tracePoint(
        LOG_TAG, "Test 3", needLog = false, needDuration = false, needFile = false
    )
    LoggerTrace.tracePoint(
        LOG_TAG, "Test 4", needLog = false, needDuration = true, needFile = false
    )
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

private fun logToFile(msg: String) {
    LoggerFile.log(LoggerFile.getZixieFileLogPathByModule(LOG_TAG), msg)
}

private fun logToH5File(context: Context) {

    val path = ZixieContext.getLogFolder() + "TEST_${DateUtil.getCurrentDateEN("yyyyMMdd")}.html"
    FileUtils.deleteFile(path)
    LoggerFile.logFile(
        path, LoggerFile.TYPE_HTML, DateUtil.getCurrentDateEN(), "这是一个测试1\nfgdfg"
    )
    LoggerFile.logFile(path, LoggerFile.TYPE_HTML, DateUtil.getCurrentDateEN(), "这是一个测试2")
    LoggerFile.logFile(path, LoggerFile.TYPE_HTML, DateUtil.getCurrentDateEN(), "这是一个测试3")
    val mp3Path = ZixieContext.getLogFolder() + "TEST_${DateUtil.getCurrentDateEN("yyyyMMdd")}.mp3"

    FileUtils.copyAssetsFileToPath(
        context, "audio.mp3", mp3Path
    )

    LoggerFile.logFile(
        path,
        LoggerFile.TYPE_HTML,
        DateUtil.getCurrentDateEN(),
        LoggerFile.getAudioH5LogData(mp3Path, "audio/mp3")
    )
    ZLog.d(path)

    showH5File(path)

}