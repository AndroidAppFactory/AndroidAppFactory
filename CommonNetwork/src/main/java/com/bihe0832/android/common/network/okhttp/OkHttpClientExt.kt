/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/21 下午3:44
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/21 下午3:44
 *
 */

package com.bihe0832.android.common.network.okhttp

import com.bihe0832.android.framework.constant.Constants
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/6/21.
 * Description: Description
 *
 */

fun Response.getResponseData(): String {
    var jsonReader: Reader? = null
    var reader: BufferedReader? = null
    try {
        val responseBody = peekBody(Long.MAX_VALUE)
        val charset = responseBody.contentType()?.charset() ?: Constants.CHAR_SET_UTF8
        jsonReader = InputStreamReader(responseBody.byteStream(), charset)
        reader = BufferedReader(jsonReader)
        val result = StringBuffer()
        var line: String? = reader.readLine()
        do {
            result.append(line)
            line = reader.readLine()
        } while (line != null)
        return result.toString()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    } finally {
        try {
            jsonReader?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            reader?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return ""
}

fun Request.getRequestParams(): String {
    this.body()?.let {
        val buffer = okio.Buffer()
        try {
            it.writeTo(buffer)
            val charset = it.contentType()?.charset() ?: Constants.CHAR_SET_UTF8
            return buffer.readString(charset)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            try {
                buffer.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    return ""
}


fun getRequestBodyByJsonString(jsonString: String): RequestBody {
    return RequestBody.create(MediaType.parse("application/json"), jsonString)
}