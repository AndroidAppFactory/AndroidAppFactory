/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/28
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/28
 *
 */

package com.bihe0832.android.lib.okhttp.wrapper.interceptor.data

import com.bihe0832.android.lib.http.common.core.BaseConnection
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.Protocol
import java.io.Serializable

/**
 * 请求内容数据记录
 *
 * 记录 HTTP 请求和响应的详细内容：
 * - 请求信息：URL、方法、协议、请求头、请求体
 * - 响应信息：状态码、错误信息、响应头、响应体
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/6/28
 *
 * @since 1.0.0
 */
class RequestContentDataRecord : Serializable {

    /** 内容请求 ID，由应用拦截器生成 */
    var contentRequestId: String = ""

    /** 追踪请求 ID，由事件监听器生成 */
    var mTraceRequestId: String = ""

    /** 请求 URL */
    var url: String = ""

    /** 请求方法（GET/POST 等） */
    var method: String = ""

    /** HTTP 协议版本 */
    var protocol: Protocol = Protocol.HTTP_1_1

    /** 请求体长度 */
    var requestBodyLength: String = ""

    /** 请求内容类型 */
    var requestContentType: MediaType? = null

    /** 请求头 */
    var requestHeadersMap: Headers? = null

    /** 请求体内容 */
    var requestBody: String = ""

    /** 响应状态码 */
    var status = -1

    /** 错误信息 */
    var errorMsg: String = ""

    /** 响应体长度 */
    var responseBodyLength: String = ""

    /** 响应内容类型 */
    var responseContentType: MediaType? = null

    /** 响应头 */
    var responseHeadersMap: Headers? = null

    /** 响应体内容 */
    var responseBody: String = ""

    /**
     * 转换为可读的日志字符串
     *
     * @return 格式化的请求/响应信息
     */
    override fun toString(): String {
        return toLogString(
            traceRequestId = mTraceRequestId
        )
    }

    /**
     * 转换为可读的日志字符串（带扩展参数）
     *
     * @param traceRequestId 请求 ID，为空时不展示
     * @param requestCostMs 请求耗时（毫秒），小于 0 时不展示
     * @param totalCostMs 总耗时（毫秒），小于 0 时不展示
     * @param isMockRequest 是否为 Mock 请求
     * @return 格式化的请求/响应信息
     */
    fun toLogString(
        traceRequestId: String,
        requestCostMs: Long = -1,
        totalCostMs: Long = -1,
        isMockRequest: Boolean = false
    ): String {
        val hasRequestId = traceRequestId.isNotEmpty()
        val hasRequestCost = requestCostMs >= 0
        val hasTotalCost = totalCostMs >= 0

        return StringBuffer().apply {
            append("\n \n")
            // 请求行
            append("--> $method $url $protocol")
            if (hasRequestId) append(" AAFRequestID:$traceRequestId")
            append("\n")

            // 请求头
            requestHeadersMap?.let { headers ->
                if (headers.size > 0) {
                    append("$headers\n")
                }
            }

            // 请求体
            if (requestBody.isNotEmpty()) {
                append("$requestBody\n\n")
            } else if (isMockRequest) {
                append("\n (Mock Request - No Body)\n\n")
            } else {
                append("\n (Bad Request - No Body)\n\n")
            }

            // 请求结束行
            if (method.equals(BaseConnection.HTTP_REQ_METHOD_POST, ignoreCase = true)) {
                append("--> END $method")
                if (requestBodyLength.isNotEmpty()) append(" ($requestBodyLength - byte body)")
                if (hasRequestCost) append(" Cost: ${requestCostMs}ms")
                append("\n")
            } else {
                append("--> END $method")
                if (hasRequestCost) append(" Cost: ${requestCostMs}ms)")
                append("\n")
            }

            // 响应行
            append("<-- $status $url $errorMsg\n")

            // 响应头
            responseHeadersMap?.let { headers ->
                if (headers.size > 0) {
                    append("$headers\n")
                }
            }

            // 响应体
            if (responseBody.isNotEmpty()) {
                append("$responseBody\n\n")
            } else if (isMockRequest) {
                append("\n(Mock Request - No Response Body)\n\n")
            }

            // 响应结束行
            append("<-- END HTTP")
            if (isMockRequest) {
                append(" (Mock Request)")
            } else if (responseBodyLength.isNotEmpty()) {
                append(" ($responseBodyLength - byte body)")
            }
            if (hasTotalCost) append(" Total Cost: ${totalCostMs}ms")
            append(" \n")
        }.toString()
    }
}