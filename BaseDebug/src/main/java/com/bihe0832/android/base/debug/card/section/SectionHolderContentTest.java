package com.bihe0832.android.base.debug.card.section;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.bihe0832.android.base.debug.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */

public class SectionHolderContentTest extends CardBaseHolder {
    public TextView mHeader;

    public SectionHolderContentTest(View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    public void initView() {
        mHeader = getView(R.id.demo_text);
    }

    @Override
    public void initData(CardBaseModule item) {
        SectionDataContentTest data = (SectionDataContentTest) item;
        mHeader.setText(data.mContentText);
        mHeader.setBackgroundColor(Color.BLUE);
    }
}
