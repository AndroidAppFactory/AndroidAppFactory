package com.bihe0832.android.common.debug.item;

import android.view.View;

import com.bihe0832.android.common.debug.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2019-11-21.
 *         Description: Description
 */
public class DebugTipsData extends DebugItemData {

    public int getResID() {
        return R.layout.com_bihe0832_card_debug_tips;
    }

    public Class<? extends CardBaseHolder> getViewHolderClass() {
        return DebugTipsHolder.class;
    }

    public DebugTipsData(String content) {
        super(content);
    }

    public DebugTipsData(String content, View.OnClickListener listener) {
        super(content, listener);
    }

    public DebugTipsData(String content, boolean background) {
        super(content, background);
    }

    public DebugTipsData(String content, boolean background, boolean showBottomLine) {
        super(content, background, showBottomLine);
    }

    public DebugTipsData(String content, View.OnClickListener listener, View.OnLongClickListener longClickListener) {
        super(content, listener, longClickListener);
    }

    public DebugTipsData(String content, View.OnClickListener listener, View.OnLongClickListener longClickListener, boolean background) {
        super(content, listener, longClickListener, background);
    }

    public DebugTipsData(String content, View.OnClickListener listener, View.OnLongClickListener longClickListener, boolean background, boolean showBottomLine) {
        super(content, listener, longClickListener, background, showBottomLine);
    }

}