package com.bihe0832.android.common.ui.bottom.bar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.bihe0832.android.common.bottom.bar.R;
import com.bihe0832.android.common.svga.SVGAHelperKt;
import com.bihe0832.android.lib.ui.bottom.bar.BaseBottomBarTab;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGAImageView;

/**
 * height:56
 */
public class SvgaBottomBarTab extends FrameLayout implements BaseBottomBarTab {
    protected SVGAImageView mIconView;
    protected TextView mTitleView;
    protected TextView mTipsView;
    protected View mDotView;

    private int mTabPosition = -1;

    protected String mActionSvga = "";
    protected int mNormalImageRes = -1;
    protected int mSelectedImageRes = -1;

    public SvgaBottomBarTab(Context context, @DrawableRes int normalImageRes, @DrawableRes int selectedImageRes, String actionSvga, String title) {
        this(context, null, 0, normalImageRes, selectedImageRes, actionSvga, title);
    }

    public SvgaBottomBarTab(Context context, AttributeSet attrs, int defStyleAttr, int normalImageRes, int selectedImageRes, String actionSvga, String title) {
        super(context, attrs, defStyleAttr);
        init(context, normalImageRes, selectedImageRes, actionSvga, title);
    }

    private void init(Context context, int normalImageRes, int selectedImageRes, String actionSvga, String title) {
        View.inflate(context, R.layout.common_tab_svga, this);
        mActionSvga = actionSvga;
        mNormalImageRes = normalImageRes;
        mSelectedImageRes = selectedImageRes;

        mIconView = findViewById(R.id.tab_icon);
        mTitleView = findViewById(R.id.tab_title);
        mTipsView = findViewById(R.id.tab_tips);
        mDotView = findViewById(R.id.tab_tips_dot);

        mTitleView.setText(title);
        mIconView.setCallback(new SVGACallback() {
            @Override
            public void onStep(int i, double v) {

            }

            @Override
            public void onRepeat() {

            }

            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                doSelect();
            }
        });
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            SVGAHelperKt.playAssets(mIconView, mActionSvga);
        } else {
            doSelect();
        }
    }

    protected void doSelect() {
        if (isSelected()) {
            mIconView.setImageResource(mSelectedImageRes);
            mIconView.setColorFilter(ContextCompat.getColor(getContext(), R.color.com_bihe0832_tab_selected));
            mTitleView.setTextColor(ContextCompat.getColor(getContext(), R.color.com_bihe0832_tab_selected));
        } else {
            mIconView.setImageResource(mNormalImageRes);
            mIconView.setColorFilter(ContextCompat.getColor(getContext(), R.color.com_bihe0832_tab_unselect));
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

    public void setUnreadCount(int num) {
        BottomBarTabExtKt.setUnreadCount(mTipsView, num);
    }

    public void setUnreadDot(boolean visible) {
        BottomBarTabExtKt.setUnreadDot(mDotView, visible);
    }
}
