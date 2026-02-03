package com.bihe0832.android.lib.download.wrapper

import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadPauseType
import com.bihe0832.android.lib.log.ZLog

/**
 * 简化的下载监听器抽象类
 *
 * 实现了 DownloadListener 接口的部分方法，提供默认实现（仅打印日志）。
 * 使用者只需重写关心的方法（通常是 onComplete 和 onFail），无需实现所有回调。
 *
 * 默认实现的方法：
 * - onWait: 等待下载时的回调（默认打印日志）
 * - onStart: 开始下载时的回调（默认打印日志）
 * - onPause: 暂停下载时的回调（默认打印日志）
 * - onDelete: 删除任务时的回调（默认打印日志）
 *
 * 需要重写的方法：
 * - onComplete: 下载完成的回调（必须实现）
 * - onFail: 下载失败的回调（必须实现）
 * - onProgress: 下载进度的回调（必须实现）
 *
 * 使用场景：
 * 1. 只关心下载成功/失败结果，不关心中间状态
 * 2. 简化监听器实现，减少样板代码
 * 3. 快速实现下载功能，无需处理所有回调
 *
 * @author zixie code@bihe0832.com
 * Created on 2021/2/1.
 * Description: 简化的下载监听器，提供默认实现减少样板代码
 */
abstract class SimpleDownloadListener : DownloadListener {
    override fun onWait(item: DownloadItem) {
        ZLog.d(DownloadItem.TAG, "onWait $item")
    }

    override fun onStart(item: DownloadItem) {
        ZLog.d(DownloadItem.TAG, "onStart $item")
    }

    override fun onPause(item: DownloadItem, @DownloadPauseType pauseType: Int) {
        ZLog.d(DownloadItem.TAG, "onPause pauseType=$pauseType $item")
    }

    override fun onDelete(item: DownloadItem) {
        ZLog.d(DownloadItem.TAG, "onDelete $item")
    }
}