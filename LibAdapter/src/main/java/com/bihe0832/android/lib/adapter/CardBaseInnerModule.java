package com.bihe0832.android.lib.adapter;


import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by zixie on 2017/12/7.
 * <p>
 * 所有卡片对应的数据都继承自该类
 */

public abstract class CardBaseInnerModule implements MultiItemEntity {

    public abstract int getResID();

    public abstract Class<? extends CardBaseHolder> getViewHolderClass();

    public String getTypeBadText() {
        return "暂不支持的数据格式";
    }

}
