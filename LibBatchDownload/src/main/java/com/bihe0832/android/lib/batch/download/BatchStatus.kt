package com.bihe0832.android.lib.batch.download

/**
 * 批次状态枚举
 */
enum class BatchStatus {
    /** 运行中 */
    RUNNING,
    /** 已暂停 */
    PAUSED,
    /** 全部完成 */
    COMPLETED,
    /** 存在失败 */
    FAILED,
    /** 已取消 */
    CANCELLED
}
