package com.bihe0832.android.base.debug.card;

import android.content.Context;

import com.bihe0832.android.base.debug.card.section.SectionDataHeader2;
import com.bihe0832.android.lib.adapter.CardBaseAdapter;

import java.util.List;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-21.
 * <p>
 * Description: Description
 */
public class DebugSectionAdapterForCustom extends CardBaseAdapter {

    public DebugSectionAdapterForCustom(Context context, List data) {
        super(context, data);
        addItemToAdapter(SectionDataHeader2.class, true);
    }
}