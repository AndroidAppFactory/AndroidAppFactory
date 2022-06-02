package com.bihe0832.android.common.debug.item;

import android.view.View;
import com.bihe0832.android.common.debug.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;

/**
 * @author hardyshi code@bihe0832.com
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

    public DebugTipsData(String netType) {
        super(netType);
    }

    public DebugTipsData(String netType, View.OnClickListener listener) {
        super(netType, listener);
    }

    public DebugTipsData(String netType, boolean background) {
        super(netType, background);
    }

    public DebugTipsData(String netType, View.OnClickListener listener, View.OnLongClickListener longClickListener) {
        super(netType, listener, longClickListener);
    }

    public DebugTipsData(String netType, View.OnClickListener listener, View.OnLongClickListener longClickListener,
                         boolean background) {
        super(netType, listener, longClickListener, background);
    }

}