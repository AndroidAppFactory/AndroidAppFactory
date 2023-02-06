package com.bihe0832.android.common.list;

import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author zixie code@bihe0832.com
 * Created on 2020/11/14.
 * Description: Description
 */
public class CardItemForCommonList {

    private Class<? extends CardBaseModule> mCardItemClass = null;
    private Class<? extends CardBaseHolder> mCardHolderClass = null;

    private boolean mCardItemIsHeader = false;

    public CardItemForCommonList(Class<? extends CardBaseModule> itemcalss) {
        this.mCardItemClass = itemcalss;
    }

    public CardItemForCommonList(Class<? extends CardBaseModule> itemcalss, boolean isHeader) {
        this.mCardItemClass = itemcalss;
        this.mCardItemIsHeader = isHeader;
    }

    public CardItemForCommonList(Class<? extends CardBaseModule> itemcalss, Class<? extends CardBaseHolder> mHolderClass, boolean isHeader) {
        this.mCardItemClass = itemcalss;
        this.mCardHolderClass = mHolderClass;
        this.mCardItemIsHeader = isHeader;
    }

    public Class<? extends CardBaseModule> getCardItemClass() {
        return mCardItemClass;
    }

    public Class<? extends CardBaseHolder> getCardHolderClass() {
        return mCardHolderClass;
    }

    public boolean isHeader() {
        return mCardItemIsHeader;
    }
}
