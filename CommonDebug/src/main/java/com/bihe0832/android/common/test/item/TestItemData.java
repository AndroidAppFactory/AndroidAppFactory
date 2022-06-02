package com.bihe0832.android.common.test.item;

import android.view.View;

import com.bihe0832.android.common.test.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-11-21.
 * Description: Description
 */
public class TestItemData extends CardBaseModule {

    public String mContentText = "";
    public View.OnClickListener mListener = null;
    public View.OnLongClickListener mLongClickListener = null;
    public boolean hasBackground = true;
    public boolean showBottomLine = true;

    public TestItemData() {
        super();
    }

    public TestItemData(String netType) {
        mContentText = netType;
    }

    public TestItemData(String netType, View.OnClickListener listener) {
        this(netType, listener, null);
    }

    public TestItemData(String netType, View.OnClickListener listener, boolean showBottomLine) {
        this(netType, listener, null, true, showBottomLine);
    }

    public TestItemData(String netType, View.OnClickListener listener, View.OnLongClickListener longClickListener) {
        this(netType, listener, longClickListener, true);
    }

    public TestItemData(String netType, View.OnClickListener listener, View.OnLongClickListener longClickListener,
                        boolean background) {
        this(netType, listener, longClickListener, background, true);
    }

    public TestItemData(String netType, View.OnClickListener listener, View.OnLongClickListener longClickListener,
                        boolean background,
                        boolean showBottomLine) {
        this.mContentText = netType;
        this.mListener = listener;
        this.mLongClickListener = longClickListener;
        this.hasBackground = background;
        this.showBottomLine = showBottomLine;
    }

    public TestItemData(String netType, boolean background) {
        mContentText = netType;
        hasBackground = background;
    }

    public int getResID() {
        return R.layout.com_bihe0832_card_test_item;
    }

    public Class<? extends CardBaseHolder> getViewHolderClass() {
        return TestItemHolder.class;
    }

    @Override
    public boolean autoAddItem() {
        return true;
    }
}