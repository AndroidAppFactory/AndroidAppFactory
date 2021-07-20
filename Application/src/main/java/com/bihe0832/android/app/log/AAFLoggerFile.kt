package com.bihe0832.android.app.log

import com.bihe0832.android.framework.log.LoggerFile


/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020-03-05.
 * Description: 用户处理特殊日志
 *
 */
object AAFLoggerFile {

    const val MODULE_UPDATE = "udpate"

    fun logUpdate(msg: String) {
        log(MODULE_UPDATE, msg)
    }

    fun log(module: String, msg: String) {
        LoggerFile.log(LoggerFile.getZixieFileLogPathByModule(module), msg)
    }

    fun openLog(module: String) {
        LoggerFile.openLog(LoggerFile.getZixieFileLogPathByModule(module))
    }

    fun sendLog(module: String) {
        LoggerFile.sendLog(LoggerFile.getZixieFileLogPathByModule(module))
    }


    fun getLogPathByModuleName(module: String): String{
        return LoggerFile.getZixieFileLogPathByModule(module)
    }
}