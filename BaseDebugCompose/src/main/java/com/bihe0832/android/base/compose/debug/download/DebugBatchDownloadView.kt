package com.bihe0832.android.base.compose.debug.download

import android.content.Context
import androidx.compose.runtime.Composable
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.lib.batch.download.BatchDownloadConfig
import com.bihe0832.android.lib.batch.download.BatchDownloadListener
import com.bihe0832.android.lib.batch.download.ErrorStrategy
import com.bihe0832.android.lib.batch.download.core.BatchDownloader
import com.bihe0832.android.lib.batch.download.wrapper.BatchDownloadManager
import com.bihe0832.android.lib.log.ZLog

private const val TAG = "BatchDownloadTest"

/** 当前正在测试的下载器实例 */
private var currentDownloader: BatchDownloader? = null

/** 测试下载 URL 列表 */
private val TEST_URLS = listOf(
    "https://cdn.bihe0832.com/app/update/get_apk.json",
    "https://dldir1.qq.com/INO/voice/taimei_trylisten.m4a",
    "https://android.bihe0832.com/app/release/ZPUZZLE_official.apk",
    "http://dldir1.qq.com/INO/assistant/com.google.android.tts.apk"
)

/**
 * 批量下载测试视图
 *
 * 提供完整的批量下载器调用示例和调试入口。
 */
@Composable
fun DebugBatchDownloadView() {
    DebugContent {
        // ========== 初始化 ==========
        DebugTips("🔧 初始化")

        DebugItem("初始化 BatchDownloadManager") { context ->
            BatchDownloadManager.init(context, true)
            ZLog.i(TAG, "BatchDownloadManager 初始化完成")
        }

        // ========== 批量下载 ==========
        DebugTips("📥 批量下载")

        DebugItem("启动批量下载（4个文件，并发3）") { context ->
            startBatchDownload(context, maxConcurrent = 3)
        }

        DebugItem("启动批量下载（4个文件，并发1）") { context ->
            startBatchDownload(context, maxConcurrent = 1)
        }

        DebugItem("启动批量下载（自动重试1次）") { context ->
            startBatchDownload(context, maxConcurrent = 3, maxRetryCount = 1)
        }

        DebugItem("启动批量下载（IMMEDIATE 策略）") { context ->
            startBatchDownloadImmediate(context)
        }

        // ========== 控制操作 ==========
        DebugTips("🎮 控制操作")

        DebugItem("暂停当前批次") {
            currentDownloader?.let { downloader ->
                downloader.pause()
                ZLog.i(TAG, "已暂停批次: ${downloader.batchId}")
            } ?: ZLog.w(TAG, "没有正在进行的批次")
        }

        DebugItem("恢复当前批次") {
            currentDownloader?.let { downloader ->
                downloader.resume()
                ZLog.i(TAG, "已恢复批次: ${downloader.batchId}")
            } ?: ZLog.w(TAG, "没有正在进行的批次")
        }

        DebugItem("重试所有失败任务") {
            currentDownloader?.let { downloader ->
                downloader.resumeFailed()
                ZLog.i(TAG, "已重试失败任务: ${downloader.batchId}")
            } ?: ZLog.w(TAG, "没有正在进行的批次")
        }

        DebugItem("取消当前批次") {
            currentDownloader?.let { downloader ->
                downloader.cancel()
                BatchDownloadManager.remove(downloader.batchId)
                ZLog.i(TAG, "已取消批次: ${downloader.batchId}")
                currentDownloader = null
            } ?: ZLog.w(TAG, "没有正在进行的批次")
        }

        // ========== 状态查询 ==========
        DebugTips("📊 状态查询")

        DebugItem("查询批次状态") {
            currentDownloader?.let { downloader ->
                val statusInfo = downloader.getStatus()
                ZLog.i(TAG, "批次状态: ${statusInfo.status}, 进度: ${statusInfo.progress}%")
                ZLog.i(TAG, "  完成: ${statusInfo.completedCount}/${statusInfo.totalCount}")
                ZLog.i(TAG, "  错误策略: ${statusInfo.errorStrategy}")
                statusInfo.taskStatusList.forEach { task ->
                    ZLog.i(TAG, "  子任务: ${task.url.takeLast(30)}, 状态: ${task.status}, 进度: ${task.progress}%")
                }
            } ?: ZLog.w(TAG, "没有正在进行的批次")
        }

        DebugItem("查询所有子任务") {
            currentDownloader?.let { downloader ->
                val all = downloader.getAll()
                ZLog.i(TAG, "所有子任务 (${all.size} 个):")
                all.forEach { item ->
                    ZLog.i(TAG, "  ${item.downloadURL.takeLast(30)} -> 状态: ${item.status}")
                }
            }
        }

        DebugItem("查询已完成子任务") {
            currentDownloader?.let { downloader ->
                val finished = downloader.getFinished()
                ZLog.i(TAG, "已完成子任务 (${finished.size} 个):")
                finished.forEach { item ->
                    ZLog.i(TAG, "  ${item.downloadURL.takeLast(30)} -> ${item.filePath}")
                }
            }
        }

        DebugItem("查询正在下载子任务") {
            currentDownloader?.let { downloader ->
                val downloading = downloader.getDownloading()
                ZLog.i(TAG, "正在下载子任务 (${downloading.size} 个):")
                downloading.forEach { item ->
                    ZLog.i(TAG, "  ${item.downloadURL.takeLast(30)} -> 进度: ${item.finished}/${item.contentLength}")
                }
            }
        }

        DebugItem("查询所有活跃批次") {
            val batches = BatchDownloadManager.getActiveBatches()
            ZLog.i(TAG, "活跃批次数: ${batches.size}")
            batches.forEach { downloader ->
                val status = downloader.getStatus()
                ZLog.i(TAG, "  ${downloader.batchId}: ${status.status}, 进度=${status.progress}%, ${status.completedCount}/${status.totalCount}")
            }
        }
    }
}

/**
 * 启动批量下载（WAIT_ALL 策略）
 */
private fun startBatchDownload(context: Context, maxConcurrent: Int, maxRetryCount: Int = 0) {
    val config = BatchDownloadConfig(
        fileFolder = AAFFileWrapper.getFileTempFolder(),
        downloadWhenUseMobile = true,
        maxConcurrent = maxConcurrent,
        maxRetryCount = maxRetryCount
    )

    val listener = object : BatchDownloadListener {
        override fun onProgress(batchId: String, progress: Int, completedCount: Int, totalCount: Int, speed: Long) {
            ZLog.i(TAG, "onProgress: 批次=$batchId, 进度=$progress%, 完成=$completedCount/$totalCount, 速度=${speed / 1024}KB/s")
        }

        override fun onComplete(batchId: String, filePaths: Map<String, String>) {
            ZLog.i(TAG, "onComplete: 批次=$batchId, 全部完成！")
            filePaths.forEach { (url, path) ->
                ZLog.i(TAG, "  ${url.takeLast(30)} -> $path")
            }
        }

        override fun onError(batchId: String, failedUrls: Map<String, Int>, completedUrls: List<String>) {
            ZLog.e(TAG, "onError: 批次=$batchId")
            ZLog.e(TAG, "  失败 ${failedUrls.size} 个:")
            failedUrls.forEach { (url, errorCode) ->
                ZLog.e(TAG, "    ${url.takeLast(30)} -> 错误码: $errorCode")
            }
            ZLog.i(TAG, "  成功 ${completedUrls.size} 个")
        }
    }

    val downloader = BatchDownloadManager.create(config, listener)
    downloader.startDownload(context, TEST_URLS)
    currentDownloader = downloader
    ZLog.i(TAG, "批量下载已启动，batchId=${downloader.batchId}, 并发数=$maxConcurrent, 重试=$maxRetryCount")
}

/**
 * 启动批量下载（IMMEDIATE 策略）
 */
private fun startBatchDownloadImmediate(context: Context) {
    val config = BatchDownloadConfig(
        fileFolder = AAFFileWrapper.getFileTempFolder(),
        downloadWhenUseMobile = true,
        maxConcurrent = 3
    )

    val listener = object : BatchDownloadListener {
        override fun onProgress(batchId: String, progress: Int, completedCount: Int, totalCount: Int, speed: Long) {
            ZLog.i(TAG, "onProgress[IMMEDIATE]: 进度=$progress%, 完成=$completedCount/$totalCount")
        }

        override fun onComplete(batchId: String, filePaths: Map<String, String>) {
            ZLog.i(TAG, "onComplete[IMMEDIATE]: 全部完成！共 ${filePaths.size} 个文件")
        }

        override fun onError(batchId: String, failedUrls: Map<String, Int>, completedUrls: List<String>) {
            ZLog.e(TAG, "onError[IMMEDIATE]: 收到失败回调，失败 ${failedUrls.size} 个")
            failedUrls.forEach { (url, errorCode) ->
                ZLog.e(TAG, "  ${url.takeLast(30)} -> 错误码: $errorCode")
            }
        }
    }

    val downloader = BatchDownloadManager.create(config, listener)
    // 启动前设置错误策略为 IMMEDIATE
    downloader.errorStrategy = ErrorStrategy.IMMEDIATE
    downloader.startDownload(context, TEST_URLS)
    currentDownloader = downloader
    ZLog.i(TAG, "批量下载已启动（IMMEDIATE 策略），batchId=${downloader.batchId}")
}
