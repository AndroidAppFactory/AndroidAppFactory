/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/21 下午4:37
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/21 下午4:37
 *
 */
package com.bihe0832.android.base.debug.request.okhttp

import com.bihe0832.android.app.api.AAFNetWorkApi
import com.bihe0832.android.base.debug.request.Constants
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * @author zixie code@bihe0832.com
 * Created on 2022/6/21.
 * Description: Description
 */
interface ApiService {

    @POST("/AndroidHTTP/post.php")
    fun getData(@Body body: RequestBody): Call<ResponseBody>

    @POST("/article/query/0/json")
    fun getNewData(@Body body: RequestBody): Call<ResponseBody>
}

fun debugOKHttp() {
    ThreadManager.getInstance().start {

        AAFNetWorkApi.getRetrofit(Constants.HTTP_DOMAIN).create(ApiService::class.java).getData(AAFNetWorkApi.getRequestBody()).apply {

                }.enqueue(ResultCall<ResponseBody>())
    }
}

private class ResultCall<T> : Callback<T> {
    override fun onResponse(p0: Call<T>, p1: Response<T>) {
        ZLog.d("NetworkResult", (p1.body() as ResponseBody).source().toString())
    }

    override fun onFailure(p0: Call<T>, p1: Throwable) {
        ZLog.d("NetworkResult", p1.toString())
        ZLog.d("NetworkResult", p0.request().toString())

    }
}