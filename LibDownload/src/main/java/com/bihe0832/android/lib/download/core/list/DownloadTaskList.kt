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
object DownloadTaskList {

    //所有在下载的id列表
    private val mDownloadList = ConcurrentHashMap<Long, DownloadItem>()
    private var mDownLoadIdList = mutableListOf<Long>()

    private var listHasChanged = false
    private var lastCachedList = mDownloadList.values.toList()


    @Synchronized
    fun getDownloadTasKList(): List<DownloadItem> {
        if (listHasChanged) {
            lastCachedList = mDownloadList.values.toList()
        }
        return lastCachedList
    }

    @Synchronized
    fun hadAddTask(item: DownloadItem): Boolean {
        return mDownLoadIdList.toList().contains(item.downloadID)
    }

    @Synchronized
    fun clear() {
        listHasChanged = true
        return mDownloadList.clear()
    }


    @Synchronized
    fun addToDownloadTaskList(item: DownloadItem) {
        if (!hadAddTask(item)) {
            listHasChanged = true
            mDownloadList[item.downloadID] = item
            mDownLoadIdList.add(item.downloadID)
        } else {
            mDownloadList[item.downloadID]?.downloadListener = item.downloadListener
        }
    }

    @Synchronized
    fun removeFromDownloadTaskList(downloadId: Long) {
        mDownloadList.remove(downloadId)
        mDownLoadIdList.remove(downloadId)
        listHasChanged = true
    }

    @Synchronized
    fun getTaskByDownloadID(downloadID: Long): DownloadItem? {
        return mDownloadList[downloadID]
    }


    @Synchronized
    fun getTaskByDownloadURL(downloadURL: String): DownloadItem? {
        return mDownloadList[DownloadItem.getDownloadIDByURL(downloadURL)]
    }
}