package com.bihe0832.android.app.log

import android.app.Activity
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.file.AAFFileTools
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.framework.log.LoggerFile.TYPE_HTML
import com.bihe0832.android.framework.log.LoggerFile.TYPE_TEXT
import com.bihe0832.android.framework.router.showH5Log
import com.bihe0832.android.framework.router.showLog


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

    @Synchronized
    fun log(filePath: String, tag: String, msg: String) {
        LoggerFile.log(filePath, tag, msg)
    }

    @Synchronized
    fun logH5(filePath: String, tag: String, msg: String) {
        LoggerFile.logH5(filePath, tag, msg)
    }

    fun sendLogByModule(module: String) {
        AAFFileTools.sendFile(LoggerFile.getZixieFileLogPathByModule(module))
    }

    fun showLogByModule(module: String) {
        showLog(
            getLogPathByModuleName(module), isReversed = true, showLine = true
        )
    }

    fun showLogByModule(activity: Activity, module: String) {
        AAFFileTools.openFileWithTips(activity, getLogPathByModuleName(module))
    }

    fun showLocalH5LogByModule(module: String) {
        showH5Log(
            getLogPathByModuleName(module, TYPE_HTML)
        )
    }

    fun getLogPathByModuleName(module: String, type: Int): String {
        return LoggerFile.getZixieFileLogPathByModule(module, ZixieContext.getLogFolder(), type)
    }

    fun getLogPathByModuleName(module: String): String {
        return getLogPathByModuleName(module, TYPE_TEXT)
    }

    fun logServer(msg: String) {
        LoggerFile.log(MODULE_SERVER, msg)
    }

    fun logUpdate(msg: String) {
        LoggerFile.log(MODULE_UPDATE, msg)
    }
}