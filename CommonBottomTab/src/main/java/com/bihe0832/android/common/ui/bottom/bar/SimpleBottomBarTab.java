package com.bihe0832.android.common.ui.bottom.bar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.bihe0832.android.common.bottom.bar.R;
import com.bihe0832.android.lib.ui.bottom.bar.BaseBottomBarTab;
import com.bihe0832.android.lib.ui.textview.TextViewWithBackground;
import com.bihe0832.android.lib.ui.textview.ext.TextViewWithBackgroundExtKt;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

/**
 * height:56
 */
public class SimpleBottomBarTab extends FrameLayout implements BaseBottomBarTab {
    protected ImageView mIconView;
    protected TextView mTitleView;
    protected TextViewWithBackground mTipsView;

    private int mTabPosition = -1;

    public SimpleBottomBarTab(Context context, @DrawableRes int icon, CharSequence title) {
        this(context, null, 0, icon, title);
    }

    public SimpleBottomBarTab(Context context, AttributeSet attrs, int defStyleAttr, int icon, CharSequence title) {
        super(context, attrs, defStyleAttr);
        init(context, icon, title);
    }

    private void init(Context context, int icon, CharSequence title) {
        View.inflate(context, R.layout.common_tab_simple, this);
        mIconView = findViewById(R.id.tab_icon);
        mIconView.setImageResource(icon);
        mTitleView = findViewById(R.id.tab_title);
        mTitleView.setText(title);
        mTipsView = findViewById(R.id.tab_tips);
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

    public void setTabPosition(int position) {
        mTabPosition = position;
    }

    public int getTabPosition() {
        return mTabPosition;
    }

    @Override
    public View getTabView() {
        return this;
    }

    /**
     * 设置未读数量
     */
    public void showUnreadMsg(int num) {
        TextViewWithBackgroundExtKt.changeStatusWithUnreadMsg(mTipsView, num, (int)getContext().getResources().getDimension(R.dimen.com_bihe0832_tab_red_dot_size));
    }
}
