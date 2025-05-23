package com.bihe0832.android.common.file.preview;

import android.graphics.Color;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author zixie code@bihe0832.com Created on 2019-11-21. Description: Description
 */
public class ContentItemData extends CardBaseModule {

    public static final int DEFAULT_TEXT_SIZE_DP = 12;
    public static final int DEFAULT_PADDING_SIZE_DP = 16;

    public String mContentText = "";
    public View.OnClickListener mListener = null;
    public View.OnLongClickListener mLongClickListener = null;
    public int backgroundColor = Color.WHITE;
    public int textColor = Color.BLACK;
    public boolean isSingleLine = true;
    public TextUtils.TruncateAt ellipsize = TruncateAt.END;
    public boolean isBold = false;
    public int bottomLineColor = Color.WHITE;
    public int textSizeDP = DEFAULT_TEXT_SIZE_DP;
    public int paddingTopDp = DEFAULT_PADDING_SIZE_DP;
    public int paddingLeftDp = DEFAULT_PADDING_SIZE_DP;

    public ContentItemData() {
        super();
    }

    public ContentItemData(String content, View.OnClickListener listener, View.OnLongClickListener longClickListener,
            int textSizeDP, int textColor, boolean isBold, boolean isSingleLine, TextUtils.TruncateAt ellipsize,
            int paddingTopDp, int paddingLeftDp, int background,
            int showBottomLine) {
        this.mContentText = content;
        this.mListener = listener;
        this.mLongClickListener = longClickListener;
        this.textSizeDP = textSizeDP;
        this.textColor = textColor;
        this.isSingleLine = isSingleLine;
        this.ellipsize = ellipsize;
        this.isBold = isBold;
        this.paddingTopDp = paddingTopDp;
        this.paddingLeftDp = paddingLeftDp;
        this.backgroundColor = background;
        this.bottomLineColor = showBottomLine;
    }


    public int getResID() {
        return R.layout.com_bihe0832_card_content_item;
    }

    public Class<? extends CardBaseHolder> getViewHolderClass() {
        return ContentItemHolder.class;
    }

    @Override
    public boolean autoAddItem() {
        return true;
    }
}