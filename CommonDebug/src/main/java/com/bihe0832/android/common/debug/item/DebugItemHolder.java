package com.bihe0832.android.common.debug.item;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.bihe0832.android.common.debug.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;
import com.bihe0832.android.lib.theme.ThemeResourcesManager;
import com.bihe0832.android.lib.text.TextFactoryUtils;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */

public class DebugItemHolder extends CardBaseHolder {

    public TextView mHeader;
    public View mBottomLine;

    public DebugItemHolder(View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    public void initView() {
        mHeader = getView(R.id.bad_card_title);
        mBottomLine = getView(R.id.test_bottom_line);
    }

    @Override
    public void initData(CardBaseModule item) {
        DebugItemData data = (DebugItemData) item;
        mHeader.setText(TextFactoryUtils.getSpannedTextByHtml(data.mContentText));
        if (null != data.mListener) {
            itemView.setOnClickListener(data.mListener);
        }
        if (null != data.mLongClickListener) {
            itemView.setOnLongClickListener(data.mLongClickListener);
        }
        if (!data.hasBackground) {
            ((View) mHeader.getParent()).setBackgroundColor(ThemeResourcesManager.INSTANCE.getColor(R.color.transparent));
        }

        if (data.showBottomLine && null != mBottomLine) {
            mBottomLine.setVisibility(View.VISIBLE);
        } else {
            mBottomLine.setVisibility(View.GONE);
        }
    }
}
