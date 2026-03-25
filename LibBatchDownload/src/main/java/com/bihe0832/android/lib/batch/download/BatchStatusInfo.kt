package com.bihe0832.android.lib.batch.download

/**
 * 批次聚合状态信息
 *
 * @param batchId 批次唯一标识
 * @param status 批次状态
 * @param progress 当前总进度（0~100）
 * @param completedCount 已完成子任务数
 * @param totalCount 子任务总数
 * @param errorStrategy 失败回调策略，默认 WAIT_ALL，可在运行时动态修改
 * @param taskStatusList 各子任务状态快照列表
 */
data class BatchStatusInfo(
    val batchId: String,
    val status: BatchStatus,
    val progress: Int,
    val completedCount: Int,
    val totalCount: Int,
    var errorStrategy: ErrorStrategy = ErrorStrategy.WAIT_ALL,
    val taskStatusList: List<SubTaskStatus>
)
