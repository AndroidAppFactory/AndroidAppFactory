package com.bihe0832.android.lib.http.dns

import okhttp3.Dns
import java.net.InetAddress

/**
 * OkHttp [Dns] 接口适配器
 *
 * 将 [AAFHttpDnsManager] 的解析能力桥接到 OkHttp 体系，
 * 可直接通过 `OkHttpClient.Builder.dns(AAFOkHttpDns)` 注入。
 *
 * @author zixie code@bihe0832.com
 * Created on 2026-06-29
 */
object AAFOkHttpDns : Dns {

    override fun lookup(hostname: String): List<InetAddress> {
        return try {
            AAFHttpDnsManager.lookup(hostname)
                ?: Dns.SYSTEM.lookup(hostname)
        } catch (e: Exception) {
            Dns.SYSTEM.lookup(hostname)
        }
    }
}
