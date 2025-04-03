package com.bihe0832.android.app.api

import android.text.TextUtils
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.constant.Constants
import com.bihe0832.android.lib.okhttp.wrapper.OkHttpWrapper
import com.bihe0832.android.lib.okhttp.wrapper.convert.GsonConverterFactory
import com.bihe0832.android.lib.okhttp.wrapper.ext.getRequestBodyByJsonString
import com.bihe0832.android.lib.request.URLUtils
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Retrofit


/**
 * Created by zixie on 2018/2/5.
 */

object AAFNetWorkApi {

    private val mHttpClient: OkHttpClient by lazy {
        OkHttpWrapper.getOkHttpClientBuilderWithBasicInterceptor(ZixieContext.applicationContext!!,!ZixieContext.isOfficial()).build()
    }

    const val LOG_TAG = "RetrofitLog"

    //获取个人信息等对应的后台接口地址
    const val REQUEST_PARAM_APP_VERSION = "version"
    const val REQUEST_PARAM_OS = "os"
    const val REQUEST_PARAM_DEVKEY = "devid"
    const val REQUEST_PARAM_PACKAGE_NAME = "package"

    fun getCommonURL(url: String?, param: String): String {
        if (TextUtils.isEmpty(url)) {
            return ""
        }
        val publicPara = StringBuffer()
        publicPara.append(REQUEST_PARAM_DEVKEY).append(URLUtils.HTTP_REQ_ENTITY_MERGE).append(ZixieContext.deviceId)
        publicPara.append(URLUtils.HTTP_REQ_ENTITY_JOIN).append(REQUEST_PARAM_OS).append(URLUtils.HTTP_REQ_ENTITY_MERGE).append(Constants.SYSTEM_CONSTANT)
        publicPara.append(URLUtils.HTTP_REQ_ENTITY_JOIN).append(REQUEST_PARAM_APP_VERSION).append(URLUtils.HTTP_REQ_ENTITY_MERGE).append(ZixieContext.getVersionCode())
        publicPara.append(URLUtils.HTTP_REQ_ENTITY_JOIN).append(REQUEST_PARAM_PACKAGE_NAME).append(URLUtils.HTTP_REQ_ENTITY_MERGE).append(ZixieContext.applicationContext?.packageName)
        return URLUtils.marge(URLUtils.marge("$url", publicPara.toString()), param)
    }

    fun getRetrofit(url: String): Retrofit {
        return Retrofit.Builder().client(mHttpClient).addConverterFactory(GsonConverterFactory()).baseUrl(url).build()
    }

    fun getRetrofitWithoutJsonParse(url: String): Retrofit {
        return Retrofit.Builder().client(mHttpClient).baseUrl(url).build()
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

    fun getRequestBody(): RequestBody {
        return getRequestBodyByJsonString(getRequestPara().toString())
    }
}
