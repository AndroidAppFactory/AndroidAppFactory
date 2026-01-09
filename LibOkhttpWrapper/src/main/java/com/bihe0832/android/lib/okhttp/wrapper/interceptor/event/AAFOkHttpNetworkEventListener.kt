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
 * 详细网络事件监听器
 *
 * 记录请求的详细阶段耗时，适用于问题定位：
 * - DNS 解析耗时
 * - 连接建立耗时
 * - TLS 握手耗时
 * - 请求头/请求体发送耗时
 * - 响应头/响应体接收耗时
 *
 * @param enableTrace 是否统计请求耗时
 * @param enableLog 是否打印日志
 * @param listener 自定义网络事件回调
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/6/27
 *
 * @since 1.0.0
 */
open class AAFOkHttpNetworkEventListener(
    enableTrace: Boolean,
    enableLog: Boolean,
    listener: EventListener?
) : AAFBasicOkHttpNetworkEventListener(enableTrace, enableLog, listener) {

    /** DNS 解析开始 */
    override fun dnsStart(call: Call, domainName: String) {
        super.dnsStart(call, domainName)
        saveEvent(RequestTraceTimeRecord.EVENT_DNS_START)
        listener?.dnsStart(call, domainName)
    }

    /** DNS 解析结束 */
    override fun dnsEnd(
        call: Call,
        domainName: String,
        inetAddressList: List<@JvmSuppressWildcards InetAddress>
    ) {
        super.dnsEnd(call, domainName, inetAddressList)
        saveEvent(RequestTraceTimeRecord.EVENT_DNS_END)
        listener?.dnsEnd(call, domainName, inetAddressList)
    }

    /** 连接开始 */
    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        super.connectStart(call, inetSocketAddress, proxy)
        saveEvent(RequestTraceTimeRecord.EVENT_CONNECT_START)
        listener?.connectStart(call, inetSocketAddress, proxy)
    }

    /** TLS 握手开始 */
    override fun secureConnectStart(call: Call) {
        super.secureConnectStart(call)
        saveEvent(RequestTraceTimeRecord.EVENT_SECURE_CONNECT_START)
        listener?.secureConnectStart(call)
    }

    /** TLS 握手结束 */
    override fun secureConnectEnd(call: Call, handshake: Handshake?) {
        super.secureConnectEnd(call, handshake)
        saveEvent(RequestTraceTimeRecord.EVENT_SECURE_CONNECT_END)
        listener?.secureConnectEnd(call, handshake)
    }

    /** 连接结束 */
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

    /** 请求头发送开始 */
    override fun requestHeadersStart(call: Call) {
        super.requestHeadersStart(call)
        saveEvent(RequestTraceTimeRecord.EVENT_REQUEST_HEADERS_START)
        listener?.requestHeadersStart(call)
    }

    /** 请求体发送开始 */
    override fun requestBodyStart(call: Call) {
        super.requestBodyStart(call)
        saveEvent(RequestTraceTimeRecord.EVENT_REQUEST_BODY_START)
        listener?.requestBodyStart(call)
    }

    /** 响应头接收开始 */
    override fun responseHeadersStart(call: Call) {
        super.responseHeadersStart(call)
        saveEvent(RequestTraceTimeRecord.EVENT_RESPONSE_HEADERS_START)
        listener?.responseHeadersStart(call)
    }

    /** 响应头接收结束 */
    override fun responseHeadersEnd(call: Call, response: Response) {
        super.responseHeadersEnd(call, response)
        saveEvent(RequestTraceTimeRecord.EVENT_RESPONSE_HEADERS_END)
        listener?.responseHeadersEnd(call, response)
    }

    /** 响应体接收开始 */
    override fun responseBodyStart(call: Call) {
        super.responseBodyStart(call)
        saveEvent(RequestTraceTimeRecord.EVENT_RESPONSE_BODY_START)
        listener?.responseBodyStart(call)
    }


}