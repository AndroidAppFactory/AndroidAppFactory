package com.bihe0832.android.app.api

import android.text.TextUtils
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.constant.Constants
import com.bihe0832.android.lib.okhttp.wrapper.OkHttpWrapper
import com.bihe0832.android.lib.okhttp.wrapper.TIME_OUT_CONNECTION
import com.bihe0832.android.lib.okhttp.wrapper.TIME_OUT_READ
import com.bihe0832.android.lib.okhttp.wrapper.TIME_OUT_WRITE
import com.bihe0832.android.lib.okhttp.wrapper.convert.GsonConverterFactory
import com.bihe0832.android.lib.okhttp.wrapper.ext.getRequestBodyByJsonString
import com.bihe0832.android.lib.request.URLUtils
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Retrofit


/**
 * AAF 网络 API 工具类
 *
 * 提供网络请求相关的工具方法，包括：
 * - OkHttpClient 配置和创建
 * - Retrofit 实例创建
 * - 公共请求参数构建
 * - URL 拼接处理
 *
 * @author zixie code@bihe0832.com
 * Created on 2018/2/5.
 */
object AAFNetWorkApi {

    /** 默认的 OkHttpClient 实例 */
    private val mHttpClient by lazy {
        getHttpClientBy(TIME_OUT_CONNECTION, TIME_OUT_READ, TIME_OUT_WRITE)
    }

    /**
     * 创建自定义超时配置的 OkHttpClient
     *
     * @param connectTimeOut 连接超时时间（毫秒）
     * @param readTimeOut 读取超时时间（毫秒）
     * @param writeTimeOut 写入超时时间（毫秒）
     * @return 配置好的 OkHttpClient 实例
     */
    fun getHttpClientBy(connectTimeOut: Long, readTimeOut: Long, writeTimeOut: Long): OkHttpClient {
        return OkHttpWrapper.getOkHttpClientBuilder(
            ZixieContext.applicationContext!!,
            connectTimeOut,
            readTimeOut,
            writeTimeOut,
            !ZixieContext.isOfficial()
        ).apply {
            if (ZixieContext.enableLog()) {
                addNetworkInterceptor(
                    OkHttpWrapper.apply {
                        setMaxRequestNumInRequestCacheList(20)
                    }.generateNetworkInterceptor(ZixieContext.enableLog()),
                )
                eventListenerFactory {
                    AAFNetworkEventListener(
                        ZixieContext.enableLog(),
                        ZixieContext.enableLog(),
                        null,
                    )
                }
            }
        }.build()
    }

    /** 日志标签 */
    const val LOG_TAG = "RetrofitLog"

    /** 请求参数：应用版本 */
    const val REQUEST_PARAM_APP_VERSION = "version"

    /** 请求参数：操作系统 */
    const val REQUEST_PARAM_OS = "os"

    /** 请求参数：设备 ID */
    const val REQUEST_PARAM_DEVKEY = "devid"

    /** 请求参数：包名 */
    const val REQUEST_PARAM_PACKAGE_NAME = "package"

    /**
     * 构建带公共参数的完整 URL
     *
     * 自动拼接设备 ID、系统类型、版本号、包名等公共参数
     *
     * @param url 基础 URL
     * @param param 额外参数
     * @return 拼接后的完整 URL
     */
    fun getCommonURL(url: String?, param: String): String {
        if (TextUtils.isEmpty(url)) {
            return ""
        }
        val publicPara = StringBuffer()
        publicPara.append(REQUEST_PARAM_DEVKEY).append(URLUtils.HTTP_REQ_ENTITY_MERGE)
            .append(ZixieContext.deviceId)
        publicPara.append(URLUtils.HTTP_REQ_ENTITY_JOIN).append(REQUEST_PARAM_OS)
            .append(URLUtils.HTTP_REQ_ENTITY_MERGE).append(Constants.SYSTEM_CONSTANT)
        publicPara.append(URLUtils.HTTP_REQ_ENTITY_JOIN).append(REQUEST_PARAM_APP_VERSION)
            .append(URLUtils.HTTP_REQ_ENTITY_MERGE).append(ZixieContext.getVersionCode())
        publicPara.append(URLUtils.HTTP_REQ_ENTITY_JOIN).append(REQUEST_PARAM_PACKAGE_NAME)
            .append(URLUtils.HTTP_REQ_ENTITY_MERGE)
            .append(ZixieContext.applicationContext?.packageName)
        return URLUtils.marge(URLUtils.marge("$url", publicPara.toString()), param)
    }

    /**
     * 创建带 JSON 解析的 Retrofit 实例
     *
     * @param url 基础 URL
     * @return Retrofit 实例
     */
    fun getRetrofit(url: String): Retrofit {
        return Retrofit.Builder().client(mHttpClient).addConverterFactory(GsonConverterFactory())
            .baseUrl(url).build()
    }

    /**
     * 创建不带 JSON 解析的 Retrofit 实例
     *
     * @param url 基础 URL
     * @return Retrofit 实例
     */
    fun getRetrofitWithoutJsonParse(url: String): Retrofit {
        return Retrofit.Builder().client(mHttpClient).baseUrl(url).build()
    }

    /**
     * 获取公共请求参数的 JSONObject
     *
     * @return 包含公共参数的 JSONObject
     */
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

    /**
     * 获取公共请求参数的 RequestBody
     *
     * @return JSON 格式的 RequestBody
     */
    fun getRequestBody(): RequestBody {
        return getRequestBodyByJsonString(getRequestPara().toString())
    }
}
