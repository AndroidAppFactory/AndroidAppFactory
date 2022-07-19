package com.bihe0832.android.lib.download.core

import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.HTTPRequestUtils
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

fun HttpsURLConnection.upateRequestInfo() {
    connectTimeout = 5000
    readTimeout = 10000
    requestMethod = "GET"
    useCaches = false
    setRequestProperty("Connection", "close")
    setRequestProperty("User-Agent", " Mozilla/5.0 (Linux; Android 10; UNKnown) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/89.0.4389.72 Mobile Safari/537.36/ Zixie Download/1}")
    setRequestProperty("Content-Type", "application/octet-stream; charset=UTF-8")
    setRequestProperty("Accept-Encoding", "identity")
    //用户指定IP的场景
    setRequestProperty("Host", url.host)
    hostnameVerifier = object : HostnameVerifier {
        override fun verify(hostname: String?, session: SSLSession?): Boolean {
            return HttpsURLConnection.getDefaultHostnameVerifier().verify(url.host, session)
        }
    }
}

fun HttpsURLConnection.logHeaderFields(msg: String) {

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