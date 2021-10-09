package com.bihe0832.android.lib.download.core

import android.os.Build
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.HTTPRequestUtils
import java.net.HttpURLConnection

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020/6/19.
 * Description: Description
 *
 */

fun HttpURLConnection.upateRequestInfo() {
    connectTimeout = 30000
    readTimeout = 15000
    requestMethod = "GET"
    useCaches = false
    setRequestProperty("Connection", "Keep-Alive")
    setRequestProperty("Content-Type", "application/octet-stream; charset=UTF-8")
    setRequestProperty("Accept-Encoding", "identity")
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2) {
        setRequestProperty("Connection", "close")
    }
}

fun HttpURLConnection.logHeaderFields(msg: String) {
    ZLog.w(DownloadItem.TAG, "$msg  Response - responseCode:$responseCode ")
    ZLog.w(DownloadItem.TAG, "$msg  Response - contentType:$contentType ")
    ZLog.w(DownloadItem.TAG, "$msg  Response - contentLength:${HTTPRequestUtils.getContentLength(this)} ")
    for ((key, value1) in headerFields.entries) {
        var values = ""
        for (value in value1) {
            values += "$value,"
        }
        ZLog.w(DownloadItem.TAG, "$msg  Response - :${key} - $values ")
    }

}