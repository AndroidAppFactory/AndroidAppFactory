package com.bihe0832.android.framework.request


import com.bihe0832.android.lib.http.common.HTTPServer
import com.bihe0832.android.lib.http.common.HttpResponseHandler
import com.bihe0832.android.lib.http.common.core.HttpBasicRequest

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-08-01.
 * Description: Description
 */
object ZixieRequestHttp {

    fun get(url: String): String {
        return HTTPServer.getInstance().doRequestSync(url)
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
        isOrigin: Boolean
    ) {
        object : HttpBasicRequest() {
            override fun getUrl(): String {
                return url
            }

            override fun getResponseHandler(): HttpResponseHandler {
                return responseHandler
            }
        }.apply {
            postData?.let {
                data = postData
            }
        }.let {
            if (isOrigin) {
                HTTPServer.getInstance().doOriginRequestAsync(it)
            } else {
                HTTPServer.getInstance().doRequestAsync(it)
            }
        }
    }
}
