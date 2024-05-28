package com.bihe0832.android.common.settings.card;

import android.view.View;
import com.bihe0832.android.common.about.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2019-11-21.
 *         Description: Description
 */
public class SettingsDataGo extends CardBaseModule {

    public View.OnClickListener mHeaderListener = null;
    public View.OnClickListener mHeaderTipsListener = null;
    public String mItemText = "";
    public String mItemIconURL = "";
    public int mItemIconRes = -1;
    public Integer mItemIconResColorFilter = null;
    public boolean mAutoGenerateColorFilter = true;
    public int mItemNewNum = -1;
    public boolean mShowGo = true;
    public boolean mShowDriver = false;
    public boolean mHeaderTextBold = false;
    public String mTipsText = "";


    public SettingsDataGo(String netType) {
        mItemText = netType;
    }

    @Override
    public int getResID() {
        return R.layout.card_setting_go;
    }

    @Override
    public Class<? extends CardBaseHolder> getViewHolderClass() {
        return SettingsHolderGo.class;
    }

    @Override
    public String toString() {
        return "SettingsData{" +
                "mHeaderListener=" + mHeaderListener +
                ", mHeaderTipsListener=" + mHeaderTipsListener +
                ", mItemText='" + mItemText + '\'' +
                ", mItemIconURL='" + mItemIconURL + '\'' +
                ", mItemIconRes=" + mItemIconRes +
                ", mItemIsNew=" + mItemNewNum +
                ", mShowDriver=" + mShowDriver +
                ", mHeaderTextBold=" + mHeaderTextBold +
                ", mTipsText='" + mTipsText + '\'' +
                '}';
    }
}