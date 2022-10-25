package com.bihe0832.android.lib.okhttp.wrapper.interceptor.data

import java.util.concurrent.ConcurrentHashMap


object AAFRequestDataRepository {

    // 请求时间追踪记录
    private val mNetworkTraceTimeRecord: ConcurrentHashMap<String, NetworkTraceTimeRecord> = ConcurrentHashMap()

    // 网络请求实际结果
    private val mNetworkContentDataRecord: ConcurrentHashMap<String, NetworkContentDataRecord> = ConcurrentHashMap()

    fun removeData(traceID: String) {
        getNetworkContentDataRecordByTraceID(traceID).contentRequestId.let {
            mNetworkContentDataRecord.remove(it)
        }
        mNetworkTraceTimeRecord.remove(traceID)
    }

    fun getNetworkContentDataRecordByContentID(requestId: String): NetworkContentDataRecord {
        var networkFeedBean = mNetworkContentDataRecord.get(requestId)
        //如果取到的数据为null
        if (networkFeedBean == null) {
            //则存储该数据到map集合中
            networkFeedBean = NetworkContentDataRecord().apply {
                this.contentRequestId = requestId
            }
            mNetworkContentDataRecord.put(requestId, networkFeedBean)
        }
        return networkFeedBean
    }

    fun getNetworkContentDataRecordByTraceID(requestId: String): NetworkContentDataRecord {
        return getNetworkContentDataRecordByContentID(getNetworkTraceTimeRecordByRequestID(requestId).contentRequestId
                ?: "")
    }

    fun getNetworkTraceTimeRecordByRequestID(requestId: String): NetworkTraceTimeRecord {
        var networkFeedBean = mNetworkTraceTimeRecord.get(requestId)
        if (networkFeedBean == null) {
            networkFeedBean = NetworkTraceTimeRecord().apply {
                this.setTraceRequestId(requestId)
            }
            mNetworkTraceTimeRecord[requestId] = networkFeedBean
        }
        return networkFeedBean
    }
}