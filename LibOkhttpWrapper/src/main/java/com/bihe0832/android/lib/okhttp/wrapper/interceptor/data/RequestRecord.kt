/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/28 下午3:13
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/28 下午3:09
 *
 */
package com.bihe0832.android.lib.okhttp.wrapper.interceptor.data

import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.AAFRequestDataRepository.getNetworkContentDataRecordByTraceID
import com.bihe0832.android.lib.okhttp.wrapper.interceptor.data.AAFRequestDataRepository.getNetworkTraceTimeRecordByRequestID

/**
 * 网络请求记录
 *
 * 包含请求的基本信息和关联的内容数据、耗时数据：
 * - 请求 ID、URL、方法、创建时间
 * - 关联 RequestContentDataRecord（请求内容）
 * - 关联 RequestTraceTimeRecord（耗时追踪）
 *
 * @param traceRequestId 追踪请求 ID
 * @param url 请求 URL
 * @param method 请求方法
 * @param createTime 创建时间（SystemClock.elapsedRealtime）
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/6/28
 *
 * @since 1.0.0
 */
class RequestRecord(
    val traceRequestId: String = "",
    val url: String = "",
    val method: String = "",
    val createTime: Long = 0L
) {

    /**
     * 转换为可读的日志字符串
     *
     * 包含完整的请求/响应信息和各阶段耗时
     *
     * @return 格式化的请求记录
     */
    override fun toString(): String {
        val contentData = getRecordContentData()
        val traceTimeRecord = getRecordTraceTimeData()

        val requestCost = traceTimeRecord.getEventCostTime(
            RequestTraceTimeRecord.EVENT_CALL_START,
            RequestTraceTimeRecord.EVENT_REQUEST_BODY_END
        )

        val totalCost = traceTimeRecord.getEventCostTime(
            RequestTraceTimeRecord.EVENT_CALL_START,
            RequestTraceTimeRecord.EVENT_CALL_END
        ).let {
            if (it > 0) it
            else traceTimeRecord.getEventCostTime(
                RequestTraceTimeRecord.EVENT_CALL_START,
                RequestTraceTimeRecord.EVENT_RESPONSE_BODY_END
            )
        }

        // 检测是否为 Mock 请求（没有经过网络拦截器，contentData 内容为空）
        val isMockRequest =
            contentData.url.isEmpty() && contentData.requestHeadersMap == null && traceTimeRecord.errorMsg.isNullOrEmpty()

        if (contentData.url.isEmpty()) {
            contentData.url = url
            contentData.method = method
        }
        if (isMockRequest) {
            contentData.method = "AAFMock"
        }
        if (traceTimeRecord.errorMsg.isNotEmpty()) {
            contentData.errorMsg += "\n\n${traceTimeRecord.errorMsg}\n"
        }

        return contentData.toLogString(
            traceRequestId = traceRequestId,
            requestCostMs = requestCost,
            totalCostMs = totalCost,
            isMockRequest = isMockRequest
        )
    }

    /**
     * 获取请求内容数据
     *
     * @return 请求内容数据记录
     */
    fun getRecordContentData(): RequestContentDataRecord {
        return getNetworkContentDataRecordByTraceID(traceRequestId)
    }

    /**
     * 获取请求耗时数据
     *
     * @return 耗时追踪记录
     */
    fun getRecordTraceTimeData(): RequestTraceTimeRecord {
        return getNetworkTraceTimeRecordByRequestID(traceRequestId)
    }

}