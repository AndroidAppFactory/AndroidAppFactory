package com.bihe0832.android.lib.ui.view;

import android.content.Context;
import android.os.Build;

import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.bihe0832.android.lib.utils.os.BuildUtils;


public class BottomBarTab extends FrameLayout {
    private ImageView mIcon;
    private TextView mTvTitle;
    private Context mContext;
    private int mTabPosition = -1;

//     private TextView mTvUnreadCount;
    private ImageView mUnreadDot;

    public BottomBarTab(Context context, @DrawableRes int icon, CharSequence title) {
        this(context, null, icon, title);
    }

    public BottomBarTab(Context context, AttributeSet attrs, int icon, CharSequence title) {
        this(context, attrs, 0, icon, title);
    }

    public BottomBarTab(Context context, AttributeSet attrs, int defStyleAttr, int icon, CharSequence title) {
        super(context, attrs, defStyleAttr);
        init(context, icon, title);
    }

    private void init(Context context, int icon, CharSequence title) {
        mContext = context;
//        TypedArray typedArray = context.obtainStyledAttributes(new int[]{R.attr.selectableItemBackgroundBorderless});
//        Drawable drawable = typedArray.getDrawable(0);
//        setBackgroundDrawable(drawable);
//        setBackgroundResource(R.color.colorAccent);
//        typedArray.recycle();

        setBackgroundDrawable(null);

        LinearLayout lLContainer = new LinearLayout(context);
        lLContainer.setOrientation(LinearLayout.VERTICAL);
        lLContainer.setGravity(Gravity.CENTER);
        LayoutParams paramsContainer = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsContainer.gravity = Gravity.CENTER;
        lLContainer.setLayoutParams(paramsContainer);

        mIcon = new ImageView(context);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        mIcon.setImageResource(icon);
        mIcon.setLayoutParams(params);
        mIcon.setAlpha(0.5f);
        mIcon.setColorFilter(ContextCompat.getColor(context, R.color.com_bihe0832_tab_unselect));
        lLContainer.addView(mIcon);

        mTvTitle = new TextView(context);
        mTvTitle.setText(title);
        LinearLayout.LayoutParams paramsTv = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsTv.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        mTvTitle.setTextSize(9);
        mTvTitle.setAlpha(0.5f);
        if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.LOLLIPOP) {
            mTvTitle.setLetterSpacing(0.19f);
        }
        mTvTitle.setTextColor(ContextCompat.getColor(context, R.color.com_bihe0832_tab_unselect));
        mTvTitle.setLayoutParams(paramsTv);
        lLContainer.addView(mTvTitle);

        addView(lLContainer);

//        int min = dip2px(context, 20);
//        int padding = dip2px(context, 5);
//        mTvUnreadCount = new TextView(context);
//        mTvUnreadCount.setBackgroundResource(R.drawable.com_bihe0832_common_msg_bubble);
//        mTvUnreadCount.setMinWidth(min);
//        mTvUnreadCount.setTextColor(Color.WHITE);
//        mTvUnreadCount.setPadding(padding, 0, padding, 0);
//        mTvUnreadCount.setGravity(Gravity.CENTER);
//        LayoutParams tvUnReadParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, min);
//        tvUnReadParams.gravity = Gravity.CENTER;
//        tvUnReadParams.leftMargin = dip2px(context, 17);
//        tvUnReadParams.bottomMargin = dip2px(context, 14);
//        mTvUnreadCount.setLayoutParams(tvUnReadParams);
//        mTvUnreadCount.setVisibility(GONE);

        mUnreadDot = new ImageView(context);
        mUnreadDot.setBackgroundResource(R.drawable.botton_bar_tab_red_dot);
        LayoutParams UnreadDotParams = new LayoutParams(dip2px(context,8),dip2px(context,8));
        UnreadDotParams.gravity = Gravity.CENTER;
        UnreadDotParams.leftMargin = dip2px(context,17);
        UnreadDotParams.bottomMargin = dip2px(context,14);
        mUnreadDot.setLayoutParams(UnreadDotParams);
        mUnreadDot.setVisibility(GONE);

//        addView(mTvUnreadCount);
        addView(mUnreadDot);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            mIcon.setAlpha(1.0f);
            mIcon.setColorFilter(ContextCompat.getColor(mContext,R.color.com_bihe0832_tab_selected));
            mTvTitle.setAlpha(1.0f);
            mTvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.com_bihe0832_tab_selected));
        } else {
            mIcon.setAlpha(0.5f);
            mIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.com_bihe0832_tab_unselect));
            mTvTitle.setAlpha(0.5f);
            mTvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.com_bihe0832_tab_unselect));
        }
    }

    public void setTabPosition(int position) {
        mTabPosition = position;
        if (position == 0) {
            setSelected(true);
        }
    }

    public int getTabPosition() {
        return mTabPosition;
    }

    /**
     * 设置未读数量
     */
//    public void setUnreadCount(int num) {
//        if (num <= 0) {
//            mTvUnreadCount.setText(String.valueOf(0));
//            mTvUnreadCount.setVisibility(GONE);
//        } else {
//            mTvUnreadCount.setVisibility(VISIBLE);
//            if (num > 99) {
//                mTvUnreadCount.setText("99+");
//            } else {
//                mTvUnreadCount.setText(String.valueOf(num));
//            }
//        }
//    }

    public void setUnreadDot(boolean visible){
        if (visible){
            mUnreadDot.setVisibility(VISIBLE);
        }else{
            mUnreadDot.setVisibility(GONE);
        }
    }

    /**
     * 获取当前未读数量
     */
//    public int getUnreadCount() {
//        int count = 0;
//        if (TextUtils.isEmpty(mTvUnreadCount.getText())) {
//            return count;
//        }
//        if (mTvUnreadCount.getText().toString().equals("99+")) {
//            return 99;
//        }
//        try {
//            count = Integer.valueOf(mTvUnreadCount.getText().toString());
//        } catch (Exception ignored) {
//        }
//        return count;
//    }

    private int dip2px(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
