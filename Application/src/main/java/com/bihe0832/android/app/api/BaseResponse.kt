package com.bihe0832.android.app.api

import com.bihe0832.android.lib.gson.JsonHelper
import com.google.gson.annotations.SerializedName

// client http 失败
const val FLAG_FAILED_HTTP = -1

// client http body 为空
const val FLAG_FAILED_BODY_EMPTY = -2

// 解析client 出错
const val FLAG_FAILED_HTTP_EXCEPTION = -3

// success
const val FLAG_SUCCESS = 0

open class BaseResponse {

    @SerializedName(value = "err_code", alternate = ["code"])
    open var errCode: Int = FLAG_SUCCESS

    @SerializedName(value = "message", alternate = ["msg"])
    open var message: String = ""

    override fun toString(): String {
        return JsonHelper.toJson(this).toString()
    }
}
