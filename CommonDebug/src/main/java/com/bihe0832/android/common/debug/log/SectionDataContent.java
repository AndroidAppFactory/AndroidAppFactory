package com.bihe0832.android.common.debug.log;

import com.bihe0832.android.common.debug.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author hardyshi code@bihe0832.com
 *         Created on 2019-11-21.
 *         Description: Description
 */
public class SectionDataContent extends CardBaseModule {

    public int getResID() {
        return R.layout.com_bihe0832_card_debug_content;
    }

    public Class<? extends CardBaseHolder> getViewHolderClass() {
        return SectionHolderContent.class;
    }

    public String mTitleName;
    public String mLogFileName;

    public SectionDataContent(String title, String log) {
        mTitleName = title;
        mLogFileName = log;
    }

    @Override
    public boolean autoAddItem() {
        return true;
    }
}