package com.bihe0832.android.lib.download.core

import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.HTTPRequestUtils
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSession

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020/6/19.
 * Description: HTTP 连接相关的扩展方法
 *
 */

/**
 * 为 OkHttp Request.Builder 添加下载所需的通用请求头
 * 
 * 参考 HttpURLConnection.upateRequestInfo 设置相同的请求头
 *
 * @param customHeaders 自定义请求头
 * @return Request.Builder 支持链式调用
 */
fun Request.Builder.addDownloadHeaders(customHeaders: Map<String, String>? = null): Request.Builder {
    // 禁用连接复用（与 HttpURLConnection 保持一致）
    addHeader("Connection", "close")
    // User-Agent
    addHeader("User-Agent", HTTPRequestUtils.USER_AGENT_COMMON_ZIXIE + "Zixie Download/2}")
    // 二进制流类型
    addHeader("Content-Type", "application/octet-stream; charset=UTF-8")
    // 禁用 gzip 压缩，确保 Content-Length 与实际数据长度一致
    addHeader("Accept-Encoding", "identity")
    // 添加自定义请求头
    if (customHeaders.isNullOrEmpty()) {
        ZLog.d(DownloadItem.TAG, "addDownloadHeaders: customHeaders is null or empty")
    } else {
        ZLog.d(DownloadItem.TAG, "addDownloadHeaders: customHeaders size=${customHeaders.size}")
        customHeaders.forEach { (key, value) ->
            if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                ZLog.d(DownloadItem.TAG, "addDownloadHeaders: adding header $key=$value")
                addHeader(key, value)
            } else {
                ZLog.w(DownloadItem.TAG, "addDownloadHeaders: requestHeader is bad, key=$key, value=$value")
            }
        }
    }
    
    return this
}

/**
 * 打印 OkHttp Request 的请求头信息（用于调试）
 *
 * @param msg 日志前缀消息
 */
fun Request.logRequestHeaderFields(msg: String) {
    ZLog.w(DownloadItem.TAG, "$msg  --------------------------------------------")
    ZLog.w(DownloadItem.TAG, "$msg  Request - url:$url ")
    ZLog.w(DownloadItem.TAG, "$msg  Request - method:$method ")
    for ((key, value) in headers) {
        ZLog.w(DownloadItem.TAG, "$msg  Request - :$key - $value ")
    }
    ZLog.w(DownloadItem.TAG, "$msg  --------------------------------------------")
}

/**
 * 打印 OkHttp Response 的响应头信息（用于调试）
 *
 * @param msg 日志前缀消息
 */
fun Response.logResponseHeaderFields(msg: String) {
    ZLog.w(DownloadItem.TAG, "$msg  --------------------------------------------")
    ZLog.w(DownloadItem.TAG, "$msg  Response - responseCode:$code ")
    ZLog.w(DownloadItem.TAG, "$msg  Response - protocol:$protocol ")
    ZLog.w(DownloadItem.TAG, "$msg  Response - contentType:${body?.contentType()} ")
    // 从 header 中读取 Content-Length，使用统一的扩展方法
    ZLog.w(DownloadItem.TAG, "$msg  Response - contentLength:${getContentLength()} ")

    for ((key, value) in headers) {
        ZLog.w(DownloadItem.TAG, "$msg  Response - :$key - $value ")
    }
    ZLog.w(DownloadItem.TAG, "$msg  --------------------------------------------")
}

/**
 * 从 HTTP Response Header 中获取 Content-Length
 *
 * @return Content-Length 值，如果不存在或解析失败则返回 0
 */
fun Response.getContentLength(): Long {
    return header("Content-Length")?.toLongOrNull() ?: 0L
}