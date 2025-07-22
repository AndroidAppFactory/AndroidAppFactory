package com.bihe0832.android.base.compose.debug.list.model

import com.bihe0832.android.app.api.BaseResponse
import com.google.gson.annotations.SerializedName

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/21.
 * Description: Description
 *
 */
// 数据结构
data class TopicResponse(

    @field:SerializedName("totalItems") val totalItems: Int? = null,

    @field:SerializedName("data") val data: List<DataItem>? = null,

    @field:SerializedName("totalPages") val totalPages: Int? = null,

    @field:SerializedName("pageSize") val pageSize: Int? = null
) : BaseResponse()

data class DataItem(

    @field:SerializedName("summary") val summary: String? = null,

    @field:SerializedName("authorName") val authorName: String? = null,

    @field:SerializedName("publishDate") val publishDate: String? = null,

    @field:SerializedName("siteName") val siteName: String? = null,

    @field:SerializedName("language") val language: String? = null,

    @field:SerializedName("id") val id: Int? = null,

    @field:SerializedName("mobileUrl") val mobileUrl: String? = null,

    @field:SerializedName("title") var title: String? = null,

    @field:SerializedName("url") val url: String? = null
)
