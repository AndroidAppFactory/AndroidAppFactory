package com.bihe0832.android.lib.http.dns

import com.bihe0832.android.lib.log.ZLog
import java.net.InetAddress

/**
 * 系统 DNS 提供者（降级方案）
 *
 * 当自定义 HTTPDNS Provider 解析失败时，自动降级使用系统 DNS。
 * 确保基础网络解析能力不受影响。
 *
 * @author zixie code@bihe0832.com
 * Created on 2026-06-29
 */
class SystemDnsProvider : AAFHttpDnsProvider {
    private val TAG = "SystemDnsProvider"
    override val name: String = "SystemDNS"

    override fun lookup(hostname: String): List<InetAddress>? {
        return try {
            val addresses = InetAddress.getAllByName(hostname)
            ZLog.d(TAG, "系统 DNS 解析 $hostname -> ${addresses.toList()}")
            addresses.filterNotNull()
        } catch (e: Exception) {
            ZLog.e(TAG, "系统 DNS 解析失败 $hostname: ${e.message}")
            null
        }
    }
}
