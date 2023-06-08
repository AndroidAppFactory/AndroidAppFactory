package com.bihe0832.android.lib.ui.custom.view.slide;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * 滑动解锁，整个UI右移动
 */
public class SlideFinishLayout extends RelativeLayout {
    /**
     * 滑动的最小距离
     */
    private int mTouchSlop;

    private Scroller mScroller;

    /**
     * 父布局
     */
    private ViewGroup mParentView;

    /**
     * 按下X坐标
     */
    private int downX;
    /**
     * 按下Y坐标
     */
    private int downY;
    /**
     * 临时存X坐标
     */
    private int tempX;

    /**
     * 临时存Y坐标
     */
    private int tempY;
    private int viewWidth;
    private int viewHeight;
    /**
     * 是否正在滑动
     */
    private boolean isSliding;

    private OnSlidingFinishListener onSlidingFinishListener;
    private boolean isFinish;

    public SlideFinishLayout(Context context) {
        super(context);
        init();
    }

    public SlideFinishLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mScroller = new Scroller(getContext());
    }

    public interface OnSlidingFinishListener {
        /**
         * 滑动销毁页面回调
         */
        void onSlidingFinish();
    }

    public void setOnSlidingFinishListener(OnSlidingFinishListener onSlidingFinishListener) {
        this.onSlidingFinishListener = onSlidingFinishListener;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            // 获取SlidingFinishLayout布局的父布局
            mParentView = (ViewGroup) this.getParent();
            viewWidth = this.getWidth();
            viewHeight = this.getHeight();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = tempX = (int) event.getRawX();
                downY = tempY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) event.getRawX();
                int moveY = (int) event.getRawY();
                int deltaX = tempX - moveX;
                int deltaY = tempY - moveY;
                tempX = moveX;
                tempY = moveY;
                if (Math.abs(moveX - downX) > mTouchSlop || Math.abs(moveY - downY) > mTouchSlop) {
                    isSliding = true;
                }

                if ((moveX - downX >= 0 || downY - moveY >= 0) && isSliding) {
                    mParentView.scrollBy(deltaX, deltaY);
                }
                break;
            case MotionEvent.ACTION_UP:
                isSliding = false;
                if (mParentView.getScrollX() <= -viewWidth / 4) {
                    isFinish = true;
                    scrollRight();
                } else if (mParentView.getScrollY() >= viewHeight / 4) {
                    isFinish = true;
                    scrollTop();
                } else {
                    scrollOrigin();
                    isFinish = false;
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void scrollTop() {
        final int delta = (viewHeight + mParentView.getScrollY());
        //滚动出界面
        mScroller.startScroll(0, -delta + 1, 0, mParentView.getScrollY(), Math.abs(delta));
        postInvalidate();
    }

    private void scrollRight() {
        final int delta = (viewWidth + mParentView.getScrollX());
        //滚动出界面
        mScroller.startScroll(mParentView.getScrollX(), 0, -delta + 1, 0, Math.abs(delta));
        postInvalidate();
    }

    private void scrollOrigin() {
        int delta = mParentView.getScrollX();
        //滚动到起始位置
        mScroller.startScroll(mParentView.getScrollX(), 0, -delta, 0, Math.abs(delta));
        postInvalidate();
    }

    @Override
    public void computeScroll() {
        // 调用startScroll的时候scroller.computeScrollOffset()返回true，
        if (mScroller.computeScrollOffset()) {
            mParentView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();

            if (mScroller.isFinished()) {
                if (onSlidingFinishListener != null && isFinish) {
                    onSlidingFinishListener.onSlidingFinish();
                }
            }
        }
    }
}
