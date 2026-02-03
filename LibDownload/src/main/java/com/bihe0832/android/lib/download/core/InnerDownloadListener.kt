package com.bihe0832.android.lib.download.core

import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadPauseType
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.core.dabase.DownloadInfoDBManager

/**
 * Summary
 * @author code@bihe0832.com
 * Created on 2024/3/29.
 * Description:
 *
 */
abstract class InnerDownloadListener : DownloadListener {
    override fun onWait(item: DownloadItem) {
        item.status = DownloadStatus.STATUS_DOWNLOAD_WAITING
        item.downloadListener?.onWait(item)
        DownloadInfoDBManager.saveDownloadInfo(item)
    }

    override fun onStart(item: DownloadItem) {
        item.status = DownloadStatus.STATUS_DOWNLOAD_STARTED
        item.lastSpeed = 0
        item.startTime = System.currentTimeMillis()
        item.downloadListener?.onStart(item)
        DownloadInfoDBManager.saveDownloadInfo(item)
    }

    override fun onProgress(item: DownloadItem) {
        item.status = DownloadStatus.STATUS_DOWNLOADING
        item.downloadListener?.onProgress(item)
    }

    override fun onPause(item: DownloadItem, @DownloadPauseType pauseType: Int) {
        item.status = DownloadStatus.STATUS_DOWNLOAD_PAUSED
        item.downloadListener?.onPause(item, pauseType)
    }

    override fun onDelete(item: DownloadItem) {
        item.downloadListener?.onDelete(item)
    }
}