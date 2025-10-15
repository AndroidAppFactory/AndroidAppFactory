package com.bihe0832.android.lib.download.range

import com.bihe0832.android.lib.download.DownloadErrorCode
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadItem.TAG
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.core.DownloadByHttpBase
import com.bihe0832.android.lib.log.ZLog


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-10.
 * Description: 文件下载引擎的具体实现
 *
 */
open class DownloadByHttpForRange(
    private val innerDownloadListener: DownloadListener,
    maxNum: Int,
    isDebug: Boolean = false
) : DownloadByHttpBase(maxNum, isDebug) {

    override fun onFail(item: DownloadItem, errorCode: Int, msg: String) {
        if (item.status != DownloadStatus.STATUS_DOWNLOAD_PAUSED) {
            innerDownloadListener.onFail(errorCode, msg, item)
        }
    }

    override fun notifyDownloadAfterFinish(downloadInfo: DownloadItem) {
        closeDownload(downloadInfo.downloadID, finishDownload = true, clearDownloadHistory = false)
        innerDownloadListener.onComplete(downloadInfo.filePath, downloadInfo)
    }

    override fun notifyProcess(downloadItem: DownloadItem) {
        innerDownloadListener.onProgress(downloadItem)
    }

    override fun notifyWait(info: DownloadItem) {
        innerDownloadListener.onWait(info)
    }

    override fun notifyStart(info: DownloadItem) {
        innerDownloadListener.onStart(info)
    }

    fun startDownload(info: DownloadItem, rangeStart: Long, rangeLength: Long, localStart: Long) {

        ZLog.e(TAG, "开始下载:${info}")
        try {
            startDownload(info, DownloadItem.TYPE_RANGE, rangeStart, rangeLength, localStart)
        } catch (e: Throwable) {
            e.printStackTrace()
            if (info.status != DownloadStatus.STATUS_DOWNLOAD_PAUSED) {
                notifyDownloadFailed(
                    info, DownloadErrorCode.ERR_DOWNLOAD_EXCEPTION, "download with exception$e"
                )
            }
        }
    }

    internal fun notifyDownloadSucc(downloadInfo: DownloadItem): String {
        return innerDownloadListener.onComplete(downloadInfo.filePath, downloadInfo)
    }
}