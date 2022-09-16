package com.bihe0832.android.base.debug.tab;

import android.content.Context;
import android.util.AttributeSet;

import com.bihe0832.android.common.ui.bottom.bar.SvgaBottomBarTab;
import com.bihe0832.android.lib.ui.view.ext.ViewExtKt;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2022/9/16.
 * Description: Description
 */
class DebugSvgaBottomBarTab extends SvgaBottomBarTab {
    public DebugSvgaBottomBarTab(Context context, int normalImageRes, int selectedImageRes, String actionSvga, String title) {
        super(context, normalImageRes, selectedImageRes, actionSvga, title);
    }

    public DebugSvgaBottomBarTab(Context context, AttributeSet attrs, int defStyleAttr, int normalImageRes, int selectedImageRes, String actionSvga, String title) {
        super(context, attrs, defStyleAttr, normalImageRes, selectedImageRes, actionSvga, title);
    }

    @Override
    public void setSelected(boolean selected) {
        if (selected) {
            ViewExtKt.setViewWidth(mIconView, DisplayUtil.dip2px(getContext(), 54f));
            ViewExtKt.setViewHeight(mIconView, DisplayUtil.dip2px(getContext(), 54f));
        } else {
            ViewExtKt.setViewWidth(mIconView, DisplayUtil.dip2px(getContext(), 24f));
            ViewExtKt.setViewHeight(mIconView, DisplayUtil.dip2px(getContext(), 24f));
        }
        super.setSelected(selected);
    }
}
