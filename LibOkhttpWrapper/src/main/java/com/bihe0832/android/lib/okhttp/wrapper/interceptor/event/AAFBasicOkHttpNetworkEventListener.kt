/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/27 下午4:46
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/27 下午4:35
 *
 */

package com.bihe0832.android.lib.okhttp.wrapper.interceptor.event

import android.text.TextUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.okhttp.wrapper.OkHttpWrapper
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.AAFRequestContext
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.AAFRequestDataRepository
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.RequestContentDataRecord
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.RequestRecord
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.RequestTraceTimeRecord
import com.bihe0832.android.lib.thread.ThreadManager
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.Protocol
import okhttp3.Request
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy

/**
 * 仅记录请求的关键阶段，日常开发推荐
 */
open class AAFBasicOkHttpNetworkEventListener(
    // 是否统计请求耗时
    protected val enableTrace: Boolean = false,
    // 是否打印基本的请求数据
    protected val enableLog: Boolean = false,
    // 网络事件回调
    protected val listener: EventListener?
) : EventListener() {

    private var mRequestTraceTimeRecord: RequestTraceTimeRecord? = null
    private var mNetworkTraceRequestID: String = ""
    private var mNetworkContentRequestID: String = ""
    private var lastTraceNetworkRequestID = ""

    open fun canTrace(call: Call): Boolean {
        return enableTrace
    }

    fun getNetworkTraceRequestID(): String {
        return mNetworkTraceRequestID
    }

    fun getNetworkContentRequestID(): String {
        return mNetworkContentRequestID
    }


    override fun callStart(call: Call) {
        super.callStart(call)
        mNetworkTraceRequestID = OkHttpWrapper.generateRequestID()
        if (canTrace(call)) {
            mRequestTraceTimeRecord = OkHttpWrapper.getRecord(
                mNetworkTraceRequestID, call.request().url.toString(),
                call.request().method
            ).getRecordTraceTimeData()
        }
        saveEvent(RequestTraceTimeRecord.EVENT_CALL_START)
        listener?.callStart(call)
    }

    override fun requestHeadersEnd(call: Call, request: Request) {
        super.requestHeadersEnd(call, request)
        saveEvent(RequestTraceTimeRecord.EVENT_REQUEST_HEADERS_END)
        request.tag(AAFRequestContext::class.java)?.requestId?.let { contentRequesetID ->
            if (!TextUtils.isEmpty(contentRequesetID)) {
                ZLog.d(
                    OkHttpWrapper.TAG,
                    "Request ID bind contentRequestId:$contentRequesetID, traceID:${mNetworkTraceRequestID}"
                )
                mNetworkContentRequestID = contentRequesetID
                if (canTrace(call)) {
                    mRequestTraceTimeRecord?.contentRequestId = mNetworkContentRequestID
                }
                AAFRequestDataRepository.getNetworkContentDataRecordByContentID(
                    mNetworkContentRequestID
                ).mTraceRequestId = mNetworkTraceRequestID
            }
        }
        listener?.requestHeadersEnd(call, request)
    }

    override fun requestBodyEnd(call: Call, byteCount: Long) {
        super.requestBodyEnd(call, byteCount)
        saveEvent(RequestTraceTimeRecord.EVENT_REQUEST_BODY_END)
        listener?.requestBodyEnd(call, byteCount)
    }

    override fun responseBodyEnd(call: Call, byteCount: Long) {
        super.responseBodyEnd(call, byteCount)
        saveEvent(RequestTraceTimeRecord.EVENT_RESPONSE_BODY_END)
        listener?.responseBodyEnd(call, byteCount)
        ThreadManager.getInstance().start({
            doLogAction()
        }, 500L)
    }

    override fun callEnd(call: Call) {
        super.callEnd(call)
        saveEvent(RequestTraceTimeRecord.EVENT_CALL_END)
        listener?.callEnd(call)
        doLogAction()
    }

    override fun connectFailed(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy,
        protocol: Protocol?,
        ioe: IOException
    ) {
        super.connectFailed(call, inetSocketAddress, proxy, protocol, ioe)
        listener?.connectFailed(call, inetSocketAddress, proxy, protocol, ioe)
        doLogAction()
    }

    @Synchronized
    fun doLogAction() {
        if (lastTraceNetworkRequestID != mNetworkTraceRequestID) {
            if (enableLog) {
                if (enableTrace) {
                    logRequest(OkHttpWrapper.getRecord(mNetworkTraceRequestID))
                } else {
                    logRequest(
                        AAFRequestDataRepository.getNetworkContentDataRecordByContentID(
                            mNetworkContentRequestID
                        )
                    )
                }
            }
            AAFRequestDataRepository.removeData(mNetworkTraceRequestID)
            lastTraceNetworkRequestID = mNetworkTraceRequestID
        }
    }

    open fun logRequest(record: RequestContentDataRecord) {
        ZLog.d(OkHttpWrapper.TAG, record.toString())
    }

    open fun logRequest(record: RequestRecord?) {
        ZLog.d(OkHttpWrapper.TAG, record.toString())
    }

    override fun callFailed(call: Call, ioe: IOException) {
        super.callFailed(call, ioe)
        listener?.callFailed(call, ioe)
        doLogAction()
    }

    protected fun saveEvent(eventName: String) {
        if (enableTrace) {
            ZLog.d(OkHttpWrapper.TAG, eventName)
            mRequestTraceTimeRecord?.saveEvent(eventName)
        }
    }
}