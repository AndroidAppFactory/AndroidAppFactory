package com.bihe0832.android.lib.download;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 下载失败后的错误码
 *
 * @author zixie code@bihe0832.com Created on 2020-06-08.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface DownloadErrorCode {

    // 成功
    int SUCC = 0;
    // 已经下载
    int HAS_DOWMLOAD = 1;
    // 下载地址不合法
    int ERR_BAD_URL = -1;
    // 下载的版本低于已经安装的版本
    int ERR_URL_IS_TOO_OLD_THAN_LOACL = -2;
    // 下载的版本低于已经在下载的版本
    int ERR_URL_IS_TOO_OLD_THAN_DOWNLOADING = -3;
    //  下载队列已满
    int ERR_NEED_WAITING = -4;
    // 下载时创建HTTP请求异常
    int ERR_HTTP_FAILED = -5;
    int ERR_HTTP_LENGTH_FAILED = -10;
    // HTTP 请求成功，但是返回长度错误
    // 下载分片时HTTP 异常
    int ERR_HTTP_EXCEPTION = -11;
    // 下载时异常
    int ERR_DOWNLOAD_EXCEPTION = -6;
    // 文件MD5不一致
    int ERR_MD5_BAD = -7;
    // 重命名文件失败（下载过程使用临时文件）
    int ERR_FILE_RENAME_FAILED = -8;
    // 回调业务时异常
    int ERR_NOTIFY_EXCEPTION = -9;
    // 下分片载时异常
    int ERR_DOWNLOAD_PART_EXCEPTION = -12;
    // 启动分片下载时异常
    int ERR_DOWNLOAD_PART_START_EXCEPTION = -13;
    // 分片下载必须提供文件完整路径
    int ERR_RANGE_BAD_PATH = -14;

    int ERR_RANGE_NOT_SUPPORT = -15;

    int ERR_RANGE_BAD_SERVER_LENGTH = -16;

    int ERR_RANGE_BAD_DOWNLOAD_LENGTH = -17;
}
