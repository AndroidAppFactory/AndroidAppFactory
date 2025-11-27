/*
 * Created by zixie <code@bihe0832.com> on 2025-01-27
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 2025-01-27
 */

package com.bihe0832.android.lib.okhttp.wrapper

import android.content.Context
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.log.ZLog
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * OkHttp 客户端管理器
 *
 * 提供统一的 OkHttpClient 创建和管理：
 * - 全局客户端单例管理
 * - HTTP/2 多路复用支持
 * - 协议自动检测和降级
 * - 连接池和缓存管理
 *
 * @author zixie code@bihe0832.com
 * Created on 2025-01-27
 * Description: 统一管理 OkHttp 客户端实例和配置
 *
 * @since 7.2.9
 */
object OkHttpClientManager {

    private const val TAG = "OkHttpClientManager"

    /** 默认连接超时时间（毫秒） */
    private const val DEFAULT_CONNECT_TIMEOUT = 30_000L

    /** 默认读取超时时间（毫秒） */
    private const val DEFAULT_READ_TIMEOUT = 30_000L

    /** 默认写入超时时间（毫秒） */
    private const val DEFAULT_WRITE_TIMEOUT = 30_000L

    /** 默认最大空闲连接数 */
    private const val DEFAULT_MAX_IDLE_CONNECTIONS = 5

    /** 默认连接存活时间（分钟） */
    private const val DEFAULT_KEEP_ALIVE_DURATION = 3L

    /** HTTP/2 协议优先的全局客户端 */
    private val http2Client: OkHttpClient by lazy {
        createOkHttpClient(
            protocols = listOf(Protocol.HTTP_2, Protocol.HTTP_1_1),
            connectTimeout = DEFAULT_CONNECT_TIMEOUT,
            readTimeout = DEFAULT_READ_TIMEOUT,
            writeTimeout = DEFAULT_WRITE_TIMEOUT
        )
    }

    /** HTTP/1.1 降级客户端 */
    private val http1Client: OkHttpClient by lazy {
        createOkHttpClient(
            protocols = listOf(Protocol.HTTP_1_1),
            connectTimeout = DEFAULT_CONNECT_TIMEOUT,
            readTimeout = DEFAULT_READ_TIMEOUT,
            writeTimeout = DEFAULT_WRITE_TIMEOUT
        )
    }

    /** 服务器 HTTP/2 支持缓存 */
    private val http2SupportCache = ConcurrentHashMap<String, Boolean>()

    /**
     * 创建 OkHttpClient 实例
     *
     * @param protocols 支持的协议列表
     * @param connectTimeout 连接超时时间（毫秒）
     * @param readTimeout 读取超时时间（毫秒）
     * @param writeTimeout 写入超时时间（毫秒）
     * @param maxIdleConnections 最大空闲连接数
     * @param keepAliveDuration 连接存活时间（分钟）
     * @return OkHttpClient 实例
     */
    fun createOkHttpClient(
        protocols: List<Protocol> = listOf(Protocol.HTTP_2, Protocol.HTTP_1_1),
        connectTimeout: Long = DEFAULT_CONNECT_TIMEOUT,
        readTimeout: Long = DEFAULT_READ_TIMEOUT,
        writeTimeout: Long = DEFAULT_WRITE_TIMEOUT,
        maxIdleConnections: Int = DEFAULT_MAX_IDLE_CONNECTIONS,
        keepAliveDuration: Long = DEFAULT_KEEP_ALIVE_DURATION
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .protocols(protocols)
            .connectionPool(
                ConnectionPool(
                    maxIdleConnections = maxIdleConnections,
                    keepAliveDuration = keepAliveDuration,
                    timeUnit = TimeUnit.MINUTES
                )
            )
            .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
            .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * 创建带缓存和调度器的 OkHttpClient
     *
     * @param context 上下文
     * @param protocols 支持的协议列表
     * @param connectTimeout 连接超时时间（毫秒）
     * @param readTimeout 读取超时时间（毫秒）
     * @param writeTimeout 写入超时时间（毫秒）
     * @param cacheSize 缓存大小（字节），默认 100MB
     * @param maxRequests 全局最大请求数，默认 30
     * @param maxRequestsPerHost 单主机最大请求数，默认 10
     * @return OkHttpClient 实例
     */
    fun createOkHttpClientWithCache(
        context: Context,
        protocols: List<Protocol> = listOf(Protocol.HTTP_2, Protocol.HTTP_1_1),
        connectTimeout: Long = DEFAULT_CONNECT_TIMEOUT,
        readTimeout: Long = DEFAULT_READ_TIMEOUT,
        writeTimeout: Long = DEFAULT_WRITE_TIMEOUT,
        cacheSize: Long = 100 * 1024 * 1024L,
        maxRequests: Int = 30,
        maxRequestsPerHost: Int = 10
    ): OkHttpClient {
        val connectionPool = ConnectionPool(
            DEFAULT_MAX_IDLE_CONNECTIONS,
            DEFAULT_KEEP_ALIVE_DURATION,
            TimeUnit.MINUTES
        )

        val cache = Cache(
            File(ZixieFileProvider.getZixieTempFolder(context) + "http-cache"),
            cacheSize
        )

        val dispatcher = Dispatcher().apply {
            this.maxRequests = maxRequests
            this.maxRequestsPerHost = maxRequestsPerHost
        }

        return OkHttpClient.Builder()
            .protocols(protocols)
            .connectionPool(connectionPool)
            .cache(cache)
            .dispatcher(dispatcher)
            .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
            .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * 根据配置获取适合的客户端
     *
     * @param url 目标 URL
     * @param preferHttp2 是否优先使用 HTTP/2
     * @return OkHttpClient 实例
     */
    fun getClient(url: String, preferHttp2: Boolean = true): OkHttpClient {
        if (!preferHttp2) {
            ZLog.d(TAG, "配置禁用 HTTP/2，使用 HTTP/1.1")
            return http1Client
        }

        val host = extractHost(url)
        val supportsHttp2 = http2SupportCache[host]

        return when (supportsHttp2) {
            true -> {
                ZLog.d(TAG, "$host 使用 HTTP/2 客户端")
                http2Client
            }
            false -> {
                ZLog.d(TAG, "$host 降级使用 HTTP/1.1 客户端")
                http1Client
            }
            null -> {
                // 首次请求，默认尝试 HTTP/2
                ZLog.d(TAG, "$host 首次请求，尝试 HTTP/2")
                http2Client
            }
        }
    }

    /**
     * 执行 HTTP 请求（带协议自动降级）
     *
     * @param request OkHttp 请求对象
     * @param preferHttp2 是否优先使用 HTTP/2
     * @return Response 响应对象
     */
    fun executeRequest(request: Request, preferHttp2: Boolean = true): Response {
        val client = getClient(request.url.toString(), preferHttp2)
        val response = client.newCall(request).execute()

        // 记录实际使用的协议
        val host = request.url.host
        if (!http2SupportCache.containsKey(host)) {
            http2SupportCache[host] = response.protocol == Protocol.HTTP_2
            ZLog.i(TAG, "首次请求 $host 检测到协议: ${response.protocol}")
        }

        return response
    }

    /**
     * 检测服务器是否支持 HTTP/2
     *
     * @param url 目标 URL
     * @return true 支持 HTTP/2，false 不支持
     */
    fun checkHttp2Support(url: String): Boolean {
        val host = extractHost(url)

        // 先查缓存
        http2SupportCache[host]?.let {
            ZLog.d(TAG, "从缓存获取 $host HTTP/2 支持: $it")
            return it
        }

        return try {
            val request = Request.Builder()
                .url(url)
                .head()
                .build()

            http2Client.newCall(request).execute().use { response ->
                val supportsHttp2 = response.protocol == Protocol.HTTP_2
                http2SupportCache[host] = supportsHttp2
                ZLog.i(TAG, "服务器 $host 协议检测: ${response.protocol}, HTTP/2支持: $supportsHttp2")
                supportsHttp2
            }
        } catch (e: Exception) {
            ZLog.e(TAG, "HTTP/2 支持检测失败: ${e.message}")
            e.printStackTrace()
            http2SupportCache[host] = false
            false
        }
    }

    /**
     * 清除 HTTP/2 支持缓存
     */
    fun clearProtocolCache() {
        http2SupportCache.clear()
        ZLog.d(TAG, "HTTP/2 支持缓存已清除")
    }

    /**
     * 获取当前支持 HTTP/2 的服务器列表
     *
     * @return 支持 HTTP/2 的主机名列表
     */
    fun getHttp2SupportedHosts(): List<String> {
        return http2SupportCache.filter { it.value }.keys.toList()
    }

    /**
     * 从 URL 提取主机名
     *
     * @param url URL 字符串
     * @return 主机名
     */
    private fun extractHost(url: String): String {
        return try {
            val uri = java.net.URI(url)
            uri.host ?: url
        } catch (e: Exception) {
            url
        }
    }
}
