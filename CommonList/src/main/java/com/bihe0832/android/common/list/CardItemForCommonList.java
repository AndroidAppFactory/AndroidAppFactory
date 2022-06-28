package com.bihe0832.android.common.list;

import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author zixie code@bihe0832.com
 * Created on 2020/11/14.
 * Description: Description
 */
public class CardItemForCommonList {

    private Class<? extends CardBaseModule> mCardItemClass = null;
    private boolean mCardItemIsHeader = false;

    public CardItemForCommonList(Class<? extends CardBaseModule> itemcalss) {
        this.mCardItemClass = itemcalss;
    }

    public CardItemForCommonList(Class<? extends CardBaseModule> itemcalss, boolean isHeader) {
        this.mCardItemClass = itemcalss;
        this.mCardItemIsHeader = isHeader;
    }

    public Class<? extends CardBaseModule> getmCardItemClass() {
        return mCardItemClass;
    }

    public boolean isHeader() {
        return mCardItemIsHeader;
    }
}
