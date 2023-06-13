package com.bihe0832.android.common.webview.core

import android.net.Uri
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.lib.jsbridge.BaseJsBridge
import java.net.URLDecoder


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-03-05.
 * Description: 用户处理特殊日志
 *
 */
object WebViewLoggerFile {

    const val MODULE_NAME = "webview"


    fun log(msg: String) {
        LoggerFile.logFile(LoggerFile.getZixieFileLogPathByModule(MODULE_NAME), msg)
    }

    fun getWebviewLogPath(): String {
        return LoggerFile.getZixieFileLogPathByModule(MODULE_NAME);
    }

    fun logCall(uri: Uri?, hostAsMethodName: String, seqid: Int, callbackName: String) {
        log("---------------------- JsBridge call start ----------------------")
        log("uri:" + URLDecoder.decode(uri?.toString()))
        log("hostAsMethodName:$hostAsMethodName")
        log("seqid:$seqid")
        log("callbackName:$callbackName")
        log("---------------------- JsBridge call end ----------------------")
    }

    fun logCallback(function: String, result: String, type: BaseJsBridge.ResponseType) {
        log("---------------------- JsBridge callback start ----------------------")
        log("function:$function")
        log("result:$result")
        log("type:$type")
        log("---------------------- JsBridge callback end ----------------------")
    }
}