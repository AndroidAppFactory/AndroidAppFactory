/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/27 下午4:46
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/27 下午4:35
 *
 */

package com.bihe0832.android.lib.okhttp.wrapper.interceptor

import android.text.TextUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.okhttp.wrapper.OkHttpWrapper
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.AAFRequestDataRepository
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.RequestContentDataRecord
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.RequestRecord
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.RequestTraceTimeRecord
import com.bihe0832.android.lib.thread.ThreadManager
import okhttp3.*
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy

open class AAFOkHttpNetworkEventListener(
    protected val enableTrace: Boolean = false,
    protected val enableLog: Boolean = false,
    protected val listener: EventListener?
) : EventListener() {

    private val TAG = "AAFRequest"
    private var mRequestTraceTimeRecord: RequestTraceTimeRecord? = null
    private var mNetworkTraceRequestID: String = ""
    private var mNetworkContentRequestID: String = ""
    private var hasLog = false

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
                mNetworkTraceRequestID,
                call.request().url().toString(),
                call.request().method()
            ).getRecordTraceTimeData()
        }
        saveEvent(RequestTraceTimeRecord.EVENT_CALL_START)
        listener?.callStart(call)
    }

    override fun dnsStart(call: Call, domainName: String) {
        super.dnsStart(call, domainName)
        saveEvent(RequestTraceTimeRecord.EVENT_DNS_START)
        listener?.dnsStart(call, domainName)
    }

    override fun dnsEnd(
        call: Call,
        domainName: String,
        inetAddressList: MutableList<InetAddress>?
    ) {
        super.dnsEnd(call, domainName, inetAddressList)
        saveEvent(RequestTraceTimeRecord.EVENT_DNS_END)
        listener?.dnsEnd(call, domainName, inetAddressList)
    }

    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        super.connectStart(call, inetSocketAddress, proxy)
        saveEvent(RequestTraceTimeRecord.EVENT_CONNECT_START)
        listener?.connectStart(call, inetSocketAddress, proxy)
    }

    override fun secureConnectStart(call: Call) {
        super.secureConnectStart(call)
        saveEvent(RequestTraceTimeRecord.EVENT_SECURE_CONNECT_START)
        listener?.secureConnectStart(call)
    }

    override fun secureConnectEnd(call: Call, handshake: Handshake?) {
        super.secureConnectEnd(call, handshake)
        saveEvent(RequestTraceTimeRecord.EVENT_SECURE_CONNECT_END)
        listener?.secureConnectEnd(call, handshake)
    }

    override fun connectEnd(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy?,
        protocol: Protocol?
    ) {
        super.connectEnd(call, inetSocketAddress, proxy, protocol)
        saveEvent(RequestTraceTimeRecord.EVENT_CONNECT_END)
        listener?.connectEnd(call, inetSocketAddress, proxy, protocol)
    }

    override fun connectFailed(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy?,
        protocol: Protocol?,
        ioe: IOException?
    ) {
        super.connectFailed(call, inetSocketAddress, proxy, protocol, ioe)
        listener?.connectFailed(call, inetSocketAddress, proxy, protocol, ioe)
        doLogAction()
    }

    override fun requestHeadersStart(call: Call) {
        super.requestHeadersStart(call)
        saveEvent(RequestTraceTimeRecord.EVENT_REQUEST_HEADERS_START)
        listener?.requestHeadersStart(call)
    }

    override fun requestHeadersEnd(call: Call, request: Request) {
        super.requestHeadersEnd(call, request)
        saveEvent(RequestTraceTimeRecord.EVENT_REQUEST_HEADERS_END)
        request.header(OkHttpWrapper.HTTP_REQ_PROPERTY_AAF_CONTENT_REQUEST_ID)
            ?.let { contentRequesetID ->
                if (!TextUtils.isEmpty(contentRequesetID)) {
                    ZLog.d(
                        TAG,
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

    override fun requestBodyStart(call: Call) {
        super.requestBodyStart(call)
        saveEvent(RequestTraceTimeRecord.EVENT_REQUEST_BODY_START)
        listener?.requestBodyStart(call)
    }

    override fun requestBodyEnd(call: Call, byteCount: Long) {
        super.requestBodyEnd(call, byteCount)
        saveEvent(RequestTraceTimeRecord.EVENT_REQUEST_BODY_END)
        listener?.requestBodyEnd(call, byteCount)
    }

    override fun responseHeadersStart(call: Call) {
        super.responseHeadersStart(call)
        saveEvent(RequestTraceTimeRecord.EVENT_RESPONSE_HEADERS_START)
        listener?.responseHeadersStart(call)
    }

    override fun responseHeadersEnd(call: Call, response: Response) {
        super.responseHeadersEnd(call, response)
        saveEvent(RequestTraceTimeRecord.EVENT_RESPONSE_HEADERS_END)
        listener?.responseHeadersEnd(call, response)
    }

    override fun responseBodyStart(call: Call) {
        super.responseBodyStart(call)
        saveEvent(RequestTraceTimeRecord.EVENT_RESPONSE_BODY_START)
        listener?.responseBodyStart(call)
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

    @Synchronized
    fun doLogAction() {
        if (!hasLog) {
            hasLog = true
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
        }
    }

    open fun logRequest(record: RequestContentDataRecord) {
        ZLog.d(TAG, record.toString())
    }

    open fun logRequest(record: RequestRecord?) {
        ZLog.d(TAG, record.toString())
    }

    override fun callFailed(call: Call, ioe: IOException) {
        super.callFailed(call, ioe)
        listener?.callFailed(call, ioe)
        doLogAction()
    }

    private fun saveEvent(eventName: String) {
        if (enableTrace) {
            ZLog.d(TAG, eventName)
            mRequestTraceTimeRecord?.saveEvent(eventName)
        }
    }
}