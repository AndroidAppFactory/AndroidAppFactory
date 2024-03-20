package com.bihe0832.android.app.api

import com.bihe0832.android.app.log.AAFLoggerFile
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.log.ZLog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Symmary
 * @author hardyshi code@bihe0832.com
 * Created on 2020-02-24.
 * Description: 所有的网络请求都使用MnaNetworkCallback来处理
 */
abstract class AAFNetworkCallback<T : BaseResponse> : Callback<T> {

    abstract fun onSuccess(result: T)

    open fun onError(errorCode: Int, msg: String) {
        ZixieContext.showDebug("请求异常，请稍后重试($errorCode, $msg)")
    }

    open fun onLoginError(errorCode: Int, msg: String) {
    }

    private fun onInnerError(call: Call<T>, errorCode: Int, msg: String) {
        AAFLoggerFile.logServer("------------------------------------")
        AAFLoggerFile.logServer("${AAFNetWorkApi.LOG_TAG}  call: ${call.hashCode()}, onError: $errorCode, $msg")
        AAFLoggerFile.logServer("------------------------------------\n")
        when (errorCode) {
            else -> {
                onError(errorCode, msg)
            }
        }
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        ZLog.d(AAFNetWorkApi.LOG_TAG, "call: ${call.hashCode()},  onResponse:$response")
        try {
            if (response.isSuccessful) {
                if (null != response.body()) {
                    response.body()!!.let { networkResponse ->
                        ZLog.d(AAFNetWorkApi.LOG_TAG, "networkResponse ->: $networkResponse")
                        if (networkResponse.errCode == FLAG_SUCCESS) {
                            onSuccess(networkResponse)
                        } else {
                            onInnerError(call, networkResponse.errCode, networkResponse.message)
                        }
                    }
                } else {
                    onInnerError(call, FLAG_FAILED_BODY_EMPTY, "onResponse is empty")
                }
            } else {
                onInnerError(call, FLAG_FAILED_HTTP, "response HTTP Failed:$response + ")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onInnerError(call, FLAG_FAILED_HTTP_EXCEPTION, "${call.request().url()} response Exception:$response + ")
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        t.printStackTrace()
        onInnerError(
            call,
            FLAG_FAILED_HTTP,
            " call: ${call.hashCode()}, ${call.request().url()} onFailure :${t.message} + "
        )
    }
}
