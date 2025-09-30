package com.bihe0832.android.lib.download;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 各种下载状态全记录，主要用于下载状态回调
 *
 * @author zixie code@bihe0832.com Created on 2020-06-08.
 */

@Retention(RetentionPolicy.SOURCE)
public @interface DownloadStatus {

    //尚未下载
    int NO_DOWNLOAD = 0;
    //队列中，等待下载
    int STATUS_DOWNLOAD_WAITING = 1;
    //开始下载，获取到下载信息
    int STATUS_DOWNLOAD_STARTED = 2;
    //正在下载
    int STATUS_DOWNLOADING = 3;
    //已经下载，处理方式与下载成功一致
    int STATUS_HAS_DOWNLOAD = 4;
    //下载成功
    int STATUS_DOWNLOAD_SUCCEED = 5;
    //暂停
    int STATUS_DOWNLOAD_PAUSED = 7;
    //下载失败，失败原因在回调时会回调
    int STATUS_DOWNLOAD_FAILED = 8;
    // 删除任务
    int STATUS_DOWNLOAD_DELETE = 9;

}
