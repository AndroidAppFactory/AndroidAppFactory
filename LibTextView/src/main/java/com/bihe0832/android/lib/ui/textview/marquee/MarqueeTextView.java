package com.bihe0832.android.lib.ui.textview.marquee;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import androidx.appcompat.widget.AppCompatTextView;

import com.bihe0832.android.lib.ui.textview.R;
import com.bihe0832.android.lib.utils.os.DisplayUtil;


public class MarqueeTextView extends AppCompatTextView {

    public interface OnScrollListener {
        void OnComplete();

        void onPause();

        void onStop();

        void onStart();
    }

    private OnScrollListener mOnScrollListener;
    /**
     * 滚动位置-从左边开始
     */
    public static final int SCROLL_START = 1;
    /**
     * 滚动位置-从右边开始
     */
    public static final int SCROLL_END = 2;
    /**
     * 默认滚动100px所需要的时间
     */
    private static final int ROLLING_INTERVAL_DEFAULT = 500;
    /**
     * 第一次滚动默认延迟
     */
    private static final int FIRST_SCROLL_DELAY_DEFAULT = 10;
    /**
     * 每次滚动的默认间隔
     */
    private static final int NEXT_SCROLL_DELAY_DEFAULT = 500;
    /**
     * 滚动器
     */
    private Scroller mScroller = null;
    /**
     * 滚动滚动100px所需要的时间
     */
    private int mRollingIntervalFor100Px = ROLLING_INTERVAL_DEFAULT;
    /**
     * 滚动的初始 X 位置
     */
    private int mPausedX = 0;
    /**
     * 是否暂停
     */
    private boolean mPaused = true;
    /**
     * 是否第一次
     */
    private boolean mFirst = true;
    /**
     * 滚动次数
     */
    private int mScrollNum = -1;
    /**
     * 开始滑动的位置
     */
    private int mScrollStart = 0;
    /**
     * 初次滚动时间间隔
     */
    private int mFirstScrollDelay = FIRST_SCROLL_DELAY_DEFAULT;
    private int mNextScrollDelay = NEXT_SCROLL_DELAY_DEFAULT;

    public MarqueeTextView(Context context) {
        this(context, null);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs, defStyle);
    }

    private void initView(Context context, AttributeSet attrs, int defStyle) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MarqueeTextView);
        mRollingIntervalFor100Px = typedArray.getInt(R.styleable.MarqueeTextView_scroll_speed_100_px, ROLLING_INTERVAL_DEFAULT);
        mScrollStart = typedArray.getInt(R.styleable.MarqueeTextView_scroll_start, SCROLL_START);
        mScrollNum = typedArray.getInt(R.styleable.MarqueeTextView_scroll_repeat, -1);
        mFirstScrollDelay = typedArray.getInt(R.styleable.MarqueeTextView_scroll_first_delay, FIRST_SCROLL_DELAY_DEFAULT);
        mNextScrollDelay = typedArray.getInt(R.styleable.MarqueeTextView_scroll_next_delay, NEXT_SCROLL_DELAY_DEFAULT);
        typedArray.recycle();
        setSingleLine();
        setEllipsize(null);
    }

    /**
     * 开始滚动
     */
    public void startScroll() {
        mPausedX = 0;
        mPaused = true;
        mFirst = true;
        resumeScroll();
    }

    public void setOnScrollListener(OnScrollListener mOnScrollListener) {
        this.mOnScrollListener = mOnScrollListener;
    }

    /**
     * 继续滚动
     */
    public void resumeScroll() {
        if (!mPaused) {
            return;
        }
        // 设置水平滚动
        setHorizontallyScrolling(true);

        // 使用 LinearInterpolator 进行滚动
        if (mScroller == null) {
            mScroller = new Scroller(this.getContext(), new LinearInterpolator());
            setScroller(mScroller);
        }
        int scrollingLen = calculateTotalScrollingLen();
        final int distance = scrollingLen - mPausedX;
        int length;
        if (mFirst) {
            if (mScrollStart == SCROLL_END) {
                length = distance + getWidth() - (-1 * getWidth());
            } else {
                length = distance - mPausedX;
            }
        } else {
            length = distance - mPausedX;
        }
        if (length < DisplayUtil.getRealScreenSizeX(getContext())) {
            length = DisplayUtil.getRealScreenSizeX(getContext());
        }
        final int duration = (Double.valueOf(mRollingIntervalFor100Px * length * 1.00000 / 100)).intValue();
        if (mFirst) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (null != mOnScrollListener) {
                        mOnScrollListener.onStart();
                    }
                    if (mScrollStart == SCROLL_END) {
                        mScroller.startScroll(-1 * getWidth(), 0, distance + getWidth(), 0, duration * 2);
                    } else {
                        mScroller.startScroll(mPausedX, 0, distance, 0, duration);
                    }
                    invalidate();
                    setVisibility(View.VISIBLE);
                    mPaused = false;
                }
            }, mFirstScrollDelay);
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScroller.startScroll(mPausedX, 0, distance, 0, duration);
                    invalidate();
                    mPaused = false;
                }
            }, mNextScrollDelay);
        }
    }

    /**
     * 暂停滚动
     */
    public void pauseScroll() {
        if (null == mScroller) {
            return;
        }
        if (mPaused) {
            return;
        }
        if (null != mOnScrollListener) {
            mOnScrollListener.onPause();
        }
        mPaused = true;
        mPausedX = mScroller.getCurrX();
        mScroller.abortAnimation();
    }

    /**
     * 停止滚动，并回到初始位置
     */
    public void stopScroll() {
        if (null == mScroller) {
            return;
        }
        if (null != mOnScrollListener) {
            mOnScrollListener.onStop();
        }
        mPaused = true;
        mScroller.startScroll(0, 0, 0, 0, 0);
    }

    /**
     * 计算滚动的距离
     *
     * @return 滚动的距离
     */
    private int calculateTotalScrollingLen() {
        TextPaint tp = getPaint();
        Rect rect = new Rect();
        String strTxt = getText().toString();
        tp.getTextBounds(strTxt, 0, strTxt.length(), rect);
        return (int) getPaint().measureText(strTxt);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (null == mScroller) return;
        if (mScroller.isFinished() && (!mPaused)) {
            if (null != mOnScrollListener) {
                mOnScrollListener.OnComplete();
            }
            if (mScrollNum > 0) {
                mScrollNum--;
            }
            if (mScrollNum == 0) {
                stopScroll();
                return;
            }

            mPaused = true;
            mPausedX = -1 * getWidth();
            mFirst = false;
            this.resumeScroll();
        }
    }

    /**
     * 设置滚动100Px 使用的时间
     */
    public void setRollingIntervalFor100Px(int duration) {
        this.mRollingIntervalFor100Px = duration;
    }

    /**
     * 获取滚动模式
     */
    public int getScrollMode() {
        return this.mScrollNum;
    }

    /**
     * 设置滚动模式
     */
    public void setScrollNum(int mode) {
        this.mScrollNum = mode;
    }

    /**
     * 获取第一次滚动延迟
     */
    public int getScrollFirstDelay() {
        return mFirstScrollDelay;
    }

    /**
     * 设置第一次滚动延迟
     */
    public void setScrollFirstDelay(int delay) {
        this.mFirstScrollDelay = delay;
    }

    public boolean isPaused() {
        return mPaused;
    }
}