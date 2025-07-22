package com.bihe0832.android.lib.okhttp.wrapper.interceptor

import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.okhttp.wrapper.OkHttpWrapper
import com.bihe0832.android.lib.okhttp.wrapper.OkHttpWrapper.generateRequestID
import okhttp3.Interceptor
import okhttp3.Response

/**
 * @author zixie code@bihe0832.com Created on 2025/3/27. Description: Description
 */
class AAFOkHttpAppInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestId = generateRequestID()
        ZLog.d(OkHttpWrapper.TAG, "AAFOkHttpAppInterceptor Request ID: $requestId")
        val newRequest = originalRequest.newBuilder()
            .tag(AAFRequestContext::class.java, AAFRequestContext(requestId))
            .header(OkHttpWrapper.HTTP_REQ_PROPERTY_AAF_CONTENT_REQUEST_ID, requestId).build()
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