package com.bihe0832.android.base.debug.card.section;

import com.bihe0832.android.base.debug.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
public class SectionDataHeader extends CardBaseModule {

    public int getResID() {
        return R.layout.card_demo_section_header;
    }

    public Class<? extends CardBaseHolder> getViewHolderClass() {
        return SectionHolderHeader.class;
    }

    public String mHeaderText;

    public SectionDataHeader(String netType) {
        mHeaderText = netType;
    }

}