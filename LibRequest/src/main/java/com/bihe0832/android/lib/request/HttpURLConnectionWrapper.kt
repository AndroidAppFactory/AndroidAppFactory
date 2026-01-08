package com.bihe0832.android.lib.request

import android.text.TextUtils
import com.bihe0832.android.lib.log.ZLog
import java.net.HttpURLConnection

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2026/1/8.
 * Description: Description
 *
 */

fun HttpURLConnection.upateRequestInfo(customProperties: Map<String, String>?) {
    connectTimeout = HTTPRequestUtils.CONNECT_TIMEOUT
    readTimeout = HTTPRequestUtils.DEFAULT_READ_TIMEOUT
    requestMethod = "GET"
    useCaches = false
    setRequestProperty("Connection", "close")
    setRequestProperty("User-Agent", HTTPRequestUtils.USER_AGENT_COMMON_ZIXIE)
    setRequestProperty("Content-Type", "application/octet-stream; charset=UTF-8")
    setRequestProperty("Accept-Encoding", "identity")
    // 添加自定义请求头
    if (customProperties.isNullOrEmpty()) {
        ZLog.d(HTTPRequestUtils.TAG, "addDownloadHeaders: customHeaders is null or empty")
    } else {
        ZLog.d(
            HTTPRequestUtils.TAG, "addDownloadHeaders: customHeaders size=${customProperties.size}"
        )
        customProperties.forEach { (key, value) ->
            if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                ZLog.d(HTTPRequestUtils.TAG, "addDownloadHeaders: adding header $key=$value")
                setRequestProperty(key, value)
            } else {
                ZLog.w(
                    HTTPRequestUtils.TAG,
                    "addDownloadHeaders: requestHeader is bad, key=$key, value=$value"
                )
            }
        }
    }
}

fun HttpURLConnection.logRequestHeaderFields(tag: String, msg: String) {
    for ((key, value) in requestProperties.entries) {
        ZLog.w(tag, "$msg  Request - :${key} - $value ")
    }
}

fun HttpURLConnection.logResponseHeaderFields(tag: String, msg: String) {
    ZLog.w(tag, "$msg  Response - responseCode:$responseCode ")
    ZLog.w(tag, "$msg  Response - contentType:$contentType ")
    // 从 header 中读取 Content-Length，使用统一的工具方法
    ZLog.w(
        tag, "$msg  Response - contentLength:${HTTPRequestUtils.getContentLength(this)} "
    )

    for ((key, value1) in headerFields.entries) {
        var values = ""
        for (value in value1) {
            values += "$value,"
        }
        ZLog.w(tag, "$msg  Response - :${key} - $values ")
    }
}