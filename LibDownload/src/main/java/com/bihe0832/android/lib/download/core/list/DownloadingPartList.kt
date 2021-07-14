package com.bihe0832.android.lib.download.core.list

import com.bihe0832.android.lib.download.DownloadItem.TAG
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.part.DownloadThread
import com.bihe0832.android.lib.log.ZLog
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020/9/30.
 * Description: Description
 *
 */
object DownloadingPartList {
    //存储所有的请求链接信息
    private var mDownloadThreadForPartList = ConcurrentHashMap<String, DownloadThread>()

    @Synchronized
    fun getPartListById(downloadId: Long): CopyOnWriteArrayList<DownloadThread> {
        var dataList = mutableListOf<DownloadThread>()
        mDownloadThreadForPartList.entries.forEach { item ->
            if (item.value.getDownloadPartInfo().downloadID == downloadId) {
                dataList.add(item.value)
            }
        }
        return CopyOnWriteArrayList(dataList.toList())
    }

    @Synchronized
    fun getDownloadingPartNum(): Int {
        return mDownloadThreadForPartList.size
    }

    @Synchronized
    fun addDownloadingPart(item: DownloadThread) {
        if (!mDownloadThreadForPartList.containsKey(item.getDownloadPartInfo().downloadPartID)) {
            mDownloadThreadForPartList[item.getDownloadPartInfo().downloadPartID] = item
        }
    }

    @Synchronized
    fun removeItem(downloadId: Long, isFinished: Boolean) {
        mDownloadThreadForPartList.entries.iterator().let {
            while (it.hasNext()) {
                val item = it.next()
                if (item.value.getDownloadPartInfo().downloadID == downloadId) {
                    ZLog.d(TAG, "cancleDownload downloadList key:" + item.key)
                    if (!isFinished) {
                        item.value.getDownloadPartInfo().partStatus = DownloadStatus.STATUS_DOWNLOAD_PAUSED
                    }
                    it.remove()
                }
            }
        }
    }
}