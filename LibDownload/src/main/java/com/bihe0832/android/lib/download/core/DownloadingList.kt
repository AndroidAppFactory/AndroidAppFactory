package com.bihe0832.android.lib.download.core

import com.bihe0832.android.lib.download.DownloadItem
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020/9/30.
 * Description: 正在下载的任务列表
 *
 */
object DownloadingList {

    //所有在下载的id列表
    private var mDownloadList = ConcurrentHashMap<String, DownloadItem>()
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
    fun getDownloadingItemList(downType: Int): CopyOnWriteArrayList<DownloadItem> {
        return cachedListMap.getOrPut(downType) {
            CopyOnWriteArrayList(mDownloadList.values.filter { it.downloadType == downType })
        }
    }

    /**
     * 获取所有正在下载的任务（不区分类型）
     * 用于进度检查等需要遍历所有任务的场景
     */
    @Synchronized
    fun getAllDownloadingItemList(): CopyOnWriteArrayList<DownloadItem> {
        return CopyOnWriteArrayList(mDownloadList.values.toList())
    }

    @Synchronized
    fun getDownloadingNum(): Int {
        return mDownloadList.size
    }

    @Synchronized
    fun isDownloading(item: DownloadItem): Boolean {
        return mDownLoadIdList.contains(item.downloadID)
    }

    @Synchronized
    fun addToDownloadingList(item: DownloadItem) {
        if (!isDownloading(item)) {
            invalidateCache(item.downloadType)
            mDownLoadIdList.add(item.downloadID)
            mDownloadList[item.downloadID.toString()] = item
        } else {
            mDownloadList[item.downloadID.toString()]?.apply {
                this.downloadListener = item.downloadListener
            }
        }
    }

    @Synchronized
    fun removeFromDownloadingList(downloadId: Long) {
        // 先获取要删除的 item 的类型，再精准失效
        val downloadType = mDownloadList[downloadId.toString()]?.downloadType
        invalidateCache(downloadType)
        mDownloadList.remove(downloadId.toString())
        mDownLoadIdList.remove(downloadId)
    }
}