package com.bihe0832.android.app.api

import com.google.gson.annotations.SerializedName

open class AAFResponse<T> : BaseResponse() {

    @SerializedName(value = "content")
    var content: T? = null

    override fun toString(): String {
        return super.toString() + content?.toString()
    }
}
