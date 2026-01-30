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
 * 基础网络事件监听器
 *
 * 仅记录请求的关键阶段，适用于日常开发：
 * - callStart：请求开始
 * - requestHeadersEnd：请求头发送完成
 * - requestBodyEnd：请求体发送完成
 * - responseBodyEnd：响应体接收完成
 * - callEnd：请求结束
 * - callFailed：请求失败
 *
 * @param enableTrace 是否统计请求耗时
 * @param enableLog 是否打印基本的请求数据
 * @param listener 自定义网络事件回调
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/6/27
 *
 * @since 1.0.0
 */
open class AAFBasicOkHttpNetworkEventListener(
    protected val enableTrace: Boolean = false,
    protected val enableLog: Boolean = false,
    protected val listener: EventListener?
) : EventListener() {

    /** 耗时追踪记录 */
    private var mRequestTraceTimeRecord: RequestTraceTimeRecord? = null

    /** 追踪请求 ID */
    private var mNetworkTraceRequestID: String = ""

    /** 内容请求 ID */
    private var mNetworkContentRequestID: String = ""

    /** 上次记录的追踪请求 ID，用于避免重复日志 */
    private var lastTraceNetworkRequestID = ""

    /**
     * 判断是否需要追踪此请求
     *
     * 子类可重写此方法自定义追踪条件
     *
     * @param call 请求调用
     * @return 是否追踪
     */
    open fun canTrace(call: Call): Boolean {
        return enableTrace
    }

    /**
     * 获取追踪请求 ID
     *
     * @return 追踪请求 ID
     */
    fun getNetworkTraceRequestID(): String {
        return mNetworkTraceRequestID
    }

    /**
     * 获取内容请求 ID
     *
     * @return 内容请求 ID
     */
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
        saveEvent(RequestTraceTimeRecord.EVENT_CONNECT_END)
        saveEvent(RequestTraceTimeRecord.EVENT_REQUEST_BODY_END)
        saveEvent(RequestTraceTimeRecord.EVENT_CALL_END)
        mRequestTraceTimeRecord?.errorMsg = ioe.toString()
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
        saveEvent(RequestTraceTimeRecord.EVENT_CALL_END)
        mRequestTraceTimeRecord?.errorMsg = ioe.toString()
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