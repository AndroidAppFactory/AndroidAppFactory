package com.bihe0832.android.lib.okhttp.wrapper.interceptor.data

import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.okhttp.wrapper.OkHttpWrapper
import java.util.concurrent.ConcurrentHashMap


object AAFRequestDataRepository {

    // 请求时间追踪记录
    private val mRequestTraceTimeRecord: ConcurrentHashMap<String, RequestTraceTimeRecord> =
        ConcurrentHashMap()

    // 网络请求实际结果
    private val mRequestContentDataRecord: ConcurrentHashMap<String, RequestContentDataRecord> =
        ConcurrentHashMap()

    fun removeData(traceID: String) {
        getNetworkContentDataRecordByTraceID(traceID).contentRequestId.let {
            mRequestContentDataRecord.remove(it)
        }
        mRequestTraceTimeRecord.remove(traceID)
        ZLog.d(
            OkHttpWrapper.TAG,
            "Content: " + mRequestContentDataRecord.size + ",Time: " + mRequestTraceTimeRecord.size
        )
    }

    fun getNetworkContentDataRecordByContentID(requestId: String): RequestContentDataRecord {
        var networkFeedBean = mRequestContentDataRecord.get(requestId)
        //如果取到的数据为null
        if (networkFeedBean == null) {
            //则存储该数据到map集合中
            networkFeedBean = RequestContentDataRecord().apply {
                this.contentRequestId = requestId
            }
            mRequestContentDataRecord.put(requestId, networkFeedBean)
        }
        ZLog.d(
            OkHttpWrapper.TAG,
            "Content: " + mRequestContentDataRecord.size + ",Time: " + mRequestTraceTimeRecord.size
        )
        return networkFeedBean
    }

    fun getNetworkContentDataRecordByTraceID(requestId: String): RequestContentDataRecord {
        return getNetworkContentDataRecordByContentID(
            getNetworkTraceTimeRecordByRequestID(requestId).contentRequestId
                ?: ""
        )
    }

    fun getNetworkTraceTimeRecordByRequestID(requestId: String): RequestTraceTimeRecord {
        var networkFeedBean = mRequestTraceTimeRecord.get(requestId)
        if (networkFeedBean == null) {
            networkFeedBean = RequestTraceTimeRecord()
                .apply {
                    this.setTraceRequestId(requestId)
                }
            mRequestTraceTimeRecord[requestId] = networkFeedBean
        }
        ZLog.d(
            OkHttpWrapper.TAG,
            "Content: " + mRequestContentDataRecord.size + ",Time: " + mRequestTraceTimeRecord.size
        )
        return networkFeedBean
    }
}