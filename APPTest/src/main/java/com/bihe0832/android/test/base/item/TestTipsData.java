package com.bihe0832.android.test.base.item;

import android.view.View;

import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.test.R;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
public class TestTipsData extends TestItemData {

    public int getResID() {
        return R.layout.card_test_tips;
    }

    public Class<? extends CardBaseHolder> getViewHolderClass() {
        return TestTipsHolder.class;
    }

    public TestTipsData(String netType) {
        super(netType);
    }

    public TestTipsData(String netType, View.OnClickListener listener) {
        super(netType, listener);
    }
}