package com.bihe0832.android.lib.batch.download

import com.bihe0832.android.lib.download.DownloadStatus

/**
 * 子任务状态快照
 *
 * @param url 下载 URL
 * @param downloadId 底层下载任务 ID
 * @param status 下载状态（复用 DownloadStatus 常量）
 * @param progress 子任务进度（0~100）
 * @param filePath 下载完成后的文件路径
 * @param errorCode 失败错误码（0 表示无错误）
 */
data class SubTaskStatus(
    val url: String,
    val downloadId: Long,
    @DownloadStatus val status: Int,
    val progress: Int,
    val filePath: String,
    val errorCode: Int
)
