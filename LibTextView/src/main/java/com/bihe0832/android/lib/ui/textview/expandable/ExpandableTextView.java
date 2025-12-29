package com.bihe0832.android.lib.ui.textview.expandable;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import com.bihe0832.android.lib.theme.ThemeResourcesManager;
import com.bihe0832.android.lib.ui.textview.R;
import com.bihe0832.android.lib.utils.os.BuildUtils;

/**
 * 在RecyclerView下使用时设置Text请直接使用setShrinkText()或者setExpandText()
 * 避免RecyclerView的复用机制导致展开状态异常的情况
 */
public abstract class ExpandableTextView extends AppCompatTextView {

    /**
     * 获取显示的文本
     */
    protected abstract CharSequence getTextByConfig();

    public static final int STATUS_SHRINK = 0;
    public static final int STATUS_EXPAND = 1;

    protected static final int MAX_LINE_ON_STRIKE = 4;
    protected static final int TO_EXPAND_HINT_COLOR = 0xFF6D859E;
    protected static final int TO_SHRINK_HINT_COLOR = 0xFF6D859E;
    protected static final boolean TOGGLE_ENABLE = true;
    protected static final boolean TO_EXPAND_HINT_SHOW = true;
    protected static final boolean TO_SHRINK_HINT_SHOW = true;
    protected static final boolean ELLIPISIS_HINT_SHOW = true;

    protected int mMaxLineOnStrike;

    protected String mEllipsisHint;
    protected boolean mShowEllipsisHint;

    protected String mToExpandHint;
    protected int mToExpandHintColor;
    protected int mToExpandHintTypeface;
    protected boolean mShowToExpandHint;

    protected String mToShrinkHint;
    protected int mToShrinkHintColor;
    protected int mToShrinkHintTypeface;
    protected boolean mShowToShrinkHint;

    protected boolean mToggleEnable;
    protected int mCurrentStatus = STATUS_SHRINK;

    protected CharSequence mOriginText;
    protected BufferType mBufferType;
    protected int mWidth;

    protected OnExpandListener mOnExpandListener;
    protected OnExpandBtnClickListener mOnExpandBtnClickListener;
    protected TouchableSpan mTouchableSpan;
    protected TextPaint mPaint;
    protected boolean performClick = true;

    public ExpandableTextView(Context context) {
        super(context);
        init();
    }

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        init();
    }

    public ExpandableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init();
    }

    protected void init() {
        mTouchableSpan = new TouchableSpan();
        setMovementMethod(new CustomMovementMethod());
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver obs = getViewTreeObserver();
                if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
                setTextInternal(getTextByConfig(), mBufferType);
            }
        });
        //当需要点击事件来完成不展开TextView并执行其他操作的时候，只需要自行设置OnClickListener即可
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });
    }

    protected void initAttrs(Context context, AttributeSet attrs) {
        mBufferType = BufferType.NORMAL;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);

        mMaxLineOnStrike = typedArray
                .getInteger(R.styleable.ExpandableTextView_etv_MaxLinesOnShrink, MAX_LINE_ON_STRIKE);

        mEllipsisHint = typedArray.getString(R.styleable.ExpandableTextView_etv_EllipsisHint);
        mShowEllipsisHint = typedArray
                .getBoolean(R.styleable.ExpandableTextView_etv_EllipsisHintShow, ELLIPISIS_HINT_SHOW);

        mToExpandHint = typedArray.getString(R.styleable.ExpandableTextView_etv_ToExpandHint);
        mToExpandHintColor = typedArray
                .getInteger(R.styleable.ExpandableTextView_etv_ToExpandHintColor, TO_EXPAND_HINT_COLOR);
        mToExpandHintTypeface = typedArray.getInt(R.styleable.ExpandableTextView_etv_ToExpandTypeface, Typeface.NORMAL);
        mShowToExpandHint = typedArray
                .getBoolean(R.styleable.ExpandableTextView_etv_ToExpandHintShow, TO_EXPAND_HINT_SHOW);

        mToShrinkHint = typedArray.getString(R.styleable.ExpandableTextView_etv_ToShrinkHint);
        mToShrinkHintColor = typedArray
                .getInteger(R.styleable.ExpandableTextView_etv_ToShrinkHintColor, TO_SHRINK_HINT_COLOR);
        mToShrinkHintTypeface = typedArray.getInt(R.styleable.ExpandableTextView_etv_ToShrinkTypeface, Typeface.NORMAL);
        mShowToShrinkHint = typedArray
                .getBoolean(R.styleable.ExpandableTextView_etv_ToShrinkHintShow, TO_SHRINK_HINT_SHOW);

        mToggleEnable = typedArray.getBoolean(R.styleable.ExpandableTextView_etv_EnableToggle, TOGGLE_ENABLE);
        mCurrentStatus = typedArray.getInt(R.styleable.ExpandableTextView_etv_InitState, STATUS_SHRINK);

        if (mEllipsisHint == null) {
            mEllipsisHint = ThemeResourcesManager.INSTANCE.getString(com.bihe0832.android.lib.aaf.res.R.string.to_ellipsis_hint);
        }

        if (mToExpandHint == null) {
            mToExpandHint = ThemeResourcesManager.INSTANCE.getString(com.bihe0832.android.lib.aaf.res.R.string.to_expand_hint);
        }

        if (mToShrinkHint == null) {
            mToShrinkHint = ThemeResourcesManager.INSTANCE.getString(com.bihe0832.android.lib.aaf.res.R.string.to_shrink_hint);
        }

        typedArray.recycle();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        mOriginText = text;
        mBufferType = type;
        setTextInternal(getTextByConfig(), type);
    }

    public void setShrinkText(String text) {
        mToShrinkHint = text;
        setTextInternal(getTextByConfig(), mBufferType);
    }

    public void setExpandText(String text) {
        mToExpandHint = text;
        setTextInternal(getTextByConfig(), mBufferType);
    }

    public void setCurrentStatus(int mCurrentStatus) {
        this.mCurrentStatus = mCurrentStatus;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
    }

    protected void setTextInternal(CharSequence text, BufferType type) {
        super.setText(text, type);
    }

    /**
     * 判断文本的末尾是不是换行
     *
     * @param text
     * @return
     */
    private boolean endOfStringIsWrap(String text) {
        if ('\n' == text.charAt(text.length() - 1)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 移除String末尾的换行符
     *
     * @param text
     * @return
     */
    private String removeEndWrap(String text) {
        boolean removeOK = false;
        while (!removeOK) {
            if ('\n' == text.charAt(text.length() - 1)) {
                //removeOK = false;
                text = text.substring(0, text.length() - 2);
            } else {
                removeOK = true;
            }
        }
        return text;
    }

    @Override
    public boolean performClick() {
        if (performClick) {
            return super.performClick();
        }
        return false;
    }

    protected int getLengthOfString(String string) {
        if (string == null) {
            return 0;
        }
        return string.length();
    }

    /**
     * 获取能和preText和addedText一起填满一行宽度的空格字符串
     *
     * @param preText
     * @param addedText
     * @return
     */
    protected String getSpaceText(CharSequence preText, CharSequence addedText) {
        StringBuilder spaceText = new StringBuilder("");
        float width = mPaint.measureText(preText.toString() + spaceText + addedText) + 0.5f;
        while (width <= mWidth - mPaint.measureText(" ")) {
            spaceText.append(" ");
            width = mPaint.measureText(preText.toString() + spaceText + addedText) + 0.5f;
        }
        return spaceText.toString();
    }

    /**
     * 设置展开状态
     *
     * @param status
     */
    public void setExpandStatus(int status) {
        if (status == STATUS_EXPAND) {
            mCurrentStatus = STATUS_EXPAND;
        } else {
            mCurrentStatus = STATUS_SHRINK;
        }
        setTextInternal(getTextByConfig(), mBufferType);
    }

    /**
     * 切换状态
     */
    private void toggle() {
        switch (mCurrentStatus) {
            case STATUS_EXPAND:
                mCurrentStatus = STATUS_SHRINK;
                if (mOnExpandListener != null) {
                    mOnExpandListener.onShrink(this);
                }
                break;
            case STATUS_SHRINK:
                mCurrentStatus = STATUS_EXPAND;
                if (mOnExpandListener != null) {
                    mOnExpandListener.onExpand(this);
                }
                break;
        }
        setTextInternal(getTextByConfig(), mBufferType);
    }

    public interface OnExpandListener {

        void onExpand(ExpandableTextView view);

        void onShrink(ExpandableTextView view);
    }

    public interface OnExpandBtnClickListener {

        void onClick();
    }

    private class TouchableSpan extends ClickableSpan {

        @Override
        public void onClick(View widget) {
            if (mOnExpandBtnClickListener != null) {
                mOnExpandBtnClickListener.onClick();
            } else {
                toggle();
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            switch (mCurrentStatus) {
                case STATUS_EXPAND:
                    ds.setColor(mToShrinkHintColor);
                    ds.setTypeface(Typeface.create(Typeface.DEFAULT, mToShrinkHintTypeface));
                    break;
                case STATUS_SHRINK:
                    ds.setColor(mToExpandHintColor);
                    ds.setTypeface(Typeface.create(Typeface.DEFAULT, mToExpandHintTypeface));
                    break;
            }
            ds.setUnderlineText(false);
        }
    }

    public class CustomMovementMethod extends LinkMovementMethod {

        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            int action = event.getAction();
            performClick = true;
            if (action == MotionEvent.ACTION_UP) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                TouchableSpan[] link = buffer.getSpans(off, off, TouchableSpan.class);

                if (link.length != 0) {
                    //除了点击事件，我们不要其他东西
                    performClick = false;
                    link[0].onClick(widget);
                    return true;
                }
            }
            return super.onTouchEvent(widget, buffer, event);
        }

    }

    public void setOnExpandListener(OnExpandListener onExpandListener) {
        this.mOnExpandListener = onExpandListener;
    }

    public void setOnExpandBtnClickListener(OnExpandBtnClickListener onExpandBtnClickListener) {
        //清空ClickListener
        setOnClickListener(null);
        this.mOnExpandBtnClickListener = onExpandBtnClickListener;
    }


    protected SpannableStringBuilder getNotFullContent(DynamicLayout mOriginLayout, CharSequence originText,
                                                       DynamicLayout mFinalLayout, String mToExpandHint) {
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        //遇到了新行
        stringBuilder
                .append(getOriginalTextFromStartToLine(mOriginLayout, originText, 0, mOriginLayout.getLineCount()));

        if (mOriginLayout.getLineCount() != mFinalLayout.getLineCount()) {
            CharSequence spaceText = getSpaceText(
                    getOriginalTextByLine(mOriginLayout, originText, mOriginLayout.getLineCount()),
                    "");
            stringBuilder.append(spaceText);
            stringBuilder.append("\n");
            stringBuilder.append(getSpaceText("", mToExpandHint));
        } else {
            stringBuilder
                    .append(getSpaceText(getOriginalTextByLine(mOriginLayout, originText, mOriginLayout.getLineCount()),
                            mToExpandHint));
        }

        stringBuilder.append(mToExpandHint);
        stringBuilder.setSpan(mTouchableSpan, stringBuilder.length() - getLengthOfString(mToExpandHint),
                stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return stringBuilder;
    }

    protected SpannableStringBuilder getFullContent(DynamicLayout mOriginLayout, CharSequence originText) {
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        int lineNo = mMaxLineOnStrike;
        if (mOriginLayout.getLineCount() == mMaxLineOnStrike) {
            lineNo = mMaxLineOnStrike - 1;
        } else {
            lineNo = mMaxLineOnStrike;
        }

        if (mMaxLineOnStrike > 1) {
            stringBuilder.append(getOriginalTextFromStartToLine(mOriginLayout, originText, 0, mMaxLineOnStrike - 1))
                    .append("\n");
        } else {
            lineNo = 0;
        }

        //折叠这行的第一个字符index
        int indexStart = mOriginLayout.getLineStart(lineNo);
        //折叠这行的最后一个字符的index
        int indexEnd = mOriginLayout.getLineEnd(lineNo);
        int indexEndTrimmed = indexEnd;

        //如果是换行的话减去一个换行
        if (originText.charAt(indexEnd - 1) == '\n' && indexEndTrimmed > indexStart) {
            indexEndTrimmed = indexEnd - 1;
        }
        float trimmedWidth = mPaint
                .measureText(originText.subSequence(indexStart, indexEndTrimmed) + mEllipsisHint + mToExpandHint);

        while (trimmedWidth > mWidth) {
            indexEndTrimmed--;
            trimmedWidth = mPaint.measureText(
                    originText.subSequence(indexStart, indexEndTrimmed).toString() + mEllipsisHint + mToExpandHint);
        }

        CharSequence spaceText = getSpaceText(originText.subSequence(indexStart, indexEndTrimmed),
                mEllipsisHint + mToExpandHint);
        stringBuilder.append(originText.subSequence(indexStart, indexEndTrimmed)).append(spaceText)
                .append(mEllipsisHint);
        stringBuilder.append(mToExpandHint);
        stringBuilder.setSpan(mTouchableSpan,
                stringBuilder.length() - getLengthOfString(mToExpandHint),
                stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return stringBuilder;
    }

    //获取指定行的文字
    protected String getOriginalTextByLine(DynamicLayout mOriginLayout, CharSequence mOriginText, int lineNum) {
        //折叠这行的第一个字符index
        return getOriginalTextFromLineToLine(mOriginLayout, mOriginText, lineNum, lineNum);
    }

    protected String getOriginalTextFromLineToLine(DynamicLayout mOriginLayout, CharSequence mOriginText,
                                                   int startLineNum,
                                                   int endLineNum) {
        int indexStart = mOriginLayout.getLineStart(startLineNum - 1);
        int indexEnd = mOriginLayout.getLineEnd(endLineNum - 1);
        return getOriginalTextFromStartToEnd(mOriginText, indexStart, indexEnd);
    }

    protected String getOriginalTextFromStartToLine(DynamicLayout mOriginLayout, CharSequence mOriginText, int start,
                                                    int lineNum) {
        //折叠这行的最后一个字符 + 1的index，
        int indexEnd = mOriginLayout.getLineEnd(lineNum - 1);
        return getOriginalTextFromStartToEnd(mOriginText, start, indexEnd);
    }

    protected String getOriginalTextFromStartToEnd(CharSequence mOriginText, int start, int indexEnd) {
        //如果是换行的话减去一个换行
        int indexEndTrimmed = indexEnd;
        if (mOriginText.charAt(indexEnd - 1) == '\n' && indexEndTrimmed > start) {
            indexEndTrimmed = indexEnd - 1;
        }
        //折叠那一行的原始字符串
        return mOriginText.subSequence(start, indexEndTrimmed).toString();
    }

}
