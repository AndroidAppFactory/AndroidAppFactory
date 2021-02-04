package com.bihe0832.android.framework.request

import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.download.wrapper.DownloadConfig
import com.bihe0832.android.lib.utils.ConvertUtils

/**
 *
 * 配置下载及校验
 *  下载URL的格式为：URL;MD5，例如：http://blog.bihe0832.com;7478b16f5acd0a6febb7f7e3d9298f3d
 *  下载以后默认会强校验MD5是否正确，如果关闭校验MD5
 *  下载以后默认会保存本地，下次会优先尝试使用本地数据
 */

class ZixieRequestConfig {

    //支持MD5检查，使用本地缓存
    fun get(urlWithMD5: String, callback: DownloadConfig.ResponseHandler) {
        var configValueArray = urlWithMD5.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val configUrl = ConvertUtils.getSafeValueFromArray(configValueArray, 0, "")
        val configMd5 = ConvertUtils.getSafeValueFromArray(configValueArray, 1, "")
        get(configUrl, configMd5, callback)
    }

    //支持MD5检查，使用本地缓存
    fun get(url: String, md5: String, callback: DownloadConfig.ResponseHandler) {
        DownloadConfig.startDownload(ZixieContext.applicationContext!!, url, md5, callback)
    }
}