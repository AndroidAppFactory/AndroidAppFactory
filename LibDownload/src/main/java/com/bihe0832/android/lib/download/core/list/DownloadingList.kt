package com.bihe0832.android.lib.download.core.list

import com.bihe0832.android.lib.download.DownloadItem
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020/9/30.
 * Description: Description
 *
 */
object DownloadingList {

    //所有在下载的id列表
    private var mDownloadList = ConcurrentHashMap<String, DownloadItem>()
    private var mDownLoadIdList = mutableListOf<Long>()

    private var listHasChanged = false
    private var lastCachedList = mDownloadList.values.toList()

    @Synchronized
    fun getDownloadingItemList(): List<DownloadItem> {
        if (listHasChanged) {
            lastCachedList = mDownloadList.values.toList()
        }
        return lastCachedList
    }

    @Synchronized
    fun getDownloadingNum(): Int {
        return mDownloadList.toList().size
    }

    fun isDownloading(item: DownloadItem): Boolean {
        return mDownLoadIdList.toList().contains(item.downloadID)
    }

    @Synchronized
    fun addToDownloadingList(item: DownloadItem) {
        if (!isDownloading(item)) {
            mDownLoadIdList.add(item.downloadID)
            mDownloadList[item.downloadID.toString()] = item
            listHasChanged = true
        }else{
            mDownloadList[item.downloadID.toString()]?.apply {
                this.downloadListener = item.downloadListener
            }
        }
    }

    @Synchronized
    fun removeFromDownloadingList(downloadId: Long) {
        mDownloadList.remove(downloadId.toString())
        mDownLoadIdList.remove(downloadId)
        listHasChanged = true
    }
}