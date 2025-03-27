/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/27 下午5:50
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/27 下午5:50
 *
 */

package com.bihe0832.android.lib.okhttp.wrapper

import android.os.SystemClock
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.event.AAFBasicOkHttpNetworkEventListener
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.AAFOKHttpInterceptor
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.event.AAFOkHttpNetworkEventListener
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.AAFOkHttpAppInterceptor
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.AAFRequestDataRepository
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.RequestRecord
import com.bihe0832.android.lib.utils.IdGenerator
import okhttp3.EventListener
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit


const val TIME_OUT_READ = 5000L
const val TIME_OUT_CONNECTION = 5000L
const val TIME_OUT_WRITE = 5000L


object OkHttpWrapper {
    const val TAG = "AAFRequest"
    const val HTTP_REQ_PROPERTY_AAF_CONTENT_REQUEST_ID = "AAF-Content-Request-Id"
    private var maxRequestListSize = 50

    private val mRequestIdGenerator by lazy {
        IdGenerator(0)
    }


    private val mRequestRecords: CopyOnWriteArrayList<RequestRecord> by lazy {
        CopyOnWriteArrayList<RequestRecord>()
    }

    private fun addRecord(record: RequestRecord) {
        if (mRequestRecords.size > maxRequestListSize) {
            AAFRequestDataRepository.removeData(mRequestRecords[0].traceRequestId)
            mRequestRecords.removeAt(0)
        }
        mRequestRecords.add(record)
    }

    // 建议不超过 0，如果请求比较复杂且内容较多，会导致内存占用偏高
    fun setMaxRequestNumInRequestCacheList(cacheMaxRequest: Int) {
        if (cacheMaxRequest > 0) {
            maxRequestListSize = cacheMaxRequest
        }
    }

    fun getOkHttpClientBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder().apply {
            connectTimeout(TIME_OUT_CONNECTION, TimeUnit.MILLISECONDS)
            readTimeout(TIME_OUT_READ, TimeUnit.MILLISECONDS)
            writeTimeout(TIME_OUT_WRITE, TimeUnit.MILLISECONDS)
            retryOnConnectionFailure(true)
            addInterceptor(AAFOkHttpAppInterceptor())
        }
    }

    fun getOkHttpClientBuilderWithInterceptor(enableTraceAndIntercept: Boolean): OkHttpClient.Builder {
        return getOkHttpClientBuilder().apply {
            addNetworkInterceptor(AAFOKHttpInterceptor(enableTraceAndIntercept))
            eventListenerFactory(
                generateOkHttpNetworkEventListener(
                    enableTraceAndIntercept,
                    enableTraceAndIntercept,
                    null
                )
            )
        }
    }

    fun getBasicOkHttpClientBuilderWithInterceptor(enableTraceAndIntercept: Boolean): OkHttpClient.Builder {
        return getOkHttpClientBuilder().apply {
            addNetworkInterceptor(AAFOKHttpInterceptor(enableTraceAndIntercept))
            eventListenerFactory(
                generateBasicOkHttpNetworkEventListener(
                    enableTraceAndIntercept,
                    enableTraceAndIntercept,
                    null
                )
            )
        }
    }

    fun generateNetworkInterceptor(enableIntercept: Boolean): Interceptor {
        return AAFOKHttpInterceptor(enableIntercept)
    }

    fun generateOkHttpNetworkEventListener(enableTrace: Boolean): EventListener.Factory {
        return generateOkHttpNetworkEventListener(enableTrace, enableTrace, null)
    }

    fun generateOkHttpNetworkEventListener(
        enableTrace: Boolean,
        enableLog: Boolean,
        listener: EventListener?
    ): EventListener.Factory {
        return EventListener.Factory {
            AAFOkHttpNetworkEventListener(
                enableTrace,
                enableLog,
                listener
            )
        }
    }

    fun generateBasicOkHttpNetworkEventListener(
        enableTrace: Boolean,
        enableLog: Boolean,
        listener: EventListener?
    ): EventListener.Factory {
        return EventListener.Factory {
            AAFBasicOkHttpNetworkEventListener(
                enableTrace,
                enableLog,
                listener
            )
        }
    }

    fun generateRequestID(): String {
        return mRequestIdGenerator.generate().toString()
    }

    fun getRecord(requestId: String?): RequestRecord? {
        return mRequestRecords.find { it.traceRequestId == requestId }
    }

    fun getRecord(requestId: String, url: String, method: String): RequestRecord {
        var record = mRequestRecords.find { it.traceRequestId == requestId }
        if (null == record) {
            record = RequestRecord(requestId, url, method, SystemClock.elapsedRealtime())
            addRecord(record)
        }
        return record
    }

}