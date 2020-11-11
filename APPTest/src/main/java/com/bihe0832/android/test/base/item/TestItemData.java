package com.bihe0832.android.test.base.item;

import android.view.View;

import com.bihe0832.android.lib.adapter.CardBaseModule;
import com.bihe0832.android.lib.adapter.CardInfo;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
@CardInfo(resFileName = "card_test_item", holderCalss = TestItemHolder.class)
public class TestItemData extends CardBaseModule {

    public String mContentText = "";
    public View.OnClickListener mListener = null;

    public TestItemData(String netType) {
        mContentText = netType;
    }

    public TestItemData(String netType, View.OnClickListener listener) {
        mContentText = netType;
        mListener = listener;
    }
}