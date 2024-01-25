package com.bihe0832.android.app.log

import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileTools
import com.bihe0832.android.framework.log.LoggerFile


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-03-05.
 * Description: 用户处理特殊日志
 *
 */
object AAFLoggerFile {

    const val MODULE_UPDATE = "udpate"
    const val MODULE_SERVER = "server"

    fun logServer(msg: String) {
        logFile(MODULE_SERVER, msg)
    }

    fun logUpdate(msg: String) {
        logFile(MODULE_UPDATE, msg)
    }

    fun log(module: String, msg: String) {
        LoggerFile.log(LoggerFile.getZixieFileLogPathByModule(module), msg)
    }

    fun logFile(module: String, msg: String) {
        LoggerFile.logFile(LoggerFile.getZixieFileLogPathByModule(module), msg)
    }

    fun openLog(module: String) {
        AAFFileTools.openFileWithTips(ZixieContext.getCurrentActivity()!!, LoggerFile.getZixieFileLogPathByModule(module))
    }

    fun sendLog(module: String) {
        AAFFileTools.sendFile(LoggerFile.getZixieFileLogPathByModule(module))
    }

    fun getLogPathByModuleName(module: String): String {
        return LoggerFile.getZixieFileLogPathByModule(module)
    }
}