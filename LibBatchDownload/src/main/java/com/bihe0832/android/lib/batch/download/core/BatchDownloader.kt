package com.bihe0832.android.lib.batch.download.core

import android.content.Context
import com.bihe0832.android.lib.batch.download.BatchDownloadConfig
import com.bihe0832.android.lib.batch.download.BatchDownloadListener
import com.bihe0832.android.lib.batch.download.BatchStatus
import com.bihe0832.android.lib.batch.download.BatchStatusInfo
import com.bihe0832.android.lib.batch.download.ErrorStrategy
import com.bihe0832.android.lib.batch.download.SubTaskStatus
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadPauseType
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.wrapper.DownloadFileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.LinkedBlockingQueue

/**
 * 批量下载器
 *
 * 基于 DownloadFileUtils（底层单文件下载）封装的批量下载管理器，每个实例管理一个独立批次。
 * 提供：
 * - URL 去重与参数校验
 * - 批次层面的并发控制（maxConcurrent）
 * - 总进度聚合与节流回调
 * - 失败自动重试（maxRetryCount）
 * - 两种错误回调策略（WAIT_ALL / IMMEDIATE）
 * - 暂停/恢复/取消/重试等批次操作
 *
 * 使用方式：
 * ```kotlin
 * val downloader = BatchDownloader(config, listener)
 * downloader.startDownload(context, urls)
 * downloader.pause()
 * downloader.resume()
 * ```
 *
 * @param config 批量下载配置
 * @param listener 批量下载回调
 *
 * @author zixie code@bihe0832.com
 */
class BatchDownloader(
    config: BatchDownloadConfig,
    private val listener: BatchDownloadListener
) {

    companion object {
        private const val TAG = "BatchDownloader"
    }

    // ==================== 批次标识 ====================

    /** 批次唯一标识 */
    val batchId: String = "batch_${System.currentTimeMillis()}_${(Math.random() * 10000).toInt()}"

    // ==================== 配置（校验后） ====================

    /** 校验并修正后的配置 */
    val config: BatchDownloadConfig

    init {
        // 并发数校验与修正
        val maxConcurrent = when {
            config.maxConcurrent < 1 -> {
                ZLog.w(TAG, "maxConcurrent(${config.maxConcurrent}) < 1，已修正为 1")
                1
            }

            config.maxConcurrent > 5 -> {
                ZLog.w(TAG, "maxConcurrent(${config.maxConcurrent}) > 5，已修正为 5")
                5
            }

            else -> config.maxConcurrent
        }
        // 重试次数校验
        val maxRetryCount = config.maxRetryCount.coerceIn(0, 3)
        this.config = config.copy(maxConcurrent = maxConcurrent, maxRetryCount = maxRetryCount)
    }

    // ==================== 实例状态 ====================

    /** 批次状态 */
    @Volatile
    var status: BatchStatus = BatchStatus.RUNNING
        private set

    /** 错误处理策略，默认 WAIT_ALL，可在运行时动态修改 */
    @Volatile
    var errorStrategy: ErrorStrategy = ErrorStrategy.WAIT_ALL

    /** 是否已暂停 */
    @Volatile
    private var isPaused: Boolean = false

    /** 上次回调的整数百分比，用于进度节流 */
    @Volatile
    private var lastReportedProgress: Int = -1

    /** 上次回调的已完成数，用于检测完成时强制通知 */
    @Volatile
    private var lastReportedCompleted: Int = -1

    /** Context 引用（applicationContext） */
    private var contextRef: Context? = null

    /** 内部 DownloadListener，创建一次后复用 */
    private val innerListener: DownloadListener by lazy { createInnerListener() }

    /** 已提交给底层下载引擎的子任务：URL -> DownloadItem，仅包含活跃/已完成的 item，按需创建 */
    private val activeItems = ConcurrentHashMap<String, DownloadItem>()

    /** URL 列表（去重后），仅保存字符串，不创建 DownloadItem */
    private var urlList: List<String> = emptyList()

    /** downloadId -> URL 映射 */
    private val downloadIdToUrlMap = ConcurrentHashMap<Long, String>()

    /** 已完成的子任务：URL -> filePath */
    private val completedMap = ConcurrentHashMap<String, String>()

    /** 失败的子任务：URL -> errorCode */
    private val failedMap = ConcurrentHashMap<String, Int>()

    /** 自动重试计数：URL -> 已重试次数 */
    private val retryCountMap = ConcurrentHashMap<String, Int>()

    /** 内部待调度队列（尚未提交给底层的 URL） */
    private val pendingQueue = LinkedBlockingQueue<String>()

    /** 已提交给底层的 URL 集合 */
    private val submittedUrls = CopyOnWriteArraySet<String>()

    // ==================== 公开 API ====================

    /**
     * 启动批量下载
     *
     * @param context 上下文
     * @param urls 下载 URL 列表
     * @return true 启动成功，false 启动失败（URL 列表为空）
     */
    fun startDownload(context: Context, urls: List<String>): Boolean {
        // 参数校验：空列表
        if (urls.isEmpty()) {
            ZLog.e(TAG, "startDownload: URL 列表为空")
            ThreadManager.getInstance().runOnUIThread {
                listener.onError(batchId, emptyMap(), emptyList())
            }
            return false
        }

        // URL 去重（保持顺序）
        val uniqueUrls = LinkedHashSet(urls).toList()
        ZLog.d(TAG, "startDownload: 原始 ${urls.size} 个 URL，去重后 ${uniqueUrls.size} 个")

        this.urlList = uniqueUrls
        this.contextRef = context.applicationContext

        // 所有 URL 放入待调度队列（只存字符串，不创建 DownloadItem）
        uniqueUrls.forEach { pendingQueue.offer(it) }

        status = BatchStatus.RUNNING

        ZLog.i(
            TAG,
            "startDownload: 批次 $batchId 创建成功，共 ${uniqueUrls.size} 个子任务，并发数 ${config.maxConcurrent}"
        )

        // 启动首批下载（此时才在 startNextTasks 中按需创建 DownloadItem）
        startNextTasks()

        return true
    }

    /**
     * 暂停下载
     */
    fun pause() {
        isPaused = true
        status = BatchStatus.PAUSED

        // 遍历已提交给底层的子任务，逐个暂停
        activeItems.values.forEach { item ->
            val itemStatus = item.status
            if (itemStatus == DownloadStatus.STATUS_DOWNLOADING || itemStatus == DownloadStatus.STATUS_DOWNLOAD_WAITING) {
                DownloadFileUtils.pauseDownload(item.downloadID, false)
            }
        }

        ZLog.i(TAG, "pause: 批次 $batchId 已暂停")
    }

    /**
     * 恢复下载
     */
    fun resume() {
        isPaused = false
        status = BatchStatus.RUNNING

        // 恢复已暂停的子任务
        activeItems.values.forEach { item ->
            if (item.status == DownloadStatus.STATUS_DOWNLOAD_PAUSED) {
                DownloadFileUtils.resumeDownload(item.downloadID, !config.downloadWhenUseMobile)
            }
        }

        // 补满并发数
        startNextTasks()

        ZLog.i(TAG, "resume: 批次 $batchId 已恢复")
    }

    /**
     * 取消并删除批次
     *
     * @param deleteFile 是否删除已下载的文件，默认 true
     */
    fun cancel(deleteFile: Boolean = true) {
        status = BatchStatus.CANCELLED
        isPaused = true  // 阻止后续调度

        // 删除所有已提交的子任务
        activeItems.values.forEach { item ->
            DownloadFileUtils.deleteTask(item.downloadID, deleteFile)
        }

        // 释放引用
        contextRef = null
        pendingQueue.clear()
        activeItems.clear()
        submittedUrls.clear()

        ZLog.i(TAG, "cancel: 批次 $batchId 已取消并清理")
    }

    /**
     * 重试所有失败的子任务
     */
    fun resumeFailed() {
        val failedUrls = failedMap.keys.toList()
        if (failedUrls.isEmpty()) {
            ZLog.d(TAG, "resumeFailed: 批次 $batchId 没有失败的子任务")
            return
        }

        failedUrls.forEach { url ->
            val item = activeItems[url] ?: return@forEach
            retryCountMap[url] = 0
            failedMap.remove(url)
            DownloadFileUtils.resumeDownload(item.downloadID, !config.downloadWhenUseMobile)
        }

        status = BatchStatus.RUNNING
        isPaused = false
        startNextTasks()

        ZLog.i(TAG, "resumeFailed: 批次 $batchId 重试 ${failedUrls.size} 个失败任务")
    }

    /**
     * 重试指定 URL 的子任务
     */
    fun resumeByUrl(url: String) {
        if (!failedMap.containsKey(url)) {
            ZLog.w(TAG, "resumeByUrl: URL $url 不在失败列表中")
            return
        }

        val item = activeItems[url] ?: run {
            ZLog.w(TAG, "resumeByUrl: 找不到 URL $url 对应的 DownloadItem")
            return
        }

        retryCountMap[url] = 0
        failedMap.remove(url)
        DownloadFileUtils.resumeDownload(item.downloadID, !config.downloadWhenUseMobile)

        if (status == BatchStatus.FAILED) {
            status = BatchStatus.RUNNING
            isPaused = false
        }

        ZLog.i(TAG, "resumeByUrl: 批次 $batchId 重试 URL: $url")
    }

    /**
     * 获取所有子任务（包括活跃、已完成、失败和待调度的）
     *
     * 注意：待调度的 URL 会创建轻量级 DownloadItem 占位对象返回，不会注册到底层下载引擎
     */
    fun getAll(): List<DownloadItem> {
        val result = mutableListOf<DownloadItem>()
        // 已提交的任务
        result.addAll(activeItems.values)
        // 已完成的（可能已从 activeItems 移除，补充进来）
        completedMap.keys.forEach { url ->
            if (!activeItems.containsKey(url)) {
                result.add(DownloadItem().apply {
                    downloadURL = url
                    fileFolder = config.fileFolder
                })
            }
        }
        // 失败的（可能已从 activeItems 移除）
        failedMap.keys.forEach { url ->
            if (!activeItems.containsKey(url) && !completedMap.containsKey(url)) {
                result.add(DownloadItem().apply {
                    downloadURL = url
                    fileFolder = config.fileFolder
                })
            }
        }
        // 待调度的（仅在 pendingQueue 中的）
        val scheduledUrls = activeItems.keys.toSet() + completedMap.keys + failedMap.keys
        pendingQueue.forEach { url ->
            if (url !in scheduledUrls) {
                result.add(DownloadItem().apply {
                    downloadURL = url
                    fileFolder = config.fileFolder
                })
            }
        }
        return result
    }

    /**
     * 获取已完成的子任务
     */
    fun getFinished(): List<DownloadItem> {
        return activeItems.values.filter {
            it.status == DownloadStatus.STATUS_DOWNLOAD_SUCCEED || it.status == DownloadStatus.STATUS_HAS_DOWNLOAD
        }
    }

    /**
     * 获取正在下载的子任务
     */
    fun getDownloading(): List<DownloadItem> {
        return activeItems.values.filter {
            it.status == DownloadStatus.STATUS_DOWNLOADING
        }
    }

    /**
     * 获取正在等待的子任务
     */
    fun getWaiting(): List<DownloadItem> {
        return activeItems.values.filter {
            it.status == DownloadStatus.STATUS_DOWNLOAD_WAITING
        }
    }

    /**
     * 查询指定 URL 的子任务，如果还未提交到底层则返回 null
     */
    fun getTaskByDownloadURL(url: String): DownloadItem? {
        return activeItems[url]
    }

    /**
     * 获取批次的聚合状态信息
     */
    fun getStatus(): BatchStatusInfo {
        val taskStatusList = mutableListOf<SubTaskStatus>()

        // 已提交的任务（从 activeItems 获取完整状态）
        activeItems.values.forEach { item ->
            taskStatusList.add(
                SubTaskStatus(
                    url = item.downloadURL,
                    downloadId = item.downloadID,
                    status = item.status,
                    progress = calculateItemProgress(item),
                    filePath = item.filePath ?: "",
                    errorCode = failedMap[item.downloadURL] ?: 0
                )
            )
        }

        // 已完成但可能已从 activeItems 清理的
        completedMap.keys.forEach { url ->
            if (!activeItems.containsKey(url)) {
                taskStatusList.add(
                    SubTaskStatus(
                        url = url,
                        downloadId = 0,
                        status = DownloadStatus.STATUS_DOWNLOAD_SUCCEED,
                        progress = 100,
                        filePath = completedMap[url] ?: "",
                        errorCode = 0
                    )
                )
            }
        }

        // 失败的但可能已从 activeItems 清理的
        failedMap.keys.forEach { url ->
            if (!activeItems.containsKey(url) && !completedMap.containsKey(url)) {
                taskStatusList.add(
                    SubTaskStatus(
                        url = url,
                        downloadId = 0,
                        status = DownloadStatus.STATUS_DOWNLOAD_FAILED,
                        progress = 0,
                        filePath = "",
                        errorCode = failedMap[url] ?: 0
                    )
                )
            }
        }

        // 待调度的（仅在 pendingQueue 中，尚未创建 DownloadItem）
        val scheduledUrls = activeItems.keys.toSet() + completedMap.keys + failedMap.keys
        pendingQueue.forEach { url ->
            if (url !in scheduledUrls) {
                taskStatusList.add(
                    SubTaskStatus(
                        url = url,
                        downloadId = 0,
                        status = DownloadStatus.STATUS_DOWNLOAD_WAITING,
                        progress = 0,
                        filePath = "",
                        errorCode = 0
                    )
                )
            }
        }

        return BatchStatusInfo(
            batchId = batchId,
            status = status,
            progress = calculateBatchProgress(),
            completedCount = completedMap.size,
            totalCount = urlList.size,
            errorStrategy = errorStrategy,
            taskStatusList = taskStatusList
        )
    }

    // ==================== 内部方法 ====================

    /**
     * 子任务调度：从待下载队列中取出任务提交给底层
     */
    private fun startNextTasks() {
        if (isPaused) return
        if (status == BatchStatus.CANCELLED) return

        val context = contextRef ?: run {
            ZLog.e(TAG, "startNextTasks: Context 已释放，无法调度")
            return
        }

        // 统计当前已提交且正在活跃的子任务数
        val activeCount = activeItems.values.count { item ->
            val itemStatus = item.status
            itemStatus == DownloadStatus.STATUS_DOWNLOADING ||
                    itemStatus == DownloadStatus.STATUS_DOWNLOAD_WAITING ||
                    itemStatus == DownloadStatus.STATUS_DOWNLOAD_STARTED
        }

        var slotsAvailable = config.maxConcurrent - activeCount

        while (slotsAvailable > 0) {
            val url = pendingQueue.poll() ?: break

            // 按需创建 DownloadItem（仅当真正需要提交时）
            val item = DownloadItem().apply {
                downloadURL = url
                fileFolder = config.fileFolder
                isDownloadWhenUseMobile = config.downloadWhenUseMobile
                isDownloadWhenAdd = true
                downloadPriority = DownloadItem.MIN_DOWNLOAD_PRIORITY
                downloadListener = innerListener
            }

            activeItems[url] = item
            submittedUrls.add(url)
            DownloadFileUtils.startDownload(context, item)
            downloadIdToUrlMap[item.downloadID] = url

            ZLog.d(TAG, "startNextTasks: 提交子任务 $url, downloadId=${item.downloadID}")
            slotsAvailable--
        }
    }

    /**
     * 为子任务创建内部 DownloadListener
     */
    private fun createInnerListener(): DownloadListener {
        return object : DownloadListener {

            override fun onWait(item: DownloadItem) {
                ZLog.d(TAG, "onWait: ${item.downloadURL}")
            }

            override fun onStart(item: DownloadItem) {
                downloadIdToUrlMap[item.downloadID] = item.downloadURL
                ZLog.d(TAG, "onStart: ${item.downloadURL}")
            }

            override fun onProgress(item: DownloadItem) {
                if (isPaused) return
                notifyBatchProgress()
            }

            override fun onPause(item: DownloadItem, @DownloadPauseType pauseType: Int) {
                ZLog.d(TAG, "onPause: ${item.downloadURL}, pauseType=$pauseType")
            }

            override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                val url = item.downloadURL
                ZLog.w(TAG, "onFail: $url, errorCode=$errorCode, msg=$msg")

                // 检查自动重试
                if (config.maxRetryCount > 0) {
                    val retryCount = if (retryCountMap.containsKey(url)) retryCountMap[url]!! else 0
                    if (retryCount < config.maxRetryCount) {
                        retryCountMap[url] = retryCount + 1
                        ZLog.i(
                            TAG,
                            "onFail: 自动重试 $url, 第 ${retryCount + 1}/${config.maxRetryCount} 次"
                        )
                        DownloadFileUtils.resumeDownload(
                            item.downloadID,
                            !config.downloadWhenUseMobile
                        )
                        return
                    }
                }

                // 已达最大重试次数或不自动重试：标记为最终失败
                failedMap[url] = errorCode
                notifyBatchProgress()

                // 根据 errorStrategy 决定是否立即回调
                if (errorStrategy == ErrorStrategy.IMMEDIATE) {
                    val failedEntry = mapOf(url to errorCode)
                    val completedUrls = completedMap.keys.toList()
                    ThreadManager.getInstance().runOnUIThread {
                        listener.onError(batchId, failedEntry, completedUrls)
                    }
                }

                startNextTasks()
                checkBatchCompletion()
            }

            override fun onComplete(filePath: String, item: DownloadItem): String {
                val url = item.downloadURL
                ZLog.i(TAG, "onComplete: $url -> $filePath")

                completedMap[url] = filePath
                // 文件完成时强制通知进度（即使百分比未变，completedCount 已变）
                notifyBatchProgress(force = true)
                startNextTasks()
                checkBatchCompletion()

                return filePath
            }

            override fun onDelete(item: DownloadItem) {
                ZLog.d(TAG, "onDelete: ${item.downloadURL}")
            }
        }
    }

    /**
     * 检查批次是否已全部结束
     */
    private fun checkBatchCompletion() {
        if (status == BatchStatus.CANCELLED) return

        val totalCount = urlList.size
        val completedCount = completedMap.size
        val failedCount = failedMap.size
        val finishedCount = completedCount + failedCount

        if (finishedCount < totalCount) {
            val hasActive = activeItems.values.any { item ->
                val itemStatus = item.status
                itemStatus == DownloadStatus.STATUS_DOWNLOADING ||
                        itemStatus == DownloadStatus.STATUS_DOWNLOAD_WAITING ||
                        itemStatus == DownloadStatus.STATUS_DOWNLOAD_STARTED
            }
            val hasPending = !pendingQueue.isEmpty()
            if (hasActive || hasPending) return
        }

        // 所有子任务都已结束
        if (failedCount == 0) {
            status = BatchStatus.COMPLETED
            val filePaths = HashMap(completedMap)
            ThreadManager.getInstance().runOnUIThread {
                listener.onComplete(batchId, filePaths)
            }
            ZLog.i(TAG, "checkBatchCompletion: 批次 $batchId 全部完成")
        } else {
            status = BatchStatus.FAILED
            if (errorStrategy == ErrorStrategy.WAIT_ALL) {
                val failedUrls = HashMap(failedMap)
                val completedUrls = completedMap.keys.toList()
                ThreadManager.getInstance().runOnUIThread {
                    listener.onError(batchId, failedUrls, completedUrls)
                }
            }
            ZLog.i(TAG, "checkBatchCompletion: 批次 $batchId 存在 $failedCount 个失败")
        }
    }

    /**
     * 通知批次进度
     *
     * @param force 是否强制通知（当文件完成/失败时，即使百分比未变也要通知，因为 completedCount 已变）
     */
    private fun notifyBatchProgress(force: Boolean = false) {
        val progress = calculateBatchProgress()
        val completedCount = completedMap.size
        val totalCount = urlList.size
        val totalSpeed = activeItems.values.sumOf { it.lastSpeed }

        if (force || progress != lastReportedProgress) {
            lastReportedProgress = progress
            lastReportedCompleted = completedCount
            ThreadManager.getInstance().runOnUIThread {
                listener.onProgress(
                    batchId,
                    progress,
                    completedCount,
                    totalCount,
                    totalSpeed
                )
            }
        }
    }

    /**
     * 计算批次总进度
     */
    private fun calculateBatchProgress(): Int {
        if (urlList.isEmpty()) return 0

        var totalFinished = 0L
        var totalContentLength = 0L
        var knownCount = 0
        var knownTotalLength = 0L

        val items: Collection<DownloadItem> = activeItems.values
        items.forEach { item ->
            val contentLength: Long = item.contentLength
            val finished: Long = item.finished

            if (contentLength > 0) {
                totalContentLength += contentLength
                totalFinished += finished
                knownCount++
                knownTotalLength += contentLength
            } else {
                totalFinished += finished
            }
        }

        val totalItemCount = urlList.size
        val unknownCount = totalItemCount - knownCount
        if (unknownCount > 0 && knownCount > 0) {
            val avgSize = knownTotalLength / knownCount
            totalContentLength += avgSize * unknownCount
            // 已完成项贡献 avgSize 的下载量
            totalFinished += avgSize * completedMap.size
        } else if (unknownCount > 0 && knownCount == 0) {
            return (completedMap.size * 100 / urlList.size)
        }

        return if (totalContentLength > 0) {
            (totalFinished * 100 / totalContentLength).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }

    /**
     * 计算单个子任务的进度百分比
     */
    private fun calculateItemProgress(item: DownloadItem): Int {
        val contentLength = item.contentLength
        return if (contentLength > 0) {
            (item.finished * 100 / contentLength).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }
}
