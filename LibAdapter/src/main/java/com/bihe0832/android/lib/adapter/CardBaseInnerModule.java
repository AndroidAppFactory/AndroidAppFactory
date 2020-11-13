package com.bihe0832.android.lib.adapter;


import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by zixie on 2017/12/7.
 * <p>
 * 所有卡片对应的数据都继承自该类
 */

public abstract class CardBaseInnerModule implements MultiItemEntity {

    public CardBaseInnerModule() {
        if (CardInfoHelper.getInstance().autoAddItem() || autoAddItem()) {
            CardInfoHelper.getInstance().addCardItem(getResID(), getViewHolderClass());
        }
    }

    public boolean autoAddItem() {
        return false;
    }

    public abstract int getResID();

    public abstract Class<? extends CardBaseHolder> getViewHolderClass();

    public String getTypeBadText() {
        return "当前版本不支持该样式，请更新版本";
    }

}
