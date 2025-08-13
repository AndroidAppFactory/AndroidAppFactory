package com.bihe0832.android.common.debug.log;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.bihe0832.android.common.debug.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2019-11-21.
 *         Description: Description
 */
public class SectionHolderHeader extends CardBaseHolder {

    public TextView mHeader;

    public SectionHolderHeader(View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    public void initView() {
        mHeader = getView(R.id.demo_text);
    }

    @Override
    public void initData(CardBaseModule item) {
        SectionDataHeader data = (SectionDataHeader) item;
        mHeader.setText(data.mHeaderText);
    }


}
