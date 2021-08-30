package com.bihe0832.android.lib.ui.textview.expandable;

import android.content.Context;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import org.jetbrains.annotations.Nullable;




public class FoldedTextView extends ExpandableTextView {


    public FoldedTextView(Context context) {
        super(context);
    }

    public FoldedTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FoldedTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected CharSequence getTextByConfig() {

        if (TextUtils.isEmpty(mOriginText)) {
            return mOriginText;
        }

        if (mWidth == 0) {
            return mOriginText;
        }

        mPaint = getPaint();
        DynamicLayout mLayout = new DynamicLayout(mOriginText, mPaint, mWidth,
                Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        int lineCount = mLayout.getLineCount();
        if (lineCount <= mMaxLineOnStrike) {
            return mOriginText;
        }

        switch (mCurrentStatus) {
            case STATUS_SHRINK: {

                String ellipsisHintFinal = mShowEllipsisHint ? mEllipsisHint : "";
                String expandHintFinal = mShowToExpandHint ? mToExpandHint : "";

                //折叠这行的第一个字符index
                int indexStart = mLayout.getLineStart(mMaxLineOnStrike - 1);
                //折叠这行的最后一个字符 + 1的index，
                int indexEnd = mLayout.getLineEnd(mMaxLineOnStrike - 1);
                //如果是换行的话减去一个换行
                int indexEndTrimmed = indexEnd;
                if (mOriginText.charAt(indexEnd - 1) == '\n' && indexEndTrimmed > indexStart) {
                    indexEndTrimmed = indexEnd - 1;
                }

                float trimmedWidth = mPaint.measureText(
                        mOriginText.subSequence(indexStart, indexEndTrimmed).toString()
                                + ellipsisHintFinal + expandHintFinal
                );

                while (trimmedWidth > mWidth) {
                    indexEndTrimmed--;
                    trimmedWidth = mPaint.measureText(
                            mOriginText.subSequence(indexStart, indexEndTrimmed).toString()
                                    + ellipsisHintFinal + expandHintFinal
                    );
                }
                CharSequence fixText = mOriginText.subSequence(0, indexEndTrimmed);
                //折叠那一行的原始字符串
                CharSequence maxLineFixText = mOriginText.subSequence(indexStart, indexEndTrimmed).toString();
                //一行占不满的时候添加的空格字符串
                CharSequence spaceText = getSpaceText(maxLineFixText, ellipsisHintFinal + expandHintFinal);
                SpannableStringBuilder stringBuilder =
                        new SpannableStringBuilder(fixText).append(spaceText).append(ellipsisHintFinal);

                if (mShowToExpandHint) {
                    stringBuilder.append(mToExpandHint);
                    stringBuilder.setSpan(mTouchableSpan,
                            stringBuilder.length() - getLengthOfString(expandHintFinal),
                            stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                return stringBuilder;
            }
            case STATUS_EXPAND: {
                if (!mShowToShrinkHint) {
                    return mOriginText;
                }
                SpannableStringBuilder ssbExpand = new SpannableStringBuilder(mOriginText).append(mToShrinkHint);
                ssbExpand.setSpan(mTouchableSpan, ssbExpand.length() - getLengthOfString(mToShrinkHint),
                        ssbExpand.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                return ssbExpand;
            }
        }
        return mOriginText;
    }
}
