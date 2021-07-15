package com.bihe0832.android.common.test.log;

import com.bihe0832.android.common.test.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author hardyshi code@bihe0832.com
 *         Created on 2019-11-21.
 *         Description: Description
 */
public class SectionDataHeader extends CardBaseModule {

    public int getResID() {
        return R.layout.com_bihe0832_card_test_log_header;
    }

    public Class<? extends CardBaseHolder> getViewHolderClass() {
        return SectionHolderHeader.class;
    }

    public String mHeaderText;

    public SectionDataHeader(String netType) {
        mHeaderText = netType;
    }

    @Override
    public boolean autoAddItem() {
        return true;
    }
}