package com.bihe0832.android.lib.download;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * DownloadAction
 *
 * @author zixie code@bihe0832.com Created on 2020/7/6.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface DownloadAction {

    //添加下载任务下载
    int ADD = 1;
    //下载检查
    int CHECK = 2;
    //下载准备
    int PREPARE = 3;
    //开始下载
    int START = 4;
    //下载重试
    int RETRY = 5;
    //下载暂停
    int PAUSE = 6;
    //下载继续
    int RESUME = 7;
    //下载完成
    int FINISH = 8;
    //安装
    int INSTALL = 9;
    //打开
    int OPEN = 10;
    //删除
    int DELETE = 11;
    // 如果业务自定义action 取100 以上
    int ERROR_APP = 100;
}
