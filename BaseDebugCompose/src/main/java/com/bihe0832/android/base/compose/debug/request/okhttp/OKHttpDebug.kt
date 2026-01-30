/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/21 下午4:37
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/21 下午4:37
 *
 */
package com.bihe0832.android.base.compose.debug.request.okhttp

import com.bihe0832.android.app.api.AAFNetWorkApi
import com.bihe0832.android.app.api.call.AAFNetworkCallback
import com.bihe0832.android.app.api.call.BaseResponse
import com.bihe0832.android.base.compose.debug.request.Constants
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.okhttp.wrapper.OkHttpWrapper
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.thread.ThreadManager
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * @author zixie code@bihe0832.com
 * Created on 2022/6/21.
 * Description: Description
 */
interface ApiService {

    @POST("/AndroidHTTP/post.php")
    fun getData(@Body body: RequestBody): Call<ResponseBody>

    @POST("/AndroidHTTP/post.php")
    fun getData1(
        @Body body: RequestBody,
//        @Header(OkHttpWrapper.HTTP_REQ_PROPERTY_AAF_CONTENT_REQUEST_DELAY) delay: Int = 300,
//        @Header(OkHttpWrapper.HTTP_REQ_PROPERTY_AAF_CONTENT_REQUEST_DATA) result: String = URLUtils.encode(
//            "{\"code\":100,\"message\":\"sdfsfsf\"}"
//        )
    ): Call<BaseResponse>

    @POST("/AndroidHTTP/post11.php")
    fun getData2(
        @Body body: RequestBody,
//        @Header(OkHttpWrapper.HTTP_REQ_PROPERTY_AAF_CONTENT_REQUEST_DELAY) delay: Int = 300,
//        @Header(OkHttpWrapper.HTTP_REQ_PROPERTY_AAF_CONTENT_REQUEST_DATA) result: String = URLUtils.encode(
//            "{\"code\":100,\"message\":\"sdfsfsf\"}"
//        )
    ): Call<BaseResponse>

    @POST("/article/query/0/json")
    fun getNewData(@Body body: RequestBody): Call<ResponseBody>
}

private val mApiServiceForConfig: ApiService =
    AAFNetWorkApi.getRetrofit(Constants.HTTP_DOMAIN).create(ApiService::class.java)

fun debugOKHttp1() {
    ThreadManager.getInstance().start {

        mApiServiceForConfig.getData1(AAFNetWorkApi.getRequestBody()).apply {

        }.enqueue(object : AAFNetworkCallback<BaseResponse>() {
            override fun onSuccess(result: BaseResponse) {
                ZLog.d("NetworkResult", result.toString())
            }
        })
    }
}

fun debugOKHttp2() {
    ThreadManager.getInstance().start {

        mApiServiceForConfig.getData2(AAFNetWorkApi.getRequestBody()).apply {

        }.enqueue(object : AAFNetworkCallback<BaseResponse>() {
            override fun onSuccess(result: BaseResponse) {
                ZLog.d("NetworkResult", result.toString())
            }
        })
    }
}

private class ResultCall<T> : Callback<T> {
    override fun onResponse(p0: Call<T>, p1: Response<T>) {
        ZLog.d("NetworkResult", (p1.body() as? ResponseBody)?.source().toString())
    }

    override fun onFailure(p0: Call<T>, p1: Throwable) {
        ZLog.d("NetworkResult", p1.toString())
        ZLog.d("NetworkResult", p0.request().toString())

    }
}

