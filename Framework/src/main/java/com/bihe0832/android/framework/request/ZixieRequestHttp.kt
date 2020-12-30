package com.bihe0832.android.framework.request


import com.bihe0832.android.lib.http.common.HTTPServer
import com.bihe0832.android.lib.http.common.HttpBasicRequest
import com.bihe0832.android.lib.http.common.HttpResponseHandler

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-08-01.
 * Description: Description
 */
object ZixieRequestHttp {

    fun get(url: String): String {
        return HTTPServer.getInstance().doRequestSync(url)
    }

    fun post(url: String, params: String): String {
        return HTTPServer.getInstance().doRequestSync(url, params)
    }

    fun get(url: String, responseHandler: HttpResponseHandler) {
        object : HttpBasicRequest() {
            override fun getUrl(): String {
                return url
            }

            override fun getResponseHandler(): HttpResponseHandler {
                return responseHandler
            }
        }.let {
            HTTPServer.getInstance().doRequestAsync(it)
        }
    }

    fun post(url: String, postData: ByteArray, responseHandler: HttpResponseHandler) {
        object : HttpBasicRequest() {
            override fun getUrl(): String {
                return url
            }

            override fun getResponseHandler(): HttpResponseHandler {
                return responseHandler
            }
        }.apply {
            data = postData
        }.let {
            HTTPServer.getInstance().doRequestAsync(it)
        }
    }
}
