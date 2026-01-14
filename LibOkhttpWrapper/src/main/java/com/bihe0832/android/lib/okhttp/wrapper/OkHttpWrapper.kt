/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/27 下午5:50
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/27 下午5:50
 *
 */

package com.bihe0832.android.lib.okhttp.wrapper

import android.content.Context
import android.os.SystemClock
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.AAFOKHttpInterceptor
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.AAFOkHttpAppInterceptor
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.AAFRequestDataRepository
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.RequestContentDataRecord
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.RequestRecord
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.event.AAFBasicOkHttpNetworkEventListener
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.event.AAFOkHttpNetworkEventListener
import com.bihe0832.android.lib.utils.IdGenerator
import okhttp3.EventListener
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

/** 默认读取超时时间（毫秒） */
const val TIME_OUT_READ = 5000L

/** 默认连接超时时间（毫秒） */
const val TIME_OUT_CONNECTION = 5000L

/** 默认写入超时时间（毫秒） */
const val TIME_OUT_WRITE = 5000L

/**
 * OkHttp 请求封装工具类
 *
 * 提供 OkHttpClient 的创建、配置和请求记录管理功能：
 * - 创建带拦截器和事件监听的 OkHttpClient
 * - 管理请求记录缓存
 * - 生成唯一请求 ID
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/6/27
 *
 * @since 1.0.0
 */
object OkHttpWrapper {

    /** 日志 TAG */
    const val TAG = "AAFRequest"

    /** 请求头：请求 ID，用于追踪请求 */
    const val HTTP_REQ_PROPERTY_AAF_CONTENT_REQUEST_ID = "AAF-Content-Request-Id"

    /** 请求头：请求延迟时间（毫秒），调试模式下生效 */
    const val HTTP_REQ_PROPERTY_AAF_CONTENT_REQUEST_DELAY = "AAF-Content-Request-Delay"

    /** 请求头：Mock 响应数据，调试模式下生效 */
    const val HTTP_REQ_PROPERTY_AAF_CONTENT_REQUEST_DATA = "AAF-Content-Request-Data"

    /** 请求记录缓存最大数量 */
    private var maxRequestListSize = 20

    /** 请求 ID 生成器 */
    private val mRequestIdGenerator by lazy {
        IdGenerator(0)
    }

    /** 请求记录缓存列表 */
    private val mRequestRecords: CopyOnWriteArrayList<RequestRecord> by lazy {
        CopyOnWriteArrayList<RequestRecord>()
    }

    /**
     * 添加请求记录到缓存
     *
     * 当缓存数量超过最大值时，会移除最早的记录
     *
     * @param record 请求记录
     */
    private fun addRecord(record: RequestRecord) {
        if (mRequestRecords.size > maxRequestListSize) {
            AAFRequestDataRepository.removeData(mRequestRecords[0].traceRequestId)
            mRequestRecords.removeAt(0)
        }
        mRequestRecords.add(record)
    }

    /**
     * 设置请求记录缓存的最大数量
     *
     * 建议不超过 20，如果请求比较复杂且内容较多，会导致内存占用偏高
     *
     * @param cacheMaxRequest 最大缓存数量，必须大于 0
     */
    fun setMaxRequestNumInRequestCacheList(cacheMaxRequest: Int) {
        if (cacheMaxRequest > 0) {
            maxRequestListSize = cacheMaxRequest
        }
    }

    /**
     * 创建 OkHttpClient.Builder
     *
     * 使用 OkHttpClientManager 创建带缓存的基础客户端，并添加应用拦截器
     *
     * @param context 上下文
     * @param connectTimeout 连接超时时间（毫秒）
     * @param readTimeout 读取超时时间（毫秒）
     * @param writeTimeout 写入超时时间（毫秒）
     * @param canInterceptRequest 是否开启请求拦截（调试模式）
     * @return OkHttpClient.Builder 实例
     */
    fun getOkHttpClientBuilder(
        context: Context,
        connectTimeout: Long,
        readTimeout: Long,
        writeTimeout: Long,
        canInterceptRequest: Boolean
    ): OkHttpClient.Builder {
        // 使用 OkHttpClientManager 创建基础客户端
        return OkHttpClientManager.createOkHttpClientWithCache(
            context = context,
            connectTimeout = connectTimeout,
            readTimeout = readTimeout,
            writeTimeout = writeTimeout
        ).newBuilder().apply {
            // 添加应用拦截器（isDebug 和 enableIntercept 都使用 canInterceptRequest）
            addInterceptor(AAFOkHttpAppInterceptor(canInterceptRequest))
        }
    }

    /**
     * 创建 OkHttpClient.Builder（使用默认超时配置）
     *
     * @param context 上下文
     * @param canInterceptRequest 是否开启请求拦截（调试模式）
     * @return OkHttpClient.Builder 实例
     */
    fun getOkHttpClientBuilder(
        context: Context,
        canInterceptRequest: Boolean
    ): OkHttpClient.Builder {
        return getOkHttpClientBuilder(
            context,
            TIME_OUT_CONNECTION,
            TIME_OUT_READ,
            TIME_OUT_WRITE,
            canInterceptRequest
        )
    }

    /**
     * 生成网络拦截器
     *
     * 用于拦截和记录请求/响应数据
     *
     * @param enableIntercept 是否开启拦截并记录请求信息
     * @return 网络拦截器实例
     */
    fun generateNetworkInterceptor(enableIntercept: Boolean): Interceptor {
        return AAFOKHttpInterceptor(enableIntercept)
    }

    /**
     * 生成网络事件监听器工厂（详细模式）
     *
     * 记录请求的详细阶段耗时，适用于问题定位
     *
     * @param enableTrace 是否开启耗时统计
     * @return EventListener.Factory 实例
     */
    fun generateNetworkEventListener(enableTrace: Boolean): EventListener.Factory {
        return generateNetworkEventListener(enableTrace, enableTrace, null)
    }

    /**
     * 生成网络事件监听器工厂（详细模式）
     *
     * 记录请求的详细阶段耗时，包括 DNS、连接、TLS 握手等
     *
     * @param enableTrace 是否开启耗时统计
     * @param enableLog 是否打印日志
     * @param listener 自定义事件监听器
     * @return EventListener.Factory 实例
     */
    fun generateNetworkEventListener(
        enableTrace: Boolean,
        enableLog: Boolean,
        listener: EventListener?
    ): EventListener.Factory {
        return EventListener.Factory {
            AAFOkHttpNetworkEventListener(
                enableTrace, enableLog, listener
            )
        }
    }

    /**
     * 创建带完整拦截器的 OkHttpClient.Builder（详细模式）
     *
     * 包含网络拦截器和详细事件监听器，适用于问题定位
     *
     * @param context 上下文
     * @param enableTraceAndIntercept 是否开启追踪和拦截
     * @return OkHttpClient.Builder 实例
     */
    fun getOkHttpClientBuilderWithInterceptor(
        context: Context,
        enableTraceAndIntercept: Boolean
    ): OkHttpClient.Builder {
        return getOkHttpClientBuilder(context, enableTraceAndIntercept).apply {
            addNetworkInterceptor(generateNetworkInterceptor(enableTraceAndIntercept))
            eventListenerFactory(
                generateNetworkEventListener(
                    enableTraceAndIntercept, enableTraceAndIntercept, null
                )
            )
        }
    }

    /**
     * 生成基础网络事件监听器工厂
     *
     * 仅记录请求的关键阶段，适用于日常开发
     *
     * @param enableTrace 是否开启耗时统计
     * @param enableLog 是否打印日志
     * @param listener 自定义事件监听器
     * @return EventListener.Factory 实例
     */
    fun generateBasicNetworkEventListener(
        enableTrace: Boolean,
        enableLog: Boolean,
        listener: EventListener?
    ): EventListener.Factory {
        return EventListener.Factory {
            AAFBasicOkHttpNetworkEventListener(
                enableTrace, enableLog, listener
            )
        }
    }

    /**
     * 创建带基础拦截器的 OkHttpClient.Builder
     *
     * 包含网络拦截器和基础事件监听器，适用于日常开发
     *
     * @param context 上下文
     * @param enableTraceAndIntercept 是否开启追踪和拦截
     * @return OkHttpClient.Builder 实例
     */
    fun getOkHttpClientBuilderWithBasicInterceptor(
        context: Context,
        enableTraceAndIntercept: Boolean
    ): OkHttpClient.Builder {
        return getOkHttpClientBuilder(context, enableTraceAndIntercept).apply {
            addNetworkInterceptor(generateNetworkInterceptor(enableTraceAndIntercept))
            eventListenerFactory(
                generateBasicNetworkEventListener(
                    enableTraceAndIntercept, enableTraceAndIntercept, null
                )
            )
        }
    }

    /**
     * 创建带基础拦截器的 OkHttpClient.Builder（自定义超时）
     *
     * @param context 上下文
     * @param connectTimeout 连接超时时间（毫秒）
     * @param readTimeout 读取超时时间（毫秒）
     * @param writeTimeout 写入超时时间（毫秒）
     * @param enableTraceAndIntercept 是否开启追踪和拦截
     * @return OkHttpClient.Builder 实例
     */
    fun getOkHttpClientBuilderWithBasicInterceptor(
        context: Context,
        connectTimeout: Long,
        readTimeout: Long,
        writeTimeout: Long,
        enableTraceAndIntercept: Boolean
    ): OkHttpClient.Builder {
        return getOkHttpClientBuilder(
            context,
            connectTimeout,
            readTimeout,
            writeTimeout,
            enableTraceAndIntercept
        ).apply {
            addNetworkInterceptor(generateNetworkInterceptor(enableTraceAndIntercept))
            eventListenerFactory(
                generateBasicNetworkEventListener(
                    enableTraceAndIntercept, enableTraceAndIntercept, null
                )
            )
        }
    }

    /**
     * 生成唯一的请求 ID
     *
     * @return 请求 ID 字符串
     */
    fun generateRequestID(): String {
        return mRequestIdGenerator.generate().toString()
    }

    /**
     * 根据请求 ID 获取请求记录
     *
     * @param requestId 请求 ID
     * @return 请求记录，不存在时返回 null
     */
    fun getRecord(requestId: String?): RequestRecord? {
        return mRequestRecords.find { it.traceRequestId == requestId }
    }

    /**
     * 获取或创建请求记录
     *
     * 如果记录不存在，会创建新记录并添加到缓存
     *
     * @param requestId 请求 ID
     * @param url 请求 URL
     * @param method 请求方法（GET/POST 等）
     * @return 请求记录
     */
    fun getRecord(requestId: String, url: String, method: String): RequestRecord {
        var record = mRequestRecords.find { it.traceRequestId == requestId }
        if (null == record) {
            record = RequestRecord(requestId, url, method, SystemClock.elapsedRealtime())
            addRecord(record)
        }
        return record
    }

}