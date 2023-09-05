package com.bihe0832.android.lib.color.picker.base;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import com.bihe0832.android.lib.color.picker.OnColorSelectedListener;

public abstract class BaseColorPickerView extends BasePickerView {

    /**
     * 放大镜paint
     */
    protected Paint mMirrorLinePaint;
    /**
     * 控件中心点X坐标
     */
    protected int mCenterX;
    /**
     * 控件中心点Y坐标
     */
    protected int mCenterY;
    /**
     * 当前选中的颜色
     */
    protected int mCurrentColor = -1;
    /**
     * 颜色放大镜的半径
     */
    protected int mZoomRadius = 40;
    /**
     * 是否有颜色放大镜
     */
    protected boolean mHasZoom = true;
    /**
     * 颜色选择监听器
     */
    protected OnColorSelectedListener mListener;

    public BaseColorPickerView(Context context) {
        super(context);
    }

    public BaseColorPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseColorPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();
        mMirrorLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMirrorLinePaint.setStyle(Paint.Style.FILL);
    }

    public int getZoomRadius() {
        return mZoomRadius;
    }

    public void setZoomRadius(int radius) {
        if (radius <= 0 || mZoomRadius == radius) {
            return;
        }
        this.mZoomRadius = radius;
        postInvalidate();
    }

    public boolean getHasScaleMirror() {
        return mHasZoom;
    }

    public void setHasScaleMirror(boolean hasScaleMirror) {
        if (this.mHasZoom == hasScaleMirror) {
            return;
        }
        this.mHasZoom = hasScaleMirror;
        postInvalidate();
    }

    /**
     * 画放大镜
     */
    protected void drawZoom(Canvas canvas) {
        if ((mTouchX == 0 && mTouchY == 0) || mIsRelease || mCurrentColor == -1 || !mHasZoom) {
            return;
        }
        float cx = mTouchX + mZoomRadius * 3;
        float cy = mTouchY - mZoomRadius * 3;

        if (cx < mZoomRadius) {
            cx = mZoomRadius;
        }
        if (cx > getWidth() - mZoomRadius) {
            cx = getWidth() - mZoomRadius;
        }

        if (cy < mZoomRadius) {
            cy = mZoomRadius;
        }
        if (cy > getHeight() - mZoomRadius) {
            cy = getHeight() - mZoomRadius;
        }
        mPaint.setStyle(Style.FILL);
        mPaint.setColor(mCurrentColor);
        canvas.drawCircle(cx, cy, mZoomRadius, mPaint);
        mPaint.setStyle(Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(2);
        canvas.drawCircle(cx, cy, mZoomRadius, mPaint);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        mListener = listener;
    }
}
