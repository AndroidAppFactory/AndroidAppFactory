package com.bihe0832.android.lib.download.core

import com.bihe0832.android.lib.download.DownloadItem
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020/9/30.
 * Description: 在下载队列中的任务列表
 *
 */
public object DownloadTaskList {

    private val mDownloadList = ConcurrentHashMap<Long, DownloadItem>()
    private var mDownLoadIdList = CopyOnWriteArrayList<Long>()

    private var listHasChanged = false
    private var lastCachedList = CopyOnWriteArrayList(mDownloadList.values.toList())


    @Synchronized
    fun getDownloadTasKList(downType:Int): CopyOnWriteArrayList<DownloadItem> {
        if (listHasChanged) {
            lastCachedList = CopyOnWriteArrayList(mDownloadList.values.toList())
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
            updateDownloadTaskListItem(item)
            mDownloadList[item.downloadID]?.downloadListener = item.downloadListener
        }
    }

    @Synchronized
    fun updateDownloadTaskListItem(item: DownloadItem) {
        if (hadAddTask(item)) {
            listHasChanged = true
            mDownloadList[item.downloadID]?.update(item)
        }
    }

    @Synchronized
    fun removeFromDownloadTaskList(downloadId: Long) {
        listHasChanged = true
        mDownloadList.remove(downloadId)
        mDownLoadIdList.remove(downloadId)
    }

    @Synchronized
    fun getTaskByDownloadID(downloadID: Long): DownloadItem? {
        return mDownloadList[downloadID]
    }

    @Synchronized
    fun getTaskByDownloadURL(downloadURL: String, downloadActionKey: String): DownloadItem? {
        return mDownloadList[DownloadItem.getDownloadIDByURL(downloadURL, downloadActionKey)]
    }
}