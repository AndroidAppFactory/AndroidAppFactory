package com.bihe0832.android.lib.download;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 下载暂停类型，用于区分不同的暂停原因和恢复策略
 *
 * @author zixie code@bihe0832.com Created on 2020-06-08.
 */

@Retention(RetentionPolicy.SOURCE)
public @interface DownloadPauseType {

    // 因切换到移动网络而暂停（仅WiFi任务），切回WiFi后自动恢复
    int PAUSED_BY_MOBILE_NETWORK = 1;
    // 用户主动暂停，只能手动恢复
    int PAUSED_BY_USER = 2;
    // 批量暂停，resumeAll时自动恢复
    int PAUSED_BY_ALL = 3;
    // 添加时未启动，需手动启动
    int PAUSED_PENDING_START = 4;
    // 网络异常暂停（断网/超时等可恢复错误），网络恢复自动重试
    int PAUSED_BY_NETWORK_ERROR = 5;

}
