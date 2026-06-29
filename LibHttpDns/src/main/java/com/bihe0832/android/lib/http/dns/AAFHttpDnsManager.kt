package com.bihe0832.android.lib.http.dns

import com.bihe0832.android.lib.log.ZLog
import okhttp3.Dns
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap

/**
 * HTTPDNS 全局管理器
 *
 * 作为应用级 DNS 解析的中枢，职责：
 * - 注入自定义 [AAFHttpDnsProvider]（业务方可接入腾讯云/阿里云等 HTTPDNS 服务）
 * - 内存缓存解析结果，减少网络开销
 * - 自定义 Provider 失败时自动降级到 [SystemDnsProvider]
 * - 提供 OkHttp [Dns] 适配器，可直接注入 OkHttpClient
 *
 * 使用方式：
 * ```kotlin
 * // 应用初始化时
 * AAFHttpDnsManager.init(MyCustomProvider(), AAFDnsConfig())
 * // 直接解析
 * val ips = AAFHttpDnsManager.lookup("api.example.com")
 * // 获取 OkHttp Dns 实例
 * clientBuilder.dns(AAFOkHttpDns)
 * ```
 *
 * @author zixie code@bihe0832.com
 * Created on 2026-06-29
 */
object AAFHttpDnsManager {

    /** 缓存条目 */
    private data class CacheEntry(
        val addresses: List<InetAddress>,
        val timestamp: Long = System.currentTimeMillis()
    )

    /** 自定义 HTTPDNS 提供者 */
    @Volatile
    private var dnsProvider: AAFHttpDnsProvider? = null

    /** 配置 */
    @Volatile
    private var config: AAFDnsConfig? = null

    /** 系统 DNS 降级提供者 */
    private val systemDnsProvider: SystemDnsProvider by lazy { SystemDnsProvider() }

    /** 当前 DNS 提供者名称（用于日志标识） */
    private val currentDnsName: String get() = dnsProvider?.name ?: systemDnsProvider.name

    /** 内存 DNS 缓存 */
    private val cache = ConcurrentHashMap<String, CacheEntry>()

    // ──────────────────────── 初始化 ────────────────────────

    /**
     * 初始化 HTTPDNS 管理器
     *
     * @param provider 自定义 DNS 提供者（如腾讯云 HTTPDNS）
     * @param config DNS 配置
     */
    fun init(provider: AAFHttpDnsProvider, config: AAFDnsConfig = AAFDnsConfig()) {
        this.dnsProvider = provider
        this.config = config
        ZLog.i(TAG, "HTTPDNS 初始化完成: provider=${provider.name}, cacheExpireMs=${config.cacheExpireMs}")

        // 初始化后立即预解析
        if (config.preloadHosts.isNotEmpty()) {
            preload(config.preloadHosts)
        }
    }

    /**
     * 重置 HTTPDNS 管理器，清除自定义 Provider
     *
     * 之后所有解析将降级到系统 DNS。
     */
    fun reset() {
        this.dnsProvider = null
        this.config = null
        cache.clear()
        ZLog.i(TAG, "HTTPDNS 已重置，后续解析将使用系统 DNS")
    }

    // ──────────────────────── 核心解析 ────────────────────────

    /**
     * 解析域名对应的 IP 地址列表
     *
     * 解析优先级：自定义 Provider → 内存缓存 → 系统 DNS
     *
     * @param hostname 待解析的域名
     * @return IP 地址列表，失败返回 null
     */
    fun lookup(hostname: String): List<InetAddress>? {
        if (isIpAddress(hostname)) {
            return try {
                listOf(InetAddress.getByName(hostname))
            } catch (e: Exception) {
                ZLog.e(TAG, "[$currentDnsName] IP 地址解析异常 $hostname: ${e.message}")
                null
            }
        }

        val provider = dnsProvider
        if (provider != null) {
            // 先查缓存
            val cached = getFromCache(hostname)
            if (cached != null) {
                ZLog.d(TAG, "[$currentDnsName] DNS 缓存命中 $hostname -> ${cached.map { it.hostAddress }}")
                return cached
            }

            // 调用自定义 Provider
            try {
                val result = provider.lookup(hostname)
                if (!result.isNullOrEmpty()) {
                    ZLog.d(TAG, "[$currentDnsName] DNS 解析成功 $hostname -> ${result.map { it.hostAddress }}")
                    putCache(hostname, result)
                    return result
                }
                ZLog.w(TAG, "[$currentDnsName] DNS 解析返回空结果 $hostname，降级到系统 DNS")
            } catch (e: Exception) {
                ZLog.e(TAG, "[$currentDnsName] DNS 解析异常 $hostname: ${e.message}，降级到系统 DNS")
            }
        }

        // 降级到系统 DNS
        ZLog.w(TAG, "[$currentDnsName] 降级到系统 DNS 解析 $hostname")
        val sysResult = systemDnsProvider.lookup(hostname)
        if (sysResult != null) {
            putCache(hostname, sysResult)
        }
        return sysResult
    }

    /**
     * 批量预解析域名
     *
     * @param hostnames 待预解析的域名列表
     */
    fun preload(hostnames: List<String>) {
        ZLog.i(TAG, "预解析域名列表: $hostnames")
        for (hostname in hostnames) {
            try {
                lookup(hostname)
            } catch (e: Exception) {
                ZLog.e(TAG, "预解析失败 $hostname: ${e.message}")
            }
        }
    }

    // ──────────────────────── 便捷方法 ────────────────────────

    /**
     * 从 URL 提取 host，通过 HTTPDNS 解析后替换为 IP 地址
     *
     * 用于 [java.net.HttpURLConnection] 直连场景：
     * - 解析成功 → 返回 IP 替换后的新 URL
     * - 解析失败 → 返回原始 URL（不阻塞原有逻辑）
     *
     * @param url 原始 URL（包含域名）
     * @return IP 替换后的 URL，或原始 URL
     */
    fun resolveUrlForConnection(url: String): String {
        return try {
            val host = extractHost(url)
            if (host.isNullOrEmpty() || isIpAddress(host)) {
                return url
            }
            val addresses = lookup(host)
            if (addresses.isNullOrEmpty()) {
                ZLog.w(TAG, "[$currentDnsName] resolveUrlForConnection 解析 $host 失败，使用原始 URL")
                return url
            }
            val ip = addresses[0].hostAddress ?: return url
            val result = url.replaceFirst(host, ip)
            ZLog.d(TAG, "[$currentDnsName] resolveUrlForConnection: $url -> $result")
            result
        } catch (e: Exception) {
            ZLog.e(TAG, "[$currentDnsName] resolveUrlForConnection 异常: ${e.message}")
            url
        }
    }

    // ──────────────────────── OkHttp 适配 ────────────────────────

    /**
     * 获取当前 DNS 提供者名称（用于日志标识）
     *
     * @return 当前 Provider 名称，未注入时返回 "SystemDNS"
     */
    fun getCurrentProviderName(): String = currentDnsName

    /**
     * 获取 OkHttp [Dns] 适配器实例
     *
     * @return OkHttp Dns 实例（[AAFOkHttpDns] object 单例）
     */
    fun getOkHttpDns(): Dns = AAFOkHttpDns

    // ──────────────────────── 缓存管理 ────────────────────────

    /**
     * 清除所有 DNS 缓存
     */
    fun clearCache() {
        cache.clear()
        ZLog.d(TAG, "DNS 缓存已清除")
    }

    /**
     * 从缓存获取解析结果
     */
    private fun getFromCache(hostname: String): List<InetAddress>? {
        val cfg = config ?: return null
        val entry = cache[hostname] ?: return null
        if (System.currentTimeMillis() - entry.timestamp > cfg.cacheExpireMs) {
            cache.remove(hostname)
            ZLog.d(TAG, "DNS 缓存过期 $hostname")
            return null
        }
        return entry.addresses
    }

    /**
     * 写入缓存
     */
    private fun putCache(hostname: String, addresses: List<InetAddress>) {
        cache[hostname] = CacheEntry(addresses)
    }

    // ──────────────────────── 工具方法 ────────────────────────

    /**
     * 判断字符串是否为 IP 地址
     *
     * 支持 IPv4 和 IPv6 格式。
     */
    fun isIpAddress(hostname: String): Boolean {
        return try {
            // IPv4: 包含点分十进制数字
            val isV4 = hostname.matches(Regex("^(\\d{1,3}\\.){3}\\d{1,3}$"))
            if (isV4) true
            // IPv6: 包含冒号
            else hostname.contains(":")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 从 URL 字符串中提取 host
     */
    private fun extractHost(url: String): String? {
        return try {
            val uri = java.net.URI(url)
            uri.host
        } catch (e: Exception) {
            // 非标准 URL，尝试简单提取
            val schemeEnd = url.indexOf("://")
            val start = if (schemeEnd > 0) schemeEnd + 3 else 0
            val pathStart = url.indexOf('/', start)
            val end = if (pathStart > 0) pathStart else url.length
            url.substring(start, end).split(":")[0]
        }
    }
}
