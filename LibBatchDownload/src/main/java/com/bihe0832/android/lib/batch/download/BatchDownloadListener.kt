package com.bihe0832.android.lib.batch.download

/**
 * 批量下载回调接口
 *
 * 与底层 DownloadListener 风格对齐，提供批次级别的回调：
 * - onProgress: 批次总进度回调（节流：仅整数百分比变化时回调）
 * - onComplete: 批次全部下载完成回调
 * - onError: 批次下载失败回调（时机取决于 ErrorStrategy）
 *
 * @author zixie code@bihe0832.com
 */
interface BatchDownloadListener {

    /**
     * 批次下载进度回调
     *
     * @param batchId 批次唯一标识
     * @param progress 总进度（0~100）
     * @param completedCount 已完成子任务数
     * @param totalCount 子任务总数
     * @param speed 汇总下载速度（字节/秒）
     */
    fun onProgress(batchId: String, progress: Int, completedCount: Int, totalCount: Int, speed: Long)

    /**
     * 批次全部下载完成回调
     *
     * @param batchId 批次唯一标识
     * @param filePaths URL → 本地文件路径的映射
     */
    fun onComplete(batchId: String, filePaths: Map<String, String>)

    /**
     * 批次下载失败回调
     *
     * 回调时机取决于 BatchStatusInfo.errorStrategy：
     * - WAIT_ALL：所有子任务结束后统一回调一次
     * - IMMEDIATE：每个子任务失败时立即回调
     *
     * @param batchId 批次唯一标识
     * @param failedUrls 失败的 URL → 错误码映射
     * @param completedUrls 已成功完成的 URL 列表
     */
    fun onError(batchId: String, failedUrls: Map<String, Int>, completedUrls: List<String>)
}
