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

    private val cachedListMap = ConcurrentHashMap<Int, CopyOnWriteArrayList<DownloadItem>>()

    /**
     * 使指定类型的缓存失效
     * @param downloadType 下载类型，为 null 时清空所有缓存
     */
    private fun invalidateCache(downloadType: Int? = null) {
        if (downloadType != null) {
            cachedListMap.remove(downloadType)
        } else {
            cachedListMap.clear()
        }
    }

    @Synchronized
    fun getDownloadTasKList(downType: Int): CopyOnWriteArrayList<DownloadItem> {
        return cachedListMap.getOrPut(downType) {
            CopyOnWriteArrayList(mDownloadList.values.filter { it.downloadType == downType })
        }
    }

    @Synchronized
    fun hadAddTask(item: DownloadItem): Boolean {
        return mDownLoadIdList.toList().contains(item.downloadID)
    }

    @Synchronized
    fun clear() {
        invalidateCache()
        return mDownloadList.clear()
    }


    @Synchronized
    fun addToDownloadTaskList(item: DownloadItem) {
        if (!hadAddTask(item)) {
            invalidateCache(item.downloadType)
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
            invalidateCache(item.downloadType)
            mDownloadList[item.downloadID]?.update(item)
        }
    }

    @Synchronized
    fun removeFromDownloadTaskList(downloadId: Long) {
        // 先获取要删除的 item 的类型，再精准失效
        val downloadType = mDownloadList[downloadId]?.downloadType
        invalidateCache(downloadType)
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