package com.bihe0832.android.lib.download.wrapper

import android.content.Context
import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.http.advanced.HttpAdvancedRequest.HttpRespParseError
import com.bihe0832.android.lib.http.advanced.HttpAdvancedRequest.RET_SUCC
import com.bihe0832.android.lib.http.common.HttpResponseHandler
import com.bihe0832.android.lib.log.ZLog


object DownloadConfig {

    //直接下载，不显示进度，4G下载直接下载
    fun startDownload(context: Context, url: String, md5: String, downloadListener: HttpResponseHandler) {
        startDownloadConfig(context, url, md5, downloadListener)
    }

    private fun startDownloadConfig(context: Context, url: String, md5: String, downloadListener: HttpResponseHandler) {
        DownloadUtils.startDownload(context, DownloadItem().apply {
            setNotificationVisibility(true)
            downloadURL = url
            md5?.let {
                fileMD5 = it
            }
            isDownloadWhenUseMobile = true
            setCanDownloadByPart(false)
            isForceDownloadNew = TextUtils.isEmpty(md5)
            this.downloadListener = object : SimpleDownloadListener() {
                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                    downloadListener.onResponse(errorCode, msg)
                }

                override fun onComplete(filePath: String, item: DownloadItem) {
                    try {
                        FileUtils.getFileContent(filePath).let {
                            downloadListener.onResponse(RET_SUCC, it)
                        }
                    } catch (e: Exception) {
                        downloadListener.onResponse(HttpRespParseError, e.message)
                    }
                }

                override fun onProgress(item: DownloadItem) {
                    ZLog.d("onProgress :$item")
                }
            }
        })
    }
}