package com.bihe0832.android.lib.batch.download

/**
 * 错误处理策略枚举
 *
 * 放在 BatchStatusInfo 中，可在运行时动态修改。
 */
enum class ErrorStrategy {
    /** 等待所有子任务结束后统一回调 onError（默认） */
    WAIT_ALL,
    /** 子任务失败时立即回调 onError */
    IMMEDIATE
}
