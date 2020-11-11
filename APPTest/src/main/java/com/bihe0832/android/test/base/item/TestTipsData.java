package com.bihe0832.android.test.base.item;

import android.view.View;

import com.bihe0832.android.lib.adapter.CardInfo;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
@CardInfo(resFileName = "card_test_tips", holderCalss = TestTipsHolder.class)
public class TestTipsData extends TestItemData {

    public TestTipsData(String netType) {
        super(netType);
    }

    public TestTipsData(String netType, View.OnClickListener listener) {
        super(netType, listener);
    }
}