package com.bihe0832.android.base.debug.card.section;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */

public class SectionHolderContent3 extends SectionHolderContentTest {

    public SectionHolderContent3(View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    public void initData(CardBaseModule item) {
        super.initData(item);
        mHeader.setBackgroundColor(Color.GREEN);
    }
}
