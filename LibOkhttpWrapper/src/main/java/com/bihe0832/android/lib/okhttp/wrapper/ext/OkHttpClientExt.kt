/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/21 下午3:44
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/21 下午3:44
 *
 */

package com.bihe0832.android.lib.okhttp.wrapper.ext

import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.okhttp.wrapper.OkHttpWrapper
import com.bihe0832.android.lib.okhttp.wrapper.convert.GsonRequestBodyConverter
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.Charset

/**
 * OkHttp 扩展函数
 *
 * 提供 Request 和 Response 的扩展方法，用于获取请求参数和响应数据
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/6/21.
 *
 * @since 1.0.0
 */


/**
 * 获取响应体数据
 *
 * 使用 peekBody 读取响应内容，不会消耗原始响应流
 *
 * @param enableLog 是否打印异常日志
 * @return 响应体字符串
 */
fun Response.getResponseData(enableLog: Boolean): String {
    var jsonReader: Reader? = null
    var reader: BufferedReader? = null
    val result = StringBuffer()
    try {
        val responseBody = peekBody(Long.MAX_VALUE)
        val charset = responseBody.contentType()?.charset() ?: Charset.forName("UTF-8")
        jsonReader = InputStreamReader(responseBody.byteStream(), charset)
        reader = BufferedReader(jsonReader)

        var line: String? = reader.readLine()
        do {
            result.append(line)
            line = reader.readLine()
        } while (line != null)
    } catch (e: java.lang.Exception) {
        ZLog.e(OkHttpWrapper.TAG, "getResponseData cause Exception:$e")
        if (enableLog) {
            e.printStackTrace()
        }
    } finally {
        try {
            jsonReader?.close()
        } catch (e: Exception) {
            if (enableLog) {
                e.printStackTrace()
            }
        }
        try {
            reader?.close()
        } catch (e: Exception) {
            if (enableLog) {
                e.printStackTrace()
            }
        }
    }
    return result.toString()
}

/**
 * 获取请求体参数
 *
 * 读取请求体内容并转换为字符串
 *
 * @param enableLog 是否打印异常日志
 * @return 请求体字符串，无请求体时返回空字符串
 */
fun Request.getRequestParams(enableLog: Boolean): String {
    this.body?.let {
        val buffer = okio.Buffer()
        try {
            it.writeTo(buffer)
            val charset = it.contentType()?.charset() ?: Charset.forName("UTF-8")
            return buffer.readString(charset)
        } catch (e: java.lang.Exception) {
            ZLog.e(OkHttpWrapper.TAG, "getResponseData cause Exception:$e")
            if (enableLog) {
                e.printStackTrace()
            }
        } finally {
            try {
                buffer.close()
            } catch (e: Exception) {
                if (enableLog) {
                    e.printStackTrace()
                }
            }
        }
    }
    return ""
}

/**
 * 根据 JSON 字符串创建请求体
 *
 * @param jsonString JSON 格式的字符串
 * @return RequestBody 实例
 */
fun getRequestBodyByJsonString(jsonString: String): RequestBody {
    return RequestBody.create(GsonRequestBodyConverter.MEDIA_TYPE, jsonString)
}