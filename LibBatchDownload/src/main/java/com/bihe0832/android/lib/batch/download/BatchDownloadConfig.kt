package com.bihe0832.android.lib.batch.download

import com.bihe0832.android.lib.download.DownloadClientConfig

/**
 * 批量下载配置
 *
 * 创建批次时指定，不可在运行时修改。
 *
 * @param fileFolder 下载目录
 * @param downloadWhenUseMobile 是否允许移动网络下载，默认 false
 * @param maxConcurrent 批次层面的最大并发数，默认 3，范围 1~5
 * @param maxRetryCount 子任务自动重试次数，默认 0（不自动重试），范围 0~3
 * @param progressIntervalMs 进度回调最小间隔（毫秒），默认 200ms，用于节流避免过度回调
 * @param batchRetryMax 批次级重试次数：当所有失败子任务都是网络型错误时，延迟后自动重试整个批次，默认 0（不启用）。启用后会在 onError 前自动重试，每次间隔递增
 * @param batchRetryDelayMs 批次重试间隔基数（毫秒），每次重试间隔 = delay × 第N次，默认 20000ms
 * @param downloadClientConfig 底层下载客户端配置（HTTP/2、分片策略等），默认使用 createDefault()
 */
data class BatchDownloadConfig(
    val fileFolder: String,
    val downloadWhenUseMobile: Boolean = false,
    val maxConcurrent: Int = 3,
    val maxRetryCount: Int = 0,
    val progressIntervalMs: Long = 200L,
    val batchRetryMax: Int = 0,
    val batchRetryDelayMs: Long = 20_000L,
    val downloadClientConfig: DownloadClientConfig = DownloadClientConfig.createDefault()
)
