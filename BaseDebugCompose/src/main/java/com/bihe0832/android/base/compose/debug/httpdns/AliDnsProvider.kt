package com.bihe0832.android.base.compose.debug.httpdns

import com.bihe0832.android.lib.http.dns.AAFHttpDnsProvider
import com.bihe0832.android.lib.http.dns.DoHResponseParser
import com.bihe0832.android.lib.log.ZLog
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetAddress

/**
 * 阿里公共 DNS (AliDNS) DoH Provider
 *
 * 通过阿里 DoH JSON API 解析域名，端点：`https://dns.alidns.com/resolve`
 *
 * 参考文档：https://alidns.com/
 *
 * @author zixie code@bihe0832.com
 * Created on 2026-06-29
 */
class AliDnsProvider : AAFHttpDnsProvider {

    override val name: String = "AliDNS"

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "AliDnsProvider"
        private const val DOH_ENDPOINT = "https://dns.alidns.com/resolve"
    }

    override fun lookup(hostname: String): List<InetAddress>? {
        return try {
            val url = "$DOH_ENDPOINT?name=$hostname&type=1"
            val request = Request.Builder().url(url).header("Accept", "application/json").build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (body.isNullOrEmpty()) {
                    ZLog.w(TAG, "AliDNS 响应为空: $hostname")
                    return null
                }
                val result = DoHResponseParser.parse(body, name)
                ZLog.d(TAG, "AliDNS 解析 $hostname -> ${result?.map { it.hostAddress }}")
                result
            }
        } catch (e: Exception) {
            ZLog.e(TAG, "AliDNS 解析异常 $hostname: ${e.message}")
            null
        }
    }
}