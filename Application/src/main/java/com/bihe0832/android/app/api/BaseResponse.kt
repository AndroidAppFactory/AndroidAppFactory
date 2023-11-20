package com.bihe0832.android.app.api

import com.bihe0832.android.lib.gson.JsonHelper
import com.google.gson.annotations.SerializedName

open class BaseResponse {

    @SerializedName(value = "err_code", alternate = ["code"])
    open var errCode: Int = 0

    @SerializedName(value = "message", alternate = ["msg"])
    open var message: String = ""

    override fun toString(): String {
        return JsonHelper.toJson(this).toString()
    }
}
