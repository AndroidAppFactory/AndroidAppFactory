package com.bihe0832.android.app.api.call

import com.google.gson.annotations.SerializedName

/**
 * AAF 通用响应封装类
 *
 * 继承自 BaseResponse，增加泛型内容字段用于承载业务数据
 *
 * @param T 业务数据类型
 *
 * @author zixie code@bihe0832.com
 * @since 1.0.0
 */
open class AAFResponse<T> : BaseResponse() {

    /** 响应的业务数据内容 */
    @SerializedName(value = "content")
    var content: T? = null

    override fun toString(): String {
        return super.toString() + content?.toString()
    }
}
