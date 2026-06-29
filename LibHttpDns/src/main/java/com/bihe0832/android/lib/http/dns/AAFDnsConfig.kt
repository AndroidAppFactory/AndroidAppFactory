package com.bihe0832.android.lib.http.dns

/**
 * HTTPDNS 全局配置
 *
 * 控制 DNS 缓存行为和预解析策略。
 *
 * @property cacheExpireMs 缓存过期时间（毫秒），默认 5 分钟
 * @property preloadHosts 应用启动时需要预解析的域名列表
 *
 * @author zixie code@bihe0832.com
 * Created on 2026-06-29
 */
internal const val TAG = "AAFDNS"

data class AAFDnsConfig(
    val cacheExpireMs: Long = DEFAULT_CACHE_EXPIRE_MS,
    val preloadHosts: List<String> = emptyList()
) {
    companion object {
        const val DEFAULT_CACHE_EXPIRE_MS = 5 * 60 * 1000L
    }
}
