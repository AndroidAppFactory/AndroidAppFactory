package com.bihe0832.android.lib.ui.textview.expandable;

import android.content.Context;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import org.jetbrains.annotations.Nullable;


public class CommentTextView extends ExpandableTextView {


    public CommentTextView(Context context) {
        super(context);
    }

    public CommentTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CommentTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();
        setOnClickListener(null);
        mToShrinkHint = mToExpandHint;
        mToShrinkHintColor = mToExpandHintColor;
        mToShrinkHintTypeface = mToExpandHintTypeface;
    }

    protected CharSequence getTextByConfig() {
        if (TextUtils.isEmpty(mOriginText)) {
            return mOriginText;
        }

        if (mWidth == 0) {
            return mOriginText;
        }

        mPaint = getPaint();
        DynamicLayout mOriginLayout = new DynamicLayout(mOriginText, mPaint, mWidth, Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                0.0f, false);
        DynamicLayout mFinalLayout = new DynamicLayout(mOriginText + mEllipsisHint + mToExpandHint, mPaint, mWidth,
                Layout.Alignment.ALIGN_NORMAL, 1.0f,
                0.0f, false);

        if (mFinalLayout.getLineCount() <= mMaxLineOnStrike) {
            return getNotFullContent(mOriginLayout, mOriginText, mFinalLayout, mToExpandHint);
        } else {
            return getFullContent(mOriginLayout, mOriginText);
        }
    }


}
