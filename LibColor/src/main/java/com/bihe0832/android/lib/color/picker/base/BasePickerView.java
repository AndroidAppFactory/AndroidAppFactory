package com.bihe0832.android.lib.color.picker.base;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;


abstract class BasePickerView extends View {

    /**
     * 触摸点X坐标
     */
    protected float mTouchX = -1;
    /**
     * 触摸点Y坐标
     */
    protected float mTouchY = -1;

    /**
     * 自定义画笔，用于绘制控件
     */
    protected Paint mPaint;
    /**
     * 是否触点释放
     */
    protected boolean mIsRelease = false;

    public BasePickerView(Context context) {
        super(context);
        init();
    }

    public BasePickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BasePickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    /**
     * 初始化
     */
    protected void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    abstract protected void computeCurrent();


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        mTouchX = event.getX();
        mTouchY = event.getY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                mIsRelease = true;
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_DOWN:
                mIsRelease = false;
                break;
        }
        computeCurrent();
        if (mIsRelease) {
            postInvalidateDelayed(700);
        } else {
            postInvalidate();
        }
        return true;
    }
}
