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

/**
 * @author zixie code@bihe0832.com Created on 2025/3/27. Description: Description
 */
class AAFOkHttpAppInterceptor(
    // 是否开启调试，开启后，会根据请求header拦截并构造请求
    private var isDebug: Boolean = false
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestId = generateRequestID()
        ZLog.d(OkHttpWrapper.TAG, "AAFOkHttpAppInterceptor Request ID: $requestId")
        val newRequest = originalRequest.newBuilder()
            .tag(AAFRequestContext::class.java, AAFRequestContext(requestId))
            .header(OkHttpWrapper.HTTP_REQ_PROPERTY_AAF_CONTENT_REQUEST_ID, requestId).build()
        if (isDebug) {
            val delayHeader =
                originalRequest.header(OkHttpWrapper.HTTP_REQ_PROPERTY_AAF_CONTENT_REQUEST_DELAY)
            val delayMs = delayHeader?.toLongOrNull() ?: 0L
            try {
                Thread.sleep(delayMs)
                ZLog.d(OkHttpWrapper.TAG, "响应根据请求Header 延迟 ${delayMs}ms 后返回")
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
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
                    .body(mockData.toResponseBody("application/json".toMediaType()))
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