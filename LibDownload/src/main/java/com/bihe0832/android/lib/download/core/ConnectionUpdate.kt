package com.bihe0832.android.lib.download.core

import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.HTTPRequestUtils
import okhttp3.Response
import java.net.HttpURLConnection
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSession

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020/6/19.
 * Description: Description
 *
 */

fun HttpURLConnection.upateRequestInfo(customProperties: Map<String, String>?) {
    connectTimeout = 5000
    readTimeout = 10000
    requestMethod = "GET"
    useCaches = false
    setRequestProperty("Connection", "close")
    setRequestProperty("User-Agent", HTTPRequestUtils.USER_AGENT_COMMON_ZIXIE + "Zixie Download/1}")
    setRequestProperty("Content-Type", "application/octet-stream; charset=UTF-8")
    setRequestProperty("Accept-Encoding", "identity")
    //用户指定IP的场景
    setRequestProperty("Host", url.host)
    if (this is HttpsURLConnection) {
        hostnameVerifier = object : HostnameVerifier {
            override fun verify(hostname: String, session: SSLSession): Boolean {
                return HttpsURLConnection.getDefaultHostnameVerifier().verify(url.host, session)
            }
        }
    }
    if (customProperties?.isNotEmpty() == true) {
        for ((key, value) in customProperties.entries) {
            if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
                ZLog.d(DownloadItem.TAG, "requestProperty is bad:$key")
            } else {
                setRequestProperty(key, value)
            }
        }
    }
}

fun HttpURLConnection.logRequestHeaderFields(msg: String) {
    for ((key, value) in requestProperties.entries) {
        ZLog.w(DownloadItem.TAG, "$msg  Request - :${key} - $value ")
    }
}

fun HttpURLConnection.logResponseHeaderFields(msg: String) {
    ZLog.w(DownloadItem.TAG, "$msg  Response - responseCode:$responseCode ")
    ZLog.w(DownloadItem.TAG, "$msg  Response - contentType:$contentType ")
    // 从 header 中读取 Content-Length，使用统一的工具方法
    ZLog.w(DownloadItem.TAG, "$msg  Response - contentLength:${HTTPRequestUtils.getContentLength(this)} ")

    for ((key, value1) in headerFields.entries) {
        var values = ""
        for (value in value1) {
            values += "$value,"
        }
        ZLog.w(DownloadItem.TAG, "$msg  Response - :${key} - $values ")
    }
}

/**
 * 打印 OkHttp Response 的响应头信息（用于调试）
 *
 * @param msg 日志前缀消息
 */
fun Response.logResponseHeaderFields(msg: String) {
    ZLog.w(DownloadItem.TAG, "$msg  Response - responseCode:$code ")
    ZLog.w(DownloadItem.TAG, "$msg  Response - protocol:$protocol ")
    ZLog.w(DownloadItem.TAG, "$msg  Response - contentType:${body?.contentType()} ")
    // 从 header 中读取 Content-Length，使用统一的扩展方法
    ZLog.w(DownloadItem.TAG, "$msg  Response - contentLength:${getContentLength()} ")

    for ((key, value) in headers) {
        ZLog.w(DownloadItem.TAG, "$msg  Response - :$key - $value ")
    }
}

/**
 * 从 HTTP Response Header 中获取 Content-Length
 *
 * @return Content-Length 值，如果不存在或解析失败则返回 0
 */
fun Response.getContentLength(): Long {
    return header("Content-Length")?.toLongOrNull() ?: 0L
}