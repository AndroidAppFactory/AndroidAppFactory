package com.bihe0832.android.app.api

import android.content.Context
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.constant.Constants
import com.bihe0832.android.lib.gson.JsonHelper
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.URLUtils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * Created by hardyshi on 2018/2/5.
 */
const val TIME_OUT_READ = 5000L
const val TIME_OUT_CONNECTION = 5000L
const val TIME_OUT_WRITE = 5000L


object AAFNetWorkApi {

    private var mHttpClient: OkHttpClient? = null
    const val LOG_TAG = "RetrofitLog"

    //获取个人信息等对应的后台接口地址
    const val REQUEST_PARAM_APP_VERSION = "version"
    const val REQUEST_PARAM_OS = "os"
    const val REQUEST_PARAM_DEVKEY = "devid"
    const val REQUEST_PARAM_PACKAGE_NAME = "package"

    fun init(context: Context, debug: Boolean = false) {
        mHttpClient =
                OkHttpClient.Builder()
                        .connectTimeout(TIME_OUT_CONNECTION, TimeUnit.MILLISECONDS)
                        .readTimeout(TIME_OUT_READ, TimeUnit.MILLISECONDS)
                        .writeTimeout(TIME_OUT_WRITE, TimeUnit.MILLISECONDS)
                        .addNetworkInterceptor(
                                HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
                                    //打印retrofit日志
                                    ZLog.d(LOG_TAG, "retrofitMsg = $message")
                                }).apply {
                                    level = if (debug) {
                                        HttpLoggingInterceptor.Level.BODY
                                    } else {
                                        HttpLoggingInterceptor.Level.NONE
                                    }
                                }
                        )
                        .build()

    }

    fun getCommonURL(url: String, param: String): String {
        val publicPara = StringBuffer()
        publicPara.append(REQUEST_PARAM_DEVKEY).append(URLUtils.HTTP_REQ_ENTITY_MERGE).append(ZixieContext.deviceId)
        publicPara.append(URLUtils.HTTP_REQ_ENTITY_JOIN).append(REQUEST_PARAM_OS).append(URLUtils.HTTP_REQ_ENTITY_MERGE).append(Constants.SYSTEM_CONSTANT)
        publicPara.append(URLUtils.HTTP_REQ_ENTITY_JOIN).append(REQUEST_PARAM_APP_VERSION).append(URLUtils.HTTP_REQ_ENTITY_MERGE).append(ZixieContext.getVersionCode())
        publicPara.append(URLUtils.HTTP_REQ_ENTITY_JOIN).append(REQUEST_PARAM_PACKAGE_NAME).append(URLUtils.HTTP_REQ_ENTITY_MERGE).append(ZixieContext.applicationContext?.packageName)
        return URLUtils.marge(URLUtils.marge("$url", publicPara.toString()), param)
    }

    fun getRetrofit(url: String): Retrofit {
        return Retrofit.Builder()
                .client(mHttpClient)
                .addConverterFactory(GsonConverterFactory.create(JsonHelper.getGson()))
                .baseUrl(url)
                .build()
    }

    fun getRetrofitWithoutJsonParse(url: String): Retrofit {
        return Retrofit.Builder()
                .client(mHttpClient)
                .baseUrl(url)
                .build()
    }

    fun getRequestPara(): JSONObject {
        val result = JSONObject()
        try {
            result.put(REQUEST_PARAM_DEVKEY, ZixieContext.deviceId)
            result.put(REQUEST_PARAM_APP_VERSION, ZixieContext.getVersionCode().toString())
            result.put(REQUEST_PARAM_OS, Constants.SYSTEM_CONSTANT)
            result.put(REQUEST_PARAM_PACKAGE_NAME, ZixieContext.applicationContext?.packageName)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }

}
