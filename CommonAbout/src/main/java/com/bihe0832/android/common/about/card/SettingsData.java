package com.bihe0832.android.common.about.card;

import android.view.View;

import com.bihe0832.android.common.about.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
public class SettingsData extends CardBaseModule {

    public View.OnClickListener mHeaderListener = null;
    public View.OnClickListener mHeaderTipsListener = null;
    public String mItemText = "";
    public String mItemIconURL = "";
    public int mItemIconRes = -1;
    public boolean mItemIsNew = false;
    public boolean mShowGo = true;
    public boolean mShowDriver = false;
    public boolean mHeaderTextBold = false;
    public String mTipsText = "";


    public SettingsData(String netType) {
        mItemText = netType;
    }

    @Override
    public int getResID() {
        return R.layout.card_setting;
    }

    @Override
    public Class<? extends CardBaseHolder> getViewHolderClass() {
        return SettingsHolder.class;
    }

    @Override
    public String toString() {
        return "SettingsData{" +
                "mHeaderListener=" + mHeaderListener +
                ", mHeaderTipsListener=" + mHeaderTipsListener +
                ", mItemText='" + mItemText + '\'' +
                ", mItemIconURL='" + mItemIconURL + '\'' +
                ", mItemIconRes=" + mItemIconRes +
                ", mItemIsNew=" + mItemIsNew +
                ", mShowDriver=" + mShowDriver +
                ", mHeaderTextBold=" + mHeaderTextBold +
                ", mTipsText='" + mTipsText + '\'' +
                '}';
    }
}