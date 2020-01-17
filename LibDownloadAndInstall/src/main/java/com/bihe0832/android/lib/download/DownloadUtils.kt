package com.bihe0832.android.lib.download

import android.content.Context
import com.bihe0832.android.lib.download.manager.DownloadByDownloadManager
import com.bihe0832.android.lib.download.manager.DownloadByHttp

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020-01-10.
 * Description: Description
 *
 */

const val DOWNLOAD_TYPE_HTTP = 1
const val DOWNLOAD_TYPE_DOWNLOADMANAGER = 2

const val NO_DOWNLOAD = 0
const val IS_DOWNLOADING = 1
const val HAS_DOWNLOAD = 2
const val IS_DOWNLOADING_HTTP = 3
const val IS_DOWNLOADING_DM = 4

object DownloadUtils {

    private val httpDownload by lazy {
        DownloadByHttp()
    }

    private val downloadManagerDownload by lazy {
        DownloadByDownloadManager()
    }

    fun hasDownload(type: Int, context: Context, url: String, fileName: String, fileMD5: String, forceDownloadNew: Boolean): Int {
        return when (type) {
            DOWNLOAD_TYPE_HTTP -> httpDownload.hasDownload(context, url, fileName, fileMD5, forceDownloadNew)
            else -> downloadManagerDownload.hasDownload(context, url, fileName, fileMD5, forceDownloadNew)
        }
    }

    fun hasDownload(context: Context, url: String, fileName: String, fileMD5: String, forceDownloadNew: Boolean): Int {
        when (downloadManagerDownload.hasDownload(context, url, fileName, fileMD5, forceDownloadNew)) {
            NO_DOWNLOAD -> {
                when (httpDownload.hasDownload(context, url, fileName, fileMD5, forceDownloadNew)) {
                    NO_DOWNLOAD -> {
                        return NO_DOWNLOAD
                    }
                    IS_DOWNLOADING -> {
                        return IS_DOWNLOADING_HTTP
                    }
                }
            }
            IS_DOWNLOADING -> {
                return IS_DOWNLOADING_DM
            }
            HAS_DOWNLOAD -> {
                return HAS_DOWNLOAD
            }
        }
        return NO_DOWNLOAD
    }

    fun startDownload(type: Int, context: Context, info: DownloadItem, downloadListener: DownloadListener?) {
        when (type) {
            DOWNLOAD_TYPE_HTTP -> httpDownload.startDownload(context, info, downloadListener)
            else -> downloadManagerDownload.startDownload(context, info, downloadListener)
        }
    }

    fun startDownload(context: Context, info: DownloadItem, downloadListener: DownloadListener?) {
        downloadManagerDownload.startDownload(context, info, downloadListener)
    }

    fun cancleDownload(url: String) {
        downloadManagerDownload.cancleDownload(url)
        httpDownload.cancleDownload(url)
    }

    fun cancleDownload(type: Int, url: String) {
        when (type) {
            DOWNLOAD_TYPE_HTTP -> httpDownload.cancleDownload(url)
            else -> downloadManagerDownload.cancleDownload(url)
        }
    }
}