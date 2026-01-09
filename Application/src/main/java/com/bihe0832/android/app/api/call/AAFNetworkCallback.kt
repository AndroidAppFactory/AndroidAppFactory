package com.bihe0832.android.app.api.call

import com.bihe0832.android.app.api.AAFNetWorkApi
import com.bihe0832.android.app.log.AAFLoggerFile
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.log.ZLog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * AAF 网络请求回调基类
 *
 * 封装 Retrofit 网络请求的通用回调处理逻辑，包括：
 * - 成功响应处理
 * - 错误码统一处理
 * - 异常捕获和日志记录
 *
 * 所有网络请求都应使用此回调类来处理响应
 *
 * @param T 响应数据类型，必须继承自 BaseResponse
 * @author zixie code@bihe0832.com
 * Created on 2020-02-24.
 */
abstract class AAFNetworkCallback<T : BaseResponse> : Callback<T> {

    /**
     * 请求成功回调
     *
     * @param result 成功的响应数据
     */
    abstract fun onSuccess(result: T)

    /**
     * 请求错误回调
     *
     * 默认显示调试提示，子类可重写自定义错误处理
     *
     * @param errorCode 错误码
     * @param msg 错误信息
     */
    open fun onError(errorCode: Int, msg: String) {
        ZixieContext.showDebug("请求异常，请稍后重试($errorCode, $msg)")
    }

    /**
     * 登录错误回调
     *
     * 用于处理需要登录的接口返回的登录错误
     *
     * @param errorCode 错误码
     * @param msg 错误信息
     */
    open fun onLoginError(errorCode: Int, msg: String) {
    }

    /**
     * 内部错误处理
     *
     * 记录错误日志并根据错误码分发到对应的错误处理方法
     */
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

    /**
     * Retrofit 响应回调
     *
     * 处理 HTTP 响应，解析业务数据并分发到成功或错误回调
     */
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
            onInnerError(call, FLAG_FAILED_HTTP_EXCEPTION, "${call.request().url} response Exception:$response + ")
        }
    }

    /**
     * Retrofit 请求失败回调
     *
     * 处理网络请求失败的情况（如网络不可用、超时等）
     */
    override fun onFailure(call: Call<T>, t: Throwable) {
        t.printStackTrace()
        onInnerError(
            call,
            FLAG_FAILED_HTTP,
            " call: ${call.hashCode()}, ${call.request().url} onFailure :${t.message} + "
        )
    }
}
