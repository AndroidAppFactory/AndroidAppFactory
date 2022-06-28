/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/28 下午3:13
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/28 下午3:09
 *
 */
package com.bihe0832.android.common.network.okhttp.interceptor.data

import com.bihe0832.android.common.network.okhttp.interceptor.data.AAFRequestDataRepository.getNetworkContentDataRecordByTraceID
import com.bihe0832.android.common.network.okhttp.interceptor.data.AAFRequestDataRepository.getNetworkTraceTimeRecordByRequestID
import com.bihe0832.android.lib.http.common.core.BaseConnection

/**
 * @desc: 一条网络请求记录
 */
class NetworkRecord(
        val traceRequestId: String = "",
        val url: String = "",
        val method: String = "",
        val createTime: Long = 0L) {

    override fun toString(): String {

        var contentData = getRecordContentData()
        var traceTimeRecord = getRecordTraceTimeData()
        StringBuffer().apply {
            append("\n \n")
            append("--> $method $url ${contentData.protocol}\n")
            append("${contentData.requestHeadersMap.toString()}\n")
            append("${contentData.requestBody}\n")
            if (method.equals(BaseConnection.HTTP_REQ_METHOD_POST, ignoreCase = true)) {
                append("--> END $method (${contentData.requestBodyLength} - byte body)   Cost: ${traceTimeRecord.getEventCostTime(NetworkTraceTimeRecord.EVENT_CALL_START, NetworkTraceTimeRecord.EVENT_REQUEST_BODY_END)}ms\n")
            } else {
                append("--> END $method Cost: ${traceTimeRecord.getEventCostTime(NetworkTraceTimeRecord.EVENT_CALL_START, NetworkTraceTimeRecord.EVENT_REQUEST_BODY_END)}ms)\n")
            }
            append("<-- ${contentData.status} $url")
            append("${contentData.errorMsg}\n")
            append("${contentData.responseHeadersMap.toString()}\n")
            append("${contentData.responseBody}\n\n")
            append("<-- END HTTP (${contentData.responseBodyLength} - byte body)   Total Cost: ${traceTimeRecord.getEventCostTime(NetworkTraceTimeRecord.EVENT_CALL_START, NetworkTraceTimeRecord.EVENT_CALL_END)}ms\n")
        }.let {
            return it.toString()
        }
    }

    /**
     *  请求数据
     */
    fun getRecordContentData(): NetworkContentDataRecord {
        return getNetworkContentDataRecordByTraceID(traceRequestId)
    }

    /**
     * 网络耗时
     */
    fun getRecordTraceTimeData(): NetworkTraceTimeRecord {
        return getNetworkTraceTimeRecordByRequestID(traceRequestId)
    }

}