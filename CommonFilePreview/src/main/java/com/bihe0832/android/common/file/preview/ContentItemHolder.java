package com.bihe0832.android.common.file.preview;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;
import com.bihe0832.android.lib.text.TextFactoryUtils;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

/**
 * @author zixie code@bihe0832.com Created on 2019-11-21. Description: Description
 */

public class ContentItemHolder extends CardBaseHolder {

    public TextView mHeader;
    public View mBottomLine;

    public ContentItemHolder(View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    public void initView() {
        mHeader = getView(R.id.bad_card_title);
        mBottomLine = getView(R.id.test_bottom_line);
    }

    @Override
    public void initData(CardBaseModule item) {
        ContentItemData data = (ContentItemData) item;
        mHeader.setText(TextFactoryUtils.getSpannedTextByHtml(data.mContentText));
        mHeader.setTextSize(TypedValue.COMPLEX_UNIT_DIP, data.textSizeDP);
        mHeader.setTextColor(data.textColor);
        mBottomLine.setBackgroundColor(data.bottomLineColor);
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
        mHeader.setPadding(DisplayUtil.dip2px(getContext(), data.paddingLeftDp),
                DisplayUtil.dip2px(getContext(), data.paddingTopDp),
                DisplayUtil.dip2px(getContext(), data.paddingLeftDp),
                DisplayUtil.dip2px(getContext(), data.paddingTopDp));
        if (null != data.mListener) {
            itemView.setOnClickListener(data.mListener);
        }
        itemView.setLongClickable(false);

        if (null != data.mLongClickListener) {
            itemView.setOnLongClickListener(data.mLongClickListener);
        }
        ((View) mHeader.getParent()).setBackgroundColor(data.backgroundColor);
    }
}
