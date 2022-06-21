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
import com.bihe0832.android.base.debug.request.advanced.TestResponse
import com.bihe0832.android.lib.log.ZLog
import okhttp3.RequestBody
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
    fun getData(@Body body: RequestBody): Call<TestResponse>
}

fun debugOKHttp() {
    AAFNetWorkApi.getRetrofit(Constants.HTTP_DOMAIN)
            .create(ApiService::class.java).getData(AAFNetWorkApi.getRequestBody()).enqueue(object : Callback<TestResponse> {
                override fun onResponse(p0: Call<TestResponse>, p1: Response<TestResponse>) {
                    ZLog.d(p1.body().toString())
                }

                override fun onFailure(p0: Call<TestResponse>, p1: Throwable) {
                    ZLog.d(p1.toString())
                }

            })
}