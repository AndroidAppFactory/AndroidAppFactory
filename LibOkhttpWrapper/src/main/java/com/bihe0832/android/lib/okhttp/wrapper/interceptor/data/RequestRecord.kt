/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/28 下午3:13
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/28 下午3:09
 *
 */
package com.bihe0832.android.lib.okhttp.wrapper.interceptor.data

import com.bihe0832.android.lib.http.common.core.BaseConnection
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.AAFRequestDataRepository.getNetworkContentDataRecordByTraceID
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.AAFRequestDataRepository.getNetworkTraceTimeRecordByRequestID

/**
 * @desc: 一条网络请求记录
 */
class RequestRecord(val traceRequestId: String = "", val url: String = "", val method: String = "", val createTime: Long = 0L) {

    override fun toString(): String {

        var contentData = getRecordContentData()
        var traceTimeRecord = getRecordTraceTimeData()
        val totalCost = traceTimeRecord.getEventCostTime(RequestTraceTimeRecord.EVENT_CALL_START, RequestTraceTimeRecord.EVENT_CALL_END).let {
            if (it > 0) {
                it
            } else {
                traceTimeRecord.getEventCostTime(RequestTraceTimeRecord.EVENT_CALL_START, RequestTraceTimeRecord.EVENT_RESPONSE_BODY_END)
            }
        }
        StringBuffer().apply {
            append("\n \n")
            append("--> $method $url ${contentData.protocol}\n")
            append("${contentData.requestHeadersMap.toString()}\n")
            append("${contentData.requestBody}\n\n")
            if (method.equals(BaseConnection.HTTP_REQ_METHOD_POST, ignoreCase = true)) {
                append("--> END $method (${contentData.requestBodyLength} - byte body)   Cost: ${
                    traceTimeRecord.getEventCostTime(RequestTraceTimeRecord.EVENT_CALL_START, RequestTraceTimeRecord.EVENT_REQUEST_BODY_END)
                }ms\n")
            } else {
                append("--> END $method Cost: ${
                    traceTimeRecord.getEventCostTime(RequestTraceTimeRecord.EVENT_CALL_START, RequestTraceTimeRecord.EVENT_REQUEST_BODY_END)
                }ms)\n")
            }
            append("<-- ${contentData.status} $url ${contentData.errorMsg}\n")
            append("${contentData.responseHeadersMap.toString()}\n")
            append("${contentData.responseBody}\n\n")
            append("<-- END HTTP (${contentData.responseBodyLength} - byte body)   Total Cost: ${totalCost}ms\n")
        }.let {
            return it.toString()
        }
    }

    /**
     *  请求数据
     */
    fun getRecordContentData(): RequestContentDataRecord {
        return getNetworkContentDataRecordByTraceID(traceRequestId)
    }

    /**
     * 网络耗时
     */
    fun getRecordTraceTimeData(): RequestTraceTimeRecord {
        return getNetworkTraceTimeRecordByRequestID(traceRequestId)
    }

}