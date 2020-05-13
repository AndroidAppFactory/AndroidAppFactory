package com.bihe0832.lib.android.adapter;


import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by hardyshi on 2017/12/7.
 *
 * 所有卡片对应的数据都继承自该类
 */

public abstract class CardBaseModule implements MultiItemEntity {

    @Override
    public abstract int getItemType();
}
