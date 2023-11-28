package com.bihe0832.android.framework.request

import com.bihe0832.android.lib.http.common.HTTPServer
import com.bihe0832.android.lib.http.common.HttpResponseHandler
import com.bihe0832.android.lib.http.common.core.BaseConnection
import java.nio.charset.Charset

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-08-01.
 * Description: Description
 */
object ZixieRequestHttp {

    fun get(url: String): String {
        return HTTPServer.getInstance().doRequestSync(url)
    }

    fun getOriginByteArray(url: String): ByteArray {
        return getOrigin(url).toByteArray(Charset.forName(BaseConnection.HTTP_REQ_VALUE_CHARSET_ISO_8599_1))
    }

    fun getOrigin(url: String): String {
        return HTTPServer.getInstance().doOriginRequestSync(url)
    }

    fun post(url: String, params: String): String {
        return HTTPServer.getInstance().doRequestSync(url, params)
    }

    fun postOrigin(url: String, params: String): String {
        return HTTPServer.getInstance().doOriginRequestSync(url, params)
    }

    fun get(url: String, responseHandler: HttpResponseHandler) {
        innerRequest(url, null, responseHandler, false)
    }

    fun getOrigin(url: String, responseHandler: HttpResponseHandler) {
        innerRequest(url, null, responseHandler, true)
    }

    fun post(url: String, postData: ByteArray, responseHandler: HttpResponseHandler) {
        innerRequest(url, postData, responseHandler, false)
    }

    fun postOrigin(url: String, postData: ByteArray, responseHandler: HttpResponseHandler) {
        innerRequest(url, postData, responseHandler, true)
    }

    private fun innerRequest(
        url: String,
        postData: ByteArray?,
        responseHandler: HttpResponseHandler,
        isOrigin: Boolean,
    ) {
        if (isOrigin) {
            HTTPServer.getInstance().doOriginRequestAsync(
                url,
                postData,
                BaseConnection.HTTP_REQ_VALUE_CONTENT_TYPE_URL_ENCODD,
                responseHandler,
            )
        } else {
            HTTPServer.getInstance().doRequestAsync(
                url,
                postData,
                BaseConnection.HTTP_REQ_VALUE_CONTENT_TYPE_URL_ENCODD,
                responseHandler,
            )
        }
    }
}
