package com.bihe0832.android.test.module.card;

import android.content.Context;

import com.bihe0832.android.lib.adapter.CardBaseAdapter;
import com.bihe0832.android.test.module.card.section.SectionDataContent;
import com.bihe0832.android.test.module.card.section.SectionDataContent2;
import com.bihe0832.android.test.module.card.section.SectionDataHeader;
import com.bihe0832.android.test.module.card.section.SectionDataHeader2;

import java.util.List;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-11-21.
 * <p>
 * Description: Description
 */
public class TestSectionAdapter extends CardBaseAdapter {

    public TestSectionAdapter(Context context, List data) {
        super(context, data);
        addItemToAdapter(SectionDataHeader.class, true);
        addItemToAdapter(SectionDataHeader2.class, true);
        addItemToAdapter(SectionDataContent.class, false);
        addItemToAdapter(SectionDataContent2.class,false);
    }
}