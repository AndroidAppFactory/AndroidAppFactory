/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/27 下午8:40
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/27 下午8:40
 *
 */
package com.bihe0832.android.lib.okhttp.wrapper.interceptor

import com.bihe0832.android.lib.okhttp.wrapper.ext.getRequestParams
import com.bihe0832.android.lib.okhttp.wrapper.ext.getResponseData
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.AAFRequestDataRepository.getNetworkContentDataRecordByContentID
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.RequestContentDataRecord
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response

/**
 * AAF 网络拦截器
 *
 * 用于拦截和记录 HTTP 请求/响应的详细信息：
 * - 记录请求 URL、方法、协议
 * - 记录请求头和请求体
 * - 记录响应状态、响应头和响应体
 * - 支持自定义请求/响应拦截处理
 *
 * 注意：此拦截器应作为网络拦截器（addNetworkInterceptor）添加
 *
 * @param enableIntercept 是否开启请求拦截并记录日志信息
 * @param enableLog 是否开启异常日志打印
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/6/27
 *
 * @since 1.0.0
 */
open class AAFOKHttpInterceptor(
    private var enableIntercept: Boolean = false,
    private var enableLog: Boolean = true
) : Interceptor {

    /**
     * 拦截响应
     *
     * 子类可重写此方法对响应进行自定义处理
     *
     * @param requestId 请求 ID
     * @param response 原始响应
     * @return 处理后的响应
     */
    protected fun interceptResponse(requestId: String?, response: Response): Response {
        return response
    }

    /**
     * 拦截请求
     *
     * 子类可重写此方法对请求进行自定义处理
     *
     * @param requestId 请求 ID
     * @param request 原始请求
     * @return 处理后的请求
     */
    protected fun interceptRequest(requestId: String?, request: Request): Request {
        return request
    }

    /**
     * 执行拦截
     *
     * 记录请求和响应的详细信息到 RequestContentDataRecord
     *
     * @param chain 拦截器链
     * @return 响应对象
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        var requestContentDataRecord: RequestContentDataRecord? = null
        val requestId = chain.request().tag(AAFRequestContext::class.java)?.requestId ?: ""
        val request = interceptRequest(requestId, chain.request())
        if (enableIntercept) {
            requestContentDataRecord = getNetworkContentDataRecordByContentID(requestId)
            val connection = chain.connection()
            val protocol = connection?.protocol() ?: Protocol.HTTP_1_1
            requestContentDataRecord.url = request.url.toString()
            requestContentDataRecord.method = request.method
            requestContentDataRecord.protocol = protocol
            requestContentDataRecord.requestHeadersMap = request.headers
            val requestBody = request.body
            if (requestBody != null) {
                val contentLength = requestBody.contentLength()
                requestContentDataRecord.requestBodyLength =
                    if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
                if (requestBody.contentType() != null) {
                    requestContentDataRecord.requestContentType = requestBody.contentType()
                }
                if (requestBody.contentType() != MultipartBody.FORM && contentLength < 500 * 1024) {
                    requestContentDataRecord.requestBody = request.getRequestParams(enableLog)
                } else {
                    requestContentDataRecord.requestBody =
                        "！！！AAF Record UnSupport Request , type: " + requestBody.contentType()
                }
            }
        }

        val response: Response
        try {
            response = interceptResponse(requestId, chain.proceed(request))
        } catch (var25: Exception) {
            if (enableIntercept) {
                requestContentDataRecord?.errorMsg = var25.toString()
            }
            throw var25
        }

        if (enableIntercept) {
            requestContentDataRecord?.responseHeadersMap = response.headers
            requestContentDataRecord?.status = response.code
            requestContentDataRecord?.errorMsg = response.message
            val responseBody = response.body
            if (responseBody != null) {
                val contentLength = responseBody.contentLength()
                requestContentDataRecord?.responseBodyLength =
                    if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
                if (responseBody.contentType() != null) {
                    requestContentDataRecord?.responseContentType = responseBody.contentType()
                }
                if (contentLength < 500 * 1024) {
                    requestContentDataRecord?.responseBody = response.getResponseData(enableLog)
                } else {
                    requestContentDataRecord?.requestBody =
                        "！！！AAF Record UnSupport Response , type: $contentLength"
                }
            }
        }
        return response
    }
}