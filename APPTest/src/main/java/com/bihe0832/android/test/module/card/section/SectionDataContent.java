package com.bihe0832.android.test.module.card.section;

import com.bihe0832.android.lib.adapter.CardBaseModule;
import com.bihe0832.android.lib.adapter.CardInfo;
import com.bihe0832.android.test.R;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
@CardInfo(id = R.layout.card_demo_section_content, hoderCalss = SectionHolderContent.class)
public class SectionDataContent extends CardBaseModule {

    public String mContentText;

    public SectionDataContent(String netType) {
        mContentText = netType;
    }
}