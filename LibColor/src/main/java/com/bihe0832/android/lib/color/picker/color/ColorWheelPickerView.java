package com.bihe0832.android.lib.color.picker.color;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import com.bihe0832.android.lib.color.picker.base.BaseColorPickerView;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

public class ColorWheelPickerView extends BaseColorPickerView {

    /**
     * 默认大小，使用dp作为单位
     */
    private static final int DEFAULT_WIDTH_DP = 300;
    /**
     * 默认大小，使用dp作为单位
     */
    private static final int DEFAULT_HEIGHT_DP = 300;
    private float radius;
    private Paint huePaint;
    private Paint saturationPaint;
    /**
     * 控件高
     */
    private int mHeight;
    /**
     * 控件宽
     */
    private int mWidth;

    public ColorWheelPickerView(Context context) {
        super(context);
    }

    public ColorWheelPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorWheelPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void init() {
        super.init();
        huePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        saturationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void computeCurrent() {
        mCurrentColor = getColorAtPoint(mTouchX, mTouchY);
        if (mListener == null) {
            return;
        }
        if (mIsRelease) {
            if (mCurrentColor != -1) {
                mListener.onColorSelecting(mCurrentColor);
            }
        } else {
            if (mCurrentColor != -1) {
                mListener.onColorSelected(mCurrentColor);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int netWidth = w - getPaddingLeft() - getPaddingRight();
        int netHeight = h - getPaddingTop() - getPaddingBottom();
        radius = Math.min(netWidth, netHeight) * 0.5f;
        if (radius < 0) {
            return;
        }
        mCenterX = (int) (w * 0.5f);
        mCenterY = (int) (h * 0.5f);

        Shader hueShader = new SweepGradient(mCenterX, mCenterY,
                new int[]{Color.RED, Color.MAGENTA, Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED},
                null);
        huePaint.setShader(hueShader);

        Shader saturationShader = new RadialGradient(mCenterX, mCenterY, radius,
                Color.WHITE, 0x00FFFFFF, Shader.TileMode.CLAMP);
        saturationPaint.setShader(saturationShader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mCenterX, mCenterY, radius, huePaint);
        canvas.drawCircle(mCenterX, mCenterY, radius, saturationPaint);
        drawZoom(canvas);
    }

    /**
     * 获取触点与圆心的距离
     */
    private double getDistanceFromCenter() {
        float factor = (mTouchX - mCenterX) * (mTouchX - mCenterX);
        factor += (mTouchY - mCenterY) * (mTouchY - mCenterY);
        return Math.sqrt(factor);
    }

    /**
     * 获取整个色环的半径，取宽和高中最小值的二分之一减去8像素
     */
    private int getBigCircleRadius() {
        int radius = mWidth > mHeight ? mHeight / 2 : mWidth / 2;
        return radius - 8;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int defaultWidth = DisplayUtil.dip2px(getContext(), DEFAULT_WIDTH_DP);
        int defaultHeight = DisplayUtil.dip2px(getContext(), DEFAULT_HEIGHT_DP);
        if (widthMode == MeasureSpec.UNSPECIFIED
                || widthMode == MeasureSpec.AT_MOST) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(defaultWidth,
                    MeasureSpec.EXACTLY);
            mWidth = defaultWidth;
        } else {
            mWidth = widthSize;
        }

        if (heightMode == MeasureSpec.UNSPECIFIED
                || heightMode == MeasureSpec.AT_MOST) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(defaultHeight,
                    MeasureSpec.EXACTLY);
            mHeight = defaultHeight;
        } else {
            mHeight = heightSize;
        }

        mCenterX = mWidth / 2;
        mCenterY = mHeight / 2;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int getColorAtPoint(float eventX, float eventY) {
        float x = eventX - mCenterX;
        float y = eventY - mCenterY;
        double r = Math.sqrt(x * x + y * y);
        float[] hsv = {0, 0, 1};
        hsv[0] = (float) (Math.atan2(y, -x) / Math.PI * 180f) + 180;
        hsv[1] = Math.max(0f, Math.min(1f, (float) (r / radius)));
        return Color.HSVToColor(hsv);
    }
}
