package com.bihe0832.android.common.ui.bottom.bar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bihe0832.android.common.bottom.bar.R;
import com.bihe0832.android.common.svga.SVGAHelperKt;
import com.bihe0832.android.lib.ui.bottom.bar.BaseBottomBarTab;
import com.bihe0832.android.lib.ui.custom.view.background.TextViewWithBackground;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGAImageView;

/**
 * height:56
 */
public class SvgaBottomBarTab extends BaseBottomBarTab {
    protected SVGAImageView mIconView;
    protected TextView mTitleView;
    protected TextViewWithBackground mTipsView;

    protected String mActionSvga = "";
    protected int mNormalImageRes = -1;
    protected int mSelectedImageRes = -1;

    public SvgaBottomBarTab(Context context, @DrawableRes int normalImageRes, @DrawableRes int selectedImageRes, String actionSvga, String title) {
        this(context, null, 0, normalImageRes, selectedImageRes, actionSvga, title);
    }

    public SvgaBottomBarTab(Context context, AttributeSet attrs, int defStyleAttr, int normalImageRes, int selectedImageRes, String actionSvga, String title) {
        super(context, attrs, defStyleAttr, normalImageRes, title);
        mActionSvga = actionSvga;
        mNormalImageRes = normalImageRes;
        mSelectedImageRes = selectedImageRes;
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
    protected int getLayoutID() {
        return R.layout.common_tab_svga;
    }

    @Override
    protected void initView(@NonNull Context context) {
        mIconView = findViewById(R.id.tab_icon);
        mTitleView = findViewById(R.id.tab_title);
        mTipsView = findViewById(R.id.tab_tips);
    }

    @Override
    protected void initViewEvent(int icon, @Nullable CharSequence title) {
        super.initViewEvent(icon, title);
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
                onSelectChanged();
            }
        });
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        onSelectChanged();
        if (selected) {
            SVGAHelperKt.playAssets(mIconView, mActionSvga);
        }
    }

    protected void onSelectChanged() {
        if (isSelected()) {
            mIconView.setImageResource(mSelectedImageRes);
            mTitleView.setTextColor(getContext().getResources().getColor(R.color.com_bihe0832_tab_selected));
        } else {
            mIconView.setImageResource(mNormalImageRes);
            mTitleView.setTextColor(getContext().getResources().getColor(R.color.com_bihe0832_tab_unselect));
        }
    }
}
