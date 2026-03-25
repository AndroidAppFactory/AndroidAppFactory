package com.bihe0832.android.lib.batch.download

/**
 * 批量下载配置
 *
 * 创建批次时指定，不可在运行时修改。
 *
 * @param fileFolder 下载目录
 * @param downloadWhenUseMobile 是否允许移动网络下载，默认 false
 * @param maxConcurrent 批次层面的最大并发数，默认 3，范围 1~5
 * @param maxRetryCount 子任务自动重试次数，默认 0（不自动重试），范围 0~3
 */
data class BatchDownloadConfig(
    val fileFolder: String,
    val downloadWhenUseMobile: Boolean = false,
    val maxConcurrent: Int = 3,
    val maxRetryCount: Int = 0
)
