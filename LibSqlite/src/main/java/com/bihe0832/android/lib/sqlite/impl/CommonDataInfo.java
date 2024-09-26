package com.bihe0832.android.lib.sqlite.impl;

/**
 * @author zixie code@bihe0832.com
 * Created on 2021/2/20.
 * Description: Description
 */
public class CommonDataInfo {
    //下载ts的基础URL
    public String key = "";
    //下载ts的基础URL
    public String value = "";
    //创建时间
    public long createTime = 0;
    //更新时间
    public long updateTime = 0;


    @Override
    public String toString() {
        return "CommonDataInfo{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
