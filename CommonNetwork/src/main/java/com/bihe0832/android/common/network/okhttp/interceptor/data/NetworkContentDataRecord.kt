package com.bihe0832.android.common.network.okhttp.interceptor.data

import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.Protocol
import java.io.Serializable

/**
 * 请求数据记录
 */
class NetworkContentDataRecord : Serializable {

    var contentRequestId: String = ""
    var mTraceRequestId: String = ""

    var url: String = ""
    var method: String = ""

    var protocol: Protocol = Protocol.HTTP_1_1
    var requestBodyLength: String = ""
    var requestContentType: MediaType? = null
    var requestHeadersMap: Headers? = null
    var requestBody: String = ""

    var status = -1
    var errorMsg: String = ""

    var responseBodyLength: String = ""
    var responseContentType: MediaType? = null
    var responseHeadersMap: Headers? = null
    var responseBody: String = ""

    override fun toString(): String {
        return "NetworkFeedBean{" +
                "mRequestId='" + contentRequestId + '\'' +
                ", mUrl='" + url + '\'' +
                ", mMethod='" + method + '\'' +
                ", mRequestHeadersMap=" + requestHeadersMap +
                ", mStatus=" + status +
                ", mResponseHeadersMap=" + responseHeadersMap +
                '}'
    }
}