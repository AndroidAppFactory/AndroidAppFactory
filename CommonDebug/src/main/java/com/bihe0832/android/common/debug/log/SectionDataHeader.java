package com.bihe0832.android.common.debug.log;

import com.bihe0832.android.common.debug.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2019-11-21.
 *         Description: Description
 */
public class SectionDataHeader extends CardBaseModule {

    public int getResID() {
        return R.layout.com_bihe0832_card_debug_header;
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