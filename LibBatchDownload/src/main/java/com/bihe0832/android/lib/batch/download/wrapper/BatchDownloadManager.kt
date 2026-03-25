package com.bihe0832.android.lib.batch.download.wrapper

import android.content.Context
import com.bihe0832.android.lib.batch.download.BatchDownloadConfig
import com.bihe0832.android.lib.batch.download.BatchDownloadListener
import com.bihe0832.android.lib.batch.download.core.BatchDownloader
import com.bihe0832.android.lib.download.wrapper.DownloadFileUtils
import com.bihe0832.android.lib.log.ZLog

/**
 * 批量下载管理器
 *
 * 提供全局初始化和工厂方法，管理多个 BatchDownloader 实例的生命周期。
 *
 * 使用方式：
 * ```kotlin
 * // Application.onCreate 中初始化
 * BatchDownloadManager.init(context, isDebug = BuildConfig.DEBUG)
 *
 * // 创建下载任务
 * val downloader = BatchDownloadManager.create(config, listener)
 * downloader.startDownload(context, urls)
 *
 * // 查询活跃批次
 * val active = BatchDownloadManager.getActiveBatches()
 * ```
 *
 * @author zixie code@bihe0832.com
 */
object BatchDownloadManager {

    private const val TAG = "BatchDownloadManager"

    /** 活跃批次映射：batchId -> BatchDownloader */
    private val activeBatches = mutableMapOf<String, BatchDownloader>()

    /**
     * 初始化批量下载
     *
     * 内部调用 DownloadFileUtils.init，应在 Application.onCreate 中调用。
     */
    fun init(context: Context, isDebug: Boolean) {
        DownloadFileUtils.init(context, isDebug)
        ZLog.i(TAG, "init: 批量下载管理器初始化完成")
    }

    /**
     * 创建批量下载器实例
     *
     * @param config 批量下载配置
     * @param listener 批量下载回调
     * @return 新建的 BatchDownloader 实例
     */
    fun create(config: BatchDownloadConfig, listener: BatchDownloadListener): BatchDownloader {
        val downloader = BatchDownloader(config, listener)
        synchronized(activeBatches) {
            activeBatches[downloader.batchId] = downloader
        }
        ZLog.d(TAG, "create: 创建批次 ${downloader.batchId}")
        return downloader
    }

    /**
     * 根据 batchId 获取批量下载器实例
     */
    fun getDownloader(batchId: String): BatchDownloader? {
        synchronized(activeBatches) {
            return activeBatches[batchId]
        }
    }

    /**
     * 获取所有活跃的批量下载器
     */
    fun getActiveBatches(): List<BatchDownloader> {
        synchronized(activeBatches) {
            return activeBatches.values.toList()
        }
    }

    /**
     * 移除已完成/取消的批次（释放引用）
     *
     * @param batchId 批次唯一标识
     */
    fun remove(batchId: String) {
        synchronized(activeBatches) {
            activeBatches.remove(batchId)
        }
        ZLog.d(TAG, "remove: 移除批次 $batchId")
    }

    /**
     * 取消并移除所有活跃批次
     *
     * @param deleteFile 是否删除已下载的文件
     */
    fun cancelAll(deleteFile: Boolean = true) {
        synchronized(activeBatches) {
            activeBatches.values.forEach { it.cancel(deleteFile) }
            activeBatches.clear()
        }
        ZLog.i(TAG, "cancelAll: 已取消所有批次")
    }
}
