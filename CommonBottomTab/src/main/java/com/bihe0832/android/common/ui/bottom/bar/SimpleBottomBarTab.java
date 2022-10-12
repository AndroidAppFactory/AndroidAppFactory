package com.bihe0832.android.common.ui.bottom.bar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.bihe0832.android.common.bottom.bar.R;
import com.bihe0832.android.lib.ui.bottom.bar.BaseBottomBarTab;
import com.bihe0832.android.lib.ui.textview.TextViewWithBackground;

/**
 * height:56
 */
public class SimpleBottomBarTab extends BaseBottomBarTab {

    protected ImageView mIconView;
    protected TextView mTitleView;
    protected TextViewWithBackground mTipsView;

    public SimpleBottomBarTab(Context context, @DrawableRes int icon, CharSequence title) {
        this(context, null, 0, icon, title);
    }

    public SimpleBottomBarTab(Context context, AttributeSet attrs, int defStyleAttr, int icon, CharSequence title) {
        super(context, attrs, defStyleAttr, icon, title);
        initView(context);
    }

    @Override
    protected int getLayoutID() {
        return R.layout.common_tab_simple;
    }

    @Override
    protected void initView(Context context) {
        mIconView = findViewById(R.id.tab_icon);
        mTitleView = findViewById(R.id.tab_title);
        mTipsView = findViewById(R.id.tab_tips);
    }

    @Override
    protected ImageView getIconView() {
        return mIconView;
    }

    @Override
    protected TextView getTitleView() {
        return mTitleView;
    }

    @Override
    protected TextViewWithBackground getTipsView() {
        return mTipsView;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            mIconView.setAlpha(1.0f);
            mIconView.setColorFilter(ContextCompat.getColor(getContext(), R.color.com_bihe0832_tab_selected));
            mTitleView.setAlpha(1.0f);
            mTitleView.setTextColor(ContextCompat.getColor(getContext(), R.color.com_bihe0832_tab_selected));
        } else {
            mIconView.setAlpha(0.5f);
            mIconView.setColorFilter(ContextCompat.getColor(getContext(), R.color.com_bihe0832_tab_unselect));
            mTitleView.setAlpha(0.5f);
            mTitleView.setTextColor(ContextCompat.getColor(getContext(), R.color.com_bihe0832_tab_unselect));
        }
    }
}
