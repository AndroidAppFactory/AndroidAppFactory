package com.bihe0832.android.common.test.item;

import android.view.View;

import com.bihe0832.android.common.test.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
public class TestTipsData extends TestItemData {

    public int getResID() {
        return R.layout.com_bihe0832_card_test_tips;
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