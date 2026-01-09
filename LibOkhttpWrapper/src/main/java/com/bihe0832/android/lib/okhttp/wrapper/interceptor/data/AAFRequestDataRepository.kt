/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/28
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/28
 *
 */

package com.bihe0832.android.lib.okhttp.wrapper.interceptor.data

import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.okhttp.wrapper.OkHttpWrapper
import java.util.concurrent.ConcurrentHashMap

/**
 * AAF 请求数据仓库
 *
 * 统一管理请求的耗时记录和内容数据：
 * - 存储请求耗时追踪记录（RequestTraceTimeRecord）
 * - 存储请求内容数据记录（RequestContentDataRecord）
 * - 提供根据 ID 获取和移除记录的方法
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/6/28
 *
 * @since 1.0.0
 */
object AAFRequestDataRepository {

    /** 请求时间追踪记录，key 为 traceRequestId */
    private val mRequestTraceTimeRecord: ConcurrentHashMap<String, RequestTraceTimeRecord> =
        ConcurrentHashMap()

    /** 网络请求实际结果，key 为 contentRequestId */
    private val mRequestContentDataRecord: ConcurrentHashMap<String, RequestContentDataRecord> =
        ConcurrentHashMap()

    /**
     * 移除指定请求的所有数据
     *
     * 同时移除耗时记录和内容数据记录
     *
     * @param traceID 追踪请求 ID
     */
    fun removeData(traceID: String) {
        getNetworkContentDataRecordByTraceID(traceID).contentRequestId.let {
            mRequestContentDataRecord.remove(it)
        }
        mRequestTraceTimeRecord.remove(traceID)
        ZLog.d(
            OkHttpWrapper.TAG,
            "removeData:" + traceID + "，Content: " + mRequestContentDataRecord.size + ",Time: " + mRequestTraceTimeRecord.size
        )
    }

    /**
     * 根据内容请求 ID 获取请求内容数据记录
     *
     * 如果记录不存在，会创建新记录
     *
     * @param requestId 内容请求 ID
     * @return 请求内容数据记录
     */
    fun getNetworkContentDataRecordByContentID(requestId: String): RequestContentDataRecord {
        var networkFeedBean = mRequestContentDataRecord.get(requestId)
        if (networkFeedBean == null) {
            networkFeedBean = RequestContentDataRecord().apply {
                this.contentRequestId = requestId
            }
            mRequestContentDataRecord.put(requestId, networkFeedBean)
        }
        ZLog.d(
            OkHttpWrapper.TAG,
            "GetNetworkContentData:" + requestId + "，Content: " + mRequestContentDataRecord.size + ",Time: " + mRequestTraceTimeRecord.size
        )
        return networkFeedBean
    }

    /**
     * 根据追踪请求 ID 获取请求内容数据记录
     *
     * @param requestId 追踪请求 ID
     * @return 请求内容数据记录
     */
    fun getNetworkContentDataRecordByTraceID(requestId: String): RequestContentDataRecord {
        return getNetworkContentDataRecordByContentID(
            getNetworkTraceTimeRecordByRequestID(requestId).contentRequestId ?: ""
        )
    }

    /**
     * 根据请求 ID 获取耗时追踪记录
     *
     * 如果记录不存在，会创建新记录
     *
     * @param requestId 追踪请求 ID
     * @return 耗时追踪记录
     */
    fun getNetworkTraceTimeRecordByRequestID(requestId: String): RequestTraceTimeRecord {
        var networkFeedBean = mRequestTraceTimeRecord.get(requestId)
        if (networkFeedBean == null) {
            networkFeedBean = RequestTraceTimeRecord().apply {
                this.setTraceRequestId(requestId)
            }
            mRequestTraceTimeRecord[requestId] = networkFeedBean
        }
        ZLog.d(
            OkHttpWrapper.TAG,
            "GetNetworkTimeRecord:" + requestId + "，Content: " + mRequestContentDataRecord.size + ",Time: " + mRequestTraceTimeRecord.size
        )
        return networkFeedBean
    }
}