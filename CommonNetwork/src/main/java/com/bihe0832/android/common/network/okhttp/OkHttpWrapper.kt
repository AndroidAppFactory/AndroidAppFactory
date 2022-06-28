/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/27 下午5:50
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/27 下午5:50
 *
 */

package com.bihe0832.android.common.network.okhttp

import android.os.SystemClock
import com.bihe0832.android.common.network.okhttp.interceptor.AAFNetworkEventListener
import com.bihe0832.android.common.network.okhttp.interceptor.AAFOKHttpInterceptor
import com.bihe0832.android.common.network.okhttp.interceptor.data.AAFRequestDataRepository
import com.bihe0832.android.common.network.okhttp.interceptor.data.NetworkRecord
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

    const val HTTP_REQ_PROPERTY_AAF_CONTENT_REQUEST_ID = "AAF-Content-Request-Id"

    private val mRequestIdGenerator by lazy {
        IdGenerator(0)
    }


    private val mRequestRecords: CopyOnWriteArrayList<NetworkRecord> by lazy {
        CopyOnWriteArrayList<NetworkRecord>()
    }

    private fun addRecord(record: NetworkRecord) {
        if (mRequestRecords.size > maxRequestListSize) {
            AAFRequestDataRepository.removeData(mRequestRecords[0].traceRequestId)
            mRequestRecords.removeAt(0)
        }
        mRequestRecords.add(record)
    }

    var maxRequestListSize = 100

    fun getOkHttpClientBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder().apply {
            connectTimeout(TIME_OUT_CONNECTION, TimeUnit.MILLISECONDS)
            readTimeout(TIME_OUT_READ, TimeUnit.MILLISECONDS)
            writeTimeout(TIME_OUT_WRITE, TimeUnit.MILLISECONDS)
            retryOnConnectionFailure(true)
        }
    }

    fun getOkHttpClientBuilderWithInterceptor(isDebug: Boolean): OkHttpClient.Builder {
        return getOkHttpClientBuilder().apply {
            addNetworkInterceptor(generateNetworkInterceptor(isDebug))
            eventListenerFactory(generateNetworkEventListener(isDebug))
        }
    }

    fun generateNetworkInterceptor(isDebug: Boolean): Interceptor {
        return AAFOKHttpInterceptor(isDebug)
    }

    fun generateNetworkEventListener(isDebug: Boolean): EventListener.Factory {
        return generateNetworkEventListener(isDebug, null)
    }

    fun generateNetworkEventListener(isDebug: Boolean, listener: EventListener?): EventListener.Factory {
        return EventListener.Factory { AAFNetworkEventListener(isDebug, listener) }
    }

    fun generateRequestID(): String {
        return mRequestIdGenerator.generate().toString()
    }

    fun getRecord(requestId: String?): NetworkRecord? {
        return mRequestRecords.find { it.traceRequestId == requestId }
    }

    fun getRecord(requestId: String, url: String, method: String): NetworkRecord {
        var record = mRequestRecords.find { it.traceRequestId == requestId }
        if (null == record) {
            record = NetworkRecord(requestId, url, method, SystemClock.elapsedRealtime())
            addRecord(record)
        }
        return record
    }

}