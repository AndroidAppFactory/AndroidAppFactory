package com.bihe0832.android.app.api.call

import com.bihe0832.android.lib.gson.JsonHelper
import com.google.gson.annotations.SerializedName

/** HTTP 请求失败 */
const val FLAG_FAILED_HTTP = -1

/** HTTP 响应体为空 */
const val FLAG_FAILED_BODY_EMPTY = -2

/** HTTP 响应解析异常 */
const val FLAG_FAILED_HTTP_EXCEPTION = -3

/** 请求成功 */
const val FLAG_SUCCESS = 0

/**
 * 网络响应基类
 *
 * 定义网络响应的基础结构，包含错误码和错误信息
 * 所有网络响应类都应继承此类
 *
 * @author zixie code@bihe0832.com
 * @since 1.0.0
 */
open class BaseResponse {

    /** 错误码，0 表示成功 */
    @SerializedName(value = "err_code", alternate = ["code"])
    open var errCode: Int = FLAG_SUCCESS

    /** 错误信息或提示消息 */
    @SerializedName(value = "message", alternate = ["msg"])
    open var message: String = ""

    override fun toString(): String {
        return JsonHelper.toJson(this).toString()
    }
}
