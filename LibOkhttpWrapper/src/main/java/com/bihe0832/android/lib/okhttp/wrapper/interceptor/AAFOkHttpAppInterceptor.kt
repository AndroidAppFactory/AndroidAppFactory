package com.bihe0832.android.lib.okhttp.wrapper.interceptor

import android.text.TextUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.okhttp.wrapper.OkHttpWrapper
import com.bihe0832.android.lib.okhttp.wrapper.OkHttpWrapper.generateRequestID
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.net.URLDecoder

/**
 * AAF 应用层拦截器
 *
 * 用于在请求发送前添加请求 ID，并支持调试模式下的请求拦截和 Mock 响应：
 * - 为每个请求生成唯一 ID，用于追踪和关联
 * - 调试模式下支持请求延迟
 * - 调试模式下支持 Mock 响应数据，模拟延迟等
 *
 * @param isDebug 是否开启调试模式，开启后会根据请求 header 拦截并构造响应
 * @param enableIntercept 是否开启请求拦截并记录日志信息（即使在 Mock 场景下也会记录）
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/3/27
 *
 * @since 1.0.0
 */
class AAFOkHttpAppInterceptor(
    // 是否开启调试，开启后，会根据请求header拦截并构造请求
    private var isDebug: Boolean = false
) : Interceptor {

    /**
     * 拦截请求
     *
     * 处理流程：
     * 1. 生成唯一请求 ID 并添加到请求头
     * 2. 调试模式下，根据 AAF-Content-Request-Delay 头延迟响应
     * 3. 调试模式下，根据 AAF-Content-Request-Data 头返回 Mock 数据
     * 4. 正常情况下继续执行请求
     *
     * @param chain 拦截器链
     * @return 响应对象
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestId = generateRequestID()
        ZLog.d(OkHttpWrapper.TAG, "AAFOkHttpAppInterceptor Request ID: $requestId")
        var requestBuilder = originalRequest.newBuilder()
            .tag(AAFRequestContext::class.java, AAFRequestContext(requestId))
            .header(OkHttpWrapper.HTTP_REQ_PROPERTY_AAF_CONTENT_REQUEST_ID, requestId)
        // 调试模式下禁用 gzip 压缩，方便查看原始响应内容
        if (isDebug) {
            requestBuilder = requestBuilder.header("Accept-Encoding", "identity")
        }
        
        val newRequest = requestBuilder.build()
        if (isDebug) {
            val delayHeader =
                originalRequest.header(OkHttpWrapper.HTTP_REQ_PROPERTY_AAF_CONTENT_REQUEST_DELAY)
            val delayMs = delayHeader?.toLongOrNull() ?: 0L
            if (delayMs > 0) {
                try {
                    Thread.sleep(delayMs)
                    ZLog.d(OkHttpWrapper.TAG, "响应根据请求Header 延迟 ${delayMs}ms 后返回")
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
        }
        if (isDebug) {
            val mockData =
                originalRequest.header(OkHttpWrapper.HTTP_REQ_PROPERTY_AAF_CONTENT_REQUEST_DATA)
                    ?: ""
            if (!TextUtils.isEmpty(mockData)) {
                return Response.Builder()
                    .request(newRequest)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("AAF OkHttpAppInterceptor MockData After Intercept Request")
                    .body(URLDecoder.decode(mockData).toResponseBody("application/json".toMediaType()))
                    .build()
            }
        }

        val response = chain.proceed(newRequest)
        ZLog.d(
            OkHttpWrapper.TAG,
            "AAFOkHttpAppInterceptor Request ID (Rresponse): " + response.request.tag(
                AAFRequestContext::class.java
            )?.requestId
        )
        return response
    }
}