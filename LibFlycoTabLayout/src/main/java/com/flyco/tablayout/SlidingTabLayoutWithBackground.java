package com.flyco.tablayout;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.flyco.tablayout.SlidingTabLayout;

/**
 * 滑动TabLayout,对于ViewPager的依赖性强
 */
public class SlidingTabLayoutWithBackground extends SlidingTabLayout {
    private RelativeLayout mALLContainer;
    private ImageView mBgImageView;

    public SlidingTabLayoutWithBackground(Context context) {
        this(context, null, 0);
    }

    public SlidingTabLayoutWithBackground(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayoutWithBackground(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    protected void initView(Context context) {
        mALLContainer = new RelativeLayout(context);
        addView(mALLContainer);

        mBgImageView = new ImageView(context);
        mBgImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mALLContainer.addView(mBgImageView);
        mTabsContainer = new LinearLayout(context);
        mTabsContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mALLContainer.addView(mTabsContainer);
    }


    public ImageView getBackgroundImageView() {
        return mBgImageView;
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        ObjectAnimator animation = ObjectAnimator.ofFloat(mBgImageView, "translationX", mTabRect.left + (mTabRect.right - mTabRect.left) / 2 - mBgImageView.getWidth() / 2);
        animation.setDuration(250);
        animation.start();
    }

}
