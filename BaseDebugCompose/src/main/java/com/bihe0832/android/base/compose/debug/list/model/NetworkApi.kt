package com.bihe0832.android.base.compose.debug.list.model

import com.bihe0832.android.app.api.AAFNetWorkApi
import com.bihe0832.android.app.api.AAFNetworkCallback
import com.bihe0832.android.common.coroutines.AAFCoroutinesData
import com.bihe0832.android.lib.log.ZLog
import retrofit2.Call
import retrofit2.http.GET
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/21.
 * Description: Description
 *
 */
internal object NetworkApi {
    interface ServerApi {
        @GET("/news")
        fun getNewsList(): Call<TopicResponse>
    }

    private var index = 1
    fun getData(callbakc: AAFNetworkCallback<TopicResponse>) {
        AAFNetWorkApi.getRetrofit("https://api.readhub.cn").create(ServerApi::class.java)
            .getNewsList().enqueue(object : AAFNetworkCallback<TopicResponse>() {
                override fun onSuccess(result: TopicResponse) {
                    ZLog.d("NetworkResult", result.toString())
                    if (index % 3 == 0){
                        callbakc.onError(-1,"test")
                    }else{
                        callbakc.onSuccess(result)
                    }
                    index++
                }

                override fun onError(errorCode: Int, msg: String) {
                    callbakc.onError(errorCode, msg)
                }

                override fun onLoginError(errorCode: Int, msg: String) {
                    callbakc.onLoginError(errorCode, msg)
                }
            })
    }

    suspend fun getData(): AAFCoroutinesData<TopicResponse> = suspendCoroutine { cont ->
        getData(object : AAFNetworkCallback<TopicResponse>() {
            override fun onSuccess(result: TopicResponse) {
                cont.resume(AAFCoroutinesData(result))
            }

            override fun onError(code: Int, msg: String) {
                cont.resume(
                    AAFCoroutinesData(code, code, msg)
                )
            }

        })
    }

}
