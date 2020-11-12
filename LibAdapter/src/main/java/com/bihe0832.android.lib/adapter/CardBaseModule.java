package com.bihe0832.android.lib.adapter;


import android.content.Context;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by zixie on 2017/12/7.
 * <p>
 * 所有卡片对应的数据都继承自该类
 */

public class CardBaseModule implements MultiItemEntity {
    private String typebadText = "暂不支持的数据格式";

    public void setTypeBadText(String typebadText) {
        this.typebadText = typebadText;
    }

    public String getTypebadText() {
        return typebadText;
    }

    protected CardBaseModule() {
        CardInfo getAnnotation = this.getClass().getAnnotation(CardInfo.class);
        if (null == getAnnotation) {
            throw new NullPointerException();
        }
    }

    final public int getItemType(Context context) {
        try {
            return CardInfoHelper.getInstance().getResIdByCardInfo(context, this.getClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
