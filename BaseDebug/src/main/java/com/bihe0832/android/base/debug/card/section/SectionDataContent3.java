package com.bihe0832.android.base.debug.card.section;

import com.bihe0832.android.lib.adapter.CardBaseHolder;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
public class SectionDataContent3 extends SectionDataContentTest {

    public SectionDataContent3(String netType) {
        super(netType);
    }

    public Class<? extends CardBaseHolder> getViewHolderClass() {
        return SectionHolderContent3.class;
    }
}