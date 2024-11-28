package com.bihe0832.android.common.debug.item;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import com.bihe0832.android.common.debug.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;
import com.bihe0832.android.lib.text.TextFactoryUtils;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

/**
 * @author zixie code@bihe0832.com Created on 2019-11-21. Description: Description
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
        mHeader.setTextSize(TypedValue.COMPLEX_UNIT_DIP, data.textSizeDP);
        mHeader.setTextColor(data.textColor);
        if (data.isBold) {
            mHeader.setTypeface(null, Typeface.BOLD);
        } else {
            mHeader.setTypeface(null, Typeface.NORMAL);
        }
        if (data.isSingleLine) {
            mHeader.setSingleLine(true);
            mHeader.setEllipsize(data.ellipsize);
        } else {
            mHeader.setSingleLine(false);
            mHeader.setEllipsize(null);
        }
        mHeader.setPadding(DisplayUtil.dip2px(getContext(), 16), DisplayUtil.dip2px(getContext(), data.paddingDp),
                DisplayUtil.dip2px(getContext(), 16), DisplayUtil.dip2px(getContext(), data.paddingDp));
        if (null != data.mListener) {
            itemView.setOnClickListener(data.mListener);
        }
        if (null != data.mLongClickListener) {
            itemView.setOnLongClickListener(data.mLongClickListener);
        }
        ((View) mHeader.getParent()).setBackgroundColor(data.backgroundColor);

        if (data.showBottomLine && null != mBottomLine) {
            mBottomLine.setVisibility(View.VISIBLE);
        } else {
            mBottomLine.setVisibility(View.GONE);
        }
    }
}
