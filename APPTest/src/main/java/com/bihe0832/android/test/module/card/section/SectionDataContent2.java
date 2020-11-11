package com.bihe0832.android.test.module.card.section;

import com.bihe0832.android.lib.adapter.CardBaseModule;
import com.bihe0832.android.lib.adapter.CardInfo;
import com.bihe0832.android.test.R;
import com.bihe0832.android.test.module.card.TestSectionAdapter;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
@CardInfo(resId = R.layout.card_demo_section_content_2, holderCalss = SectionHolderContent2.class)
public class SectionDataContent2 extends CardBaseModule {

    public String mContentText;

    public SectionDataContent2(String netType) {
        mContentText = netType;
    }
}