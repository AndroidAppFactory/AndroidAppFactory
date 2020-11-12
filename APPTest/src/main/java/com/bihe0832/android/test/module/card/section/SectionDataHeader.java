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
@CardInfo(resFileName = "card_demo_section_header.xml", holderCalss = SectionHolderHeader.class)
public class SectionDataHeader extends CardBaseModule {


    public String mHeaderText;

    public SectionDataHeader(String netType) {
        super();
        mHeaderText = netType;
    }

}