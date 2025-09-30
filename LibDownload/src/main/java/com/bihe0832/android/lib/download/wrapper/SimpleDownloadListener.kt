package com.bihe0832.android.lib.download.wrapper

import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.log.ZLog

/**
 * @author zixie code@bihe0832.com Created on 2/1/21.
 */
abstract class SimpleDownloadListener : DownloadListener {
    override fun onWait(item: DownloadItem) {
        ZLog.d(DownloadItem.TAG, "onWait $item")
    }

    override fun onStart(item: DownloadItem) {
        ZLog.d(DownloadItem.TAG, "onStart $item")
    }

    override fun onPause(item: DownloadItem) {
        ZLog.d(DownloadItem.TAG, "onPause $item")
    }

    override fun onDelete(item: DownloadItem) {
        ZLog.d(DownloadItem.TAG, "onDelete $item")
    }
}