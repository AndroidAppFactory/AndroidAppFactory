/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/27 下午4:46
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/27 下午4:35
 *
 */

package com.bihe0832.android.lib.okhttp.wrapper.interceptor.event

import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.RequestTraceTimeRecord
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.Handshake
import okhttp3.Protocol
import okhttp3.Response
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy


/**
 * 记录请求的详细阶段的耗时，问题定位推荐
 */
open class AAFOkHttpNetworkEventListener(
    enableTrace: Boolean,
    enableLog: Boolean,
    listener: EventListener?
) : AAFBasicOkHttpNetworkEventListener(enableTrace, enableLog, listener) {


    override fun dnsStart(call: Call, domainName: String) {
        super.dnsStart(call, domainName)
        saveEvent(RequestTraceTimeRecord.EVENT_DNS_START)
        listener?.dnsStart(call, domainName)
    }

    override fun dnsEnd(
        call: Call,
        domainName: String,
        inetAddressList: List<@JvmSuppressWildcards InetAddress>
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
        proxy: Proxy,
        protocol: Protocol?
    ) {
        super.connectEnd(call, inetSocketAddress, proxy, protocol)
        saveEvent(RequestTraceTimeRecord.EVENT_CONNECT_END)
        listener?.connectEnd(call, inetSocketAddress, proxy, protocol)
    }

    override fun requestHeadersStart(call: Call) {
        super.requestHeadersStart(call)
        saveEvent(RequestTraceTimeRecord.EVENT_REQUEST_HEADERS_START)
        listener?.requestHeadersStart(call)
    }


    override fun requestBodyStart(call: Call) {
        super.requestBodyStart(call)
        saveEvent(RequestTraceTimeRecord.EVENT_REQUEST_BODY_START)
        listener?.requestBodyStart(call)
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


}