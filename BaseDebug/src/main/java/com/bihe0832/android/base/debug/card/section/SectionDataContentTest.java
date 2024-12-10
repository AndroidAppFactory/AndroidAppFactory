package com.bihe0832.android.base.debug.card.section;

import com.bihe0832.android.base.debug.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
public class SectionDataContentTest extends CardBaseModule {

    public int getResID() {
        return R.layout.card_demo_section_content_2;
    }

    public Class<? extends CardBaseHolder> getViewHolderClass() {
        return SectionHolderContentTest.class;
    }
    public String mContentText;

    public SectionDataContentTest(String netType) {
        mContentText = netType;
    }
}