package com.bihe0832.android.lib.ui.custom.view.slide;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.customview.widget.ViewDragHelper;

/**
 * @author zixie code@bihe0832.com
 * Created on 2023/5/12.
 * Description: 仿滑动解锁等滑动特效，仅内部LockBtn 在范围内滑动
 */
public class SlideViewLayout extends FrameLayout {
    /**
     * 滑动滑块
     */
    private View mLockBtn;
    /**
     * 拽托帮助类
     */

    private ViewDragHelper mViewDragHelper;
    /**
     * 回调
     */
    private Callback mCallback;

    public SlideViewLayout(@NonNull Context context) {
        this(context, null);
    }

    public SlideViewLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideViewLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(View lockBtn, Callback callback) {
        mLockBtn = lockBtn;
        mCallback = callback;
        final SlideViewLayout slideRail = this;
        mViewDragHelper = ViewDragHelper.create(this, 0.3f, new ViewDragHelper.Callback() {
            private int mTop;
            private int mLeft;

            @Override
            public boolean tryCaptureView(@NonNull View child, int pointerId) {
                //判断能拽托的View，这里会遍历内部子控件来决定是否可以拽托，我们只需要滑块可以滑动
                return child == mLockBtn;
            }

            @Override
            public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
                //拽托子View横向滑动时回调，回调的left，则是可以滑动的左上角x坐标
                int lockBtnWidth = mLockBtn.getWidth();
                //限制左右临界点
                int fullWidth = slideRail.getWidth();
                //最少的左边
                int leftMinDistance = getPaddingStart();
                //最多的右边
                int leftMaxDistance = fullWidth - getPaddingEnd() - lockBtnWidth;
                //修复两端的临界值
                if (left < leftMinDistance) {
                    return leftMinDistance;
                } else if (left > leftMaxDistance) {
                    return leftMaxDistance;
                }
                return left;
            }

            @Override
            public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
                //拽托子View纵向滑动时回调，锁定顶部padding距离即可，不能不复写，否则少了顶部的padding，位置就偏去上面了
                return getPaddingTop();
            }

            @Override
            public void onViewCaptured(@NonNull View capturedChild, int activePointerId) {
                super.onViewCaptured(capturedChild, activePointerId);
                //捕获到拽托的View时回调，获取顶部距离
                mTop = capturedChild.getTop();
                mLeft = capturedChild.getLeft();
            }

            @Override
            public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                //获取滑块当前的位置
                int currentLeft = releasedChild.getLeft();
                //获取滑块的宽度
                int lockBtnWidth = mLockBtn.getWidth();
                //获取滑道宽度
                int fullWidth = slideRail.getWidth();
                //一般滑道的宽度，用来判断滑块距离起点近还是终点近
                int halfWidth = fullWidth * 2 / 3;
                int currentWidth = Math.abs(currentLeft - mLeft);
                //松手位置在小于一半，并且滑动速度小于1000，则回到左边
                if (currentWidth <= halfWidth) {
                    mViewDragHelper.settleCapturedViewAt(mLeft, mTop);
                    invalidate();
                } else {
                    if (currentLeft < mLeft) {
                        mViewDragHelper.settleCapturedViewAt(fullWidth - mLeft - lockBtnWidth, mTop);
                    } else {
                        mViewDragHelper.settleCapturedViewAt(fullWidth - getPaddingEnd() - lockBtnWidth, mTop);
                    }
                    invalidate();
                    if (mCallback != null) {
                        mCallback.onUnlock();
                    }
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //将onInterceptTouchEvent委托给ViewDragHelper
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //将onTouchEvent委托给ViewDragHelper
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        //判断是否移动到头了，未到头则继续
        if (mViewDragHelper != null) {
            if (mViewDragHelper.continueSettling(true)) {
                invalidate();
            }
        }
    }

    public interface Callback {
        /**
         * 当解锁时回调
         */
        void onUnlock();
    }

    public void reset() {
        FrameLayout.LayoutParams layoutParams = (LayoutParams) mLockBtn.getLayoutParams();
        mLockBtn.setLayoutParams(layoutParams);
    }
}
