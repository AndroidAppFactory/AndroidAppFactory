package com.bihe0832.android.common.debug.item;

import android.view.View;

import com.bihe0832.android.common.debug.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
public class DebugItemData extends CardBaseModule {

    public String mContentText = "";
    public View.OnClickListener mListener = null;
    public View.OnLongClickListener mLongClickListener = null;
    public boolean hasBackground = true;
    public boolean showBottomLine = true;

    public DebugItemData() {
        super();
    }

    public DebugItemData(String content) {
        mContentText = content;
    }

    public DebugItemData(String content, View.OnClickListener listener) {
        this(content, listener, null);
    }

    public DebugItemData(String content, View.OnClickListener listener, boolean showBottomLine) {
        this(content, listener, null, true, showBottomLine);
    }

    public DebugItemData(String content, View.OnClickListener listener, View.OnLongClickListener longClickListener) {
        this(content, listener, longClickListener, true);
    }

    public DebugItemData(String content, View.OnClickListener listener, View.OnLongClickListener longClickListener,
                         boolean background) {
        this(content, listener, longClickListener, background, true);
    }

    public DebugItemData(String content, View.OnClickListener listener, View.OnLongClickListener longClickListener,
                         boolean background,
                         boolean showBottomLine) {
        this.mContentText = content;
        this.mListener = listener;
        this.mLongClickListener = longClickListener;
        this.hasBackground = background;
        this.showBottomLine = showBottomLine;
    }

    public DebugItemData(String content, boolean background) {
        this(content, null, null, background, true);
    }

    public DebugItemData(String content, boolean background, boolean showBottomLine) {
        this(content, null, null, background, showBottomLine);
    }

    public int getResID() {
        return R.layout.com_bihe0832_card_debug_item;
    }

    public Class<? extends CardBaseHolder> getViewHolderClass() {
        return DebugItemHolder.class;
    }

    @Override
    public boolean autoAddItem() {
        return true;
    }
}