package com.bihe0832.android.lib.download;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 各种下载状态全记录，主要用于下载状态回调
 *
 * @author zixie code@bihe0832.com Created on 2020-06-08.
 */

@Retention(RetentionPolicy.SOURCE)
public @interface DownloadPauseType {

    //网络切变暂停，可自动恢复
    int PAUSED_BY_NETWORK = 1;
    //主动暂停，只能主动恢复
    int PAUSED_BY_USER = 2;
    //批量暂停，可自动恢复
    int PAUSED_BY_ALL = 3;
    //批量暂停，添加时自动暂停
    int PAUSED_BY_ADD = 4;

}
