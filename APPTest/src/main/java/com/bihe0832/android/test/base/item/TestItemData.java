package com.bihe0832.android.test.base.item;

import android.view.View;

import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;
import com.bihe0832.android.test.R;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
public class TestItemData extends CardBaseModule {

    public TestItemData(String netType) {
        super("");
        mContentText = netType;
    }

    public TestItemData(String netType, View.OnClickListener listener) {
        super("");
        mContentText = netType;
        mListener = listener;
    }

    public int getResID() {
        return R.layout.card_test_item;
    }

    public Class<? extends CardBaseHolder> getViewHolderClass() {
        return TestItemHolder.class;
    }

    public String mContentText = "";
    public View.OnClickListener mListener = null;


}