package com.bihe0832.android.base.test.card.section;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.bihe0832.android.base.test.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */

public class SectionHolderContent2 extends CardBaseHolder {
    public TextView mHeader;

    public SectionHolderContent2(View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    public void initView() {
        mHeader = getView(R.id.demo_text);
    }

    @Override
    public void initData(CardBaseModule item) {
        SectionDataContent2 data = (SectionDataContent2) item;
        mHeader.setText(data.mContentText);
    }
}
