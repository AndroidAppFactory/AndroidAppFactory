package com.bihe0832.android.common.network.okhttp.interceptor.data

import com.bihe0832.android.lib.http.common.core.BaseConnection
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

        StringBuffer().apply {
            append("\n \n")
            append("--> $method $url ${protocol}\n")
            append("${requestHeadersMap.toString()}\n")
            append("${requestBody}\n\n")
            if (method.equals(BaseConnection.HTTP_REQ_METHOD_POST, ignoreCase = true)) {
                append("--> END $method (${requestBodyLength} - byte body) \n")
            } else {
                append("--> END $method \n")
            }
            append("<-- ${status} $url")
            append("${errorMsg}\n")
            append("${responseHeadersMap.toString()}\n")
            append("${responseBody}\n\n")
            append("<-- END HTTP (${responseBodyLength} - byte body) \n")
        }.let {
            return it.toString()
        }
    }
}