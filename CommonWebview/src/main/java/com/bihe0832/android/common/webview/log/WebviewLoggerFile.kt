package com.bihe0832.android.common.webview.log

import com.bihe0832.android.framework.log.LoggerFile


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-03-05.
 * Description: 用户处理特殊日志
 *
 */
object WebviewLoggerFile {

    const val MODULE_NAME = "webview"


    fun log(msg: String) {
        LoggerFile.logFile(LoggerFile.getZixieFileLogPathByModule(MODULE_NAME), msg)
    }

    fun getWebviewLogPath(): String {
        return LoggerFile.getZixieFileLogPathByModule(MODULE_NAME);
    }
}