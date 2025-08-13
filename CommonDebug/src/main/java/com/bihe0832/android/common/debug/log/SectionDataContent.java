package com.bihe0832.android.common.debug.log;

import android.view.View;
import com.bihe0832.android.common.debug.R;
import com.bihe0832.android.lib.adapter.CardBaseHolder;
import com.bihe0832.android.lib.adapter.CardBaseModule;

/**
 * @author zixie code@bihe0832.com Created on 2019-11-21. Description: Description
 */
public class SectionDataContent extends CardBaseModule {

    public int getResID() {
        return R.layout.com_bihe0832_card_debug_content;
    }

    public static final int TYPE_OPEN = 1;
    public static final int TYPE_SEND = 2;

    public Class<? extends CardBaseHolder> getViewHolderClass() {
        return SectionHolderContent.class;
    }

    public String mTitleName;
    public String mLogFileName;
    public boolean mSort = false;
    public boolean mShowLine = true;
    public boolean mShowAction = false;
    public ItemOnClickListener mActionListener = null;

    public SectionDataContent() {

    }

    public SectionDataContent(String title, String logFilePath, boolean showAction) {
        mTitleName = title;
        mLogFileName = logFilePath;
        mShowAction = showAction;
    }

    public SectionDataContent(String title, String logFilePath, boolean showAction, boolean sort, boolean showLine) {
        mTitleName = title;
        mLogFileName = logFilePath;
        mShowAction = showAction;
        mSort = sort;
        mShowLine = showLine;
    }

    public SectionDataContent(String title, ItemOnClickListener clickListener) {
        mTitleName = title;
        mActionListener = clickListener;
    }

    @Override
    public boolean autoAddItem() {
        return true;
    }

    public interface ItemOnClickListener {

        void onClick(View var1, int type);
    }
}