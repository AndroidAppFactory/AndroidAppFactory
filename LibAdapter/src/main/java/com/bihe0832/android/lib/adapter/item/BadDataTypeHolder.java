package com.bihe0832.android.lib.adapter.item;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;
import com.bihe0832.android.lib.adapter.R;
import com.bihe0832.android.lib.text.TextFactoryUtils;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2019-11-21.
 *         Description: Description
 */

public class BadDataTypeHolder extends CardBaseHolder {

    public TextView mHeader;

    public BadDataTypeHolder(View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    public void initView() {
        mHeader = getView(R.id.bad_card_title);
    }

    @Override
    public void initData(CardBaseModule item) {
        mHeader.setText(TextFactoryUtils.getSpannedTextByHtml(item.getTypeBadText()));
    }
}
