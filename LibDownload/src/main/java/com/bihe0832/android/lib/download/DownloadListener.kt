package com.bihe0832.android.lib.download

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-09.
 * Description: 下载状态回调
 *
 */
interface DownloadListener {

    /**
     * 任务进入等待状态通知，在对任务进行重新下载、恢复下载时回调
     * 根据需要更新任务的UI状态显示，如:任务状态显示为”等待中“
     */
    fun onWait(item: DownloadItem)

    /**
     * 任务开始运行，在任务从等待队列进入执行线程池运行时回调
     * 根据需要更新任务的UI状态显示，如:任务状态显示为”下载中“
     */
    fun onStart(item: DownloadItem)

    /**
     * 任务下载进度通知，在任务获取一段数据时回调，一般每个任务每秒回调一次
     * 根据需要更新任务的UI状态显示，如:更新下载进度、下载速度
     */
    fun onProgress(item: DownloadItem)

    /**
     * 任务已暂停通知，只在主线程调用，用于更新UI，对于等待中和下载中的任务，调用pause() 接口时，都会在主线程回调此暂停通知
     * 注:只在主线程回调，根据需要更新任务的UI状态显示，如:任务状态显示为“已暂停”
     */
    fun onPause(item: DownloadItem)

    /**
     * 任务下载失败通知，在任务下载失败后回调
     * 根据需要更新任务的UI状态显示，如:任务状态显示为”已失败“
     */
    fun onFail(errorCode: Int, msg: String, item: DownloadItem)

    /**任务下载完成通知，在任务保存完全部数据时回调。
     * 根据需要更新任务的UI状态显示，如:任务状态显示为”已完成“
     */
    fun onComplete(filePath: String, item: DownloadItem): String

    fun onDelete(item: DownloadItem)
}
