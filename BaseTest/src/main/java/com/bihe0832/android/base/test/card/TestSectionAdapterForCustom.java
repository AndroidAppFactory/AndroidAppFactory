package com.bihe0832.android.base.test.card;

import android.content.Context;

import com.bihe0832.android.base.test.card.section.SectionDataHeader;
import com.bihe0832.android.base.test.card.section.SectionDataHeader2;
import com.bihe0832.android.common.test.item.TestTipsData;
import com.bihe0832.android.lib.adapter.CardBaseAdapter;

import java.util.List;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-11-21.
 * <p>
 * Description: Description
 */
public class TestSectionAdapterForCustom extends CardBaseAdapter {

    public TestSectionAdapterForCustom(Context context, List data) {
        super(context, data);
        addItemToAdapter(TestTipsData.class, true);
        addItemToAdapter(SectionDataHeader.class, true);
        addItemToAdapter(SectionDataHeader2.class, true);
    }
}