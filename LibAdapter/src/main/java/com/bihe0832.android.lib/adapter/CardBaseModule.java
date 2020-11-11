package com.bihe0832.android.lib.adapter;


import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by zixie on 2017/12/7.
 * <p>
 * 所有卡片对应的数据都继承自该类
 */

public class CardBaseModule implements MultiItemEntity {

    protected CardBaseModule() {
        CardInfo getAnnotation = this.getClass().getAnnotation(CardInfo.class);
        if (null == getAnnotation) {
            throw new NullPointerException();
        }
    }

    final public int getItemType() {
        CardInfo getAnnotation = this.getClass().getAnnotation(CardInfo.class);
        return getAnnotation.resId();
    }
}
