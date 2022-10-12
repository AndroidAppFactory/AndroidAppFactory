package com.bihe0832.android.base.debug.tab.bottom;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.bihe0832.android.base.debug.R;
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
    protected int getLayoutID() {
        return R.layout.debug_tab_svga;
    }

    @Override
    public void setSelected(boolean selected) {
        mIconView.setColorFilter(Color.WHITE);
        if (selected) {
            ViewExtKt.setViewWidth(mIconView, DisplayUtil.dip2px(getContext(), 54f));
            ViewExtKt.setViewHeight(mIconView, DisplayUtil.dip2px(getContext(), 54f));
        } else {
            ViewExtKt.setViewWidth(mIconView, DisplayUtil.dip2px(getContext(), 24f));
            ViewExtKt.setViewHeight(mIconView, DisplayUtil.dip2px(getContext(), 24f));
        }

        super.setSelected(selected);
    }

    @Override
    protected void onSelectChanged() {
        updateReadDot();
        super.onSelectChanged();
    }

    void updateReadDot() {
        if (mTipsView.getVisibility() == View.VISIBLE) {
            int totalWidth = getWidth();
            int iconWidth = totalWidth;
            if (isSelected()) {
                iconWidth = DisplayUtil.dip2px(getContext(), 54f);
            } else {
                iconWidth = DisplayUtil.dip2px(getContext(), 24f);
            }
            Paint paint = new Paint();
            paint.setTextSize(DisplayUtil.dip2px(getContext(), 9));
            float length = paint.measureText(mTipsView.getText().toString());
            DebugSvgaBottomBarTabExtKt.resetReadDotRightMargin(mTipsView, totalWidth, iconWidth, (int) length);
        }
    }

    @Override
    public void showUnreadMsg() {
        super.showUnreadMsg();
        updateReadDot();
    }
}
