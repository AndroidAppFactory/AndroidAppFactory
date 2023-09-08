package com.bihe0832.android.lib.color.picker.deep;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.bihe0832.android.lib.color.picker.ColorSlidePicker;
import com.bihe0832.android.lib.color.picker.base.BaseColorPickerView;
import com.bihe0832.android.lib.color.utils.ColorUtils;


public class DeepSlideView extends BaseColorPickerView implements ColorSlidePicker {

    /**
     * 默认基色
     */
    private int baseColor = Color.WHITE;
    private int startColor = Color.BLACK;
    private int endColor = Color.WHITE;

    private float mDepth = 0.5f;

    public DeepSlideView(Context context) {
        super(context);
    }

    public DeepSlideView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DeepSlideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        slideLinePaint.setStrokeWidth(slideLineWidth);
        slideLinePaint.setColor(slideLineColor);
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[2] = 0;
        startColor = Color.HSVToColor(hsv);
        hsv[2] = 1;
        endColor = Color.HSVToColor(hsv);
        Shader shader = new LinearGradient(0, 0, getWidth() * 1f, getHeight() * 1f, startColor, endColor,
                Shader.TileMode.CLAMP);
        mPaint.setShader(shader);
        if (mTouchX == -1 || mTouchY == -1) {
            setPosition(mDepth);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        slideLinePaint.setColor(slideLineColor);
        canvas.drawRect(0, 0, getWidth() * 1f, getHeight(), mPaint);
        drawSlideLine(canvas);
    }

    /**
     * 画指示线
     */
    private void drawSlideLine(Canvas canvas) {
        if (hasSlideLine) {
            if (mTouchX < slideLineWidth / 2) {
                mTouchX = slideLineWidth / 2;
            }
            if (mTouchX > getWidth() - slideLineWidth / 2) {
                mTouchX = getWidth() - slideLineWidth / 2;
            }
            canvas.drawLine(mTouchX, 0, mTouchX, getHeight(), slideLinePaint);
        }
    }

    /**
     * 设置基色
     */
    public void setBaseColor(@ColorInt int color) {
        this.baseColor = color;
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[2] = 0;
        int startColor = Color.HSVToColor(hsv);
        hsv[2] = 1;
        int endColor = Color.HSVToColor(hsv);
        mDepth = (float) (ColorUtils.getColorBrightness(baseColor) / ColorUtils.getColorBrightness(endColor));
        Shader shader = new LinearGradient(0, 0, getWidth() * 1f, getHeight() * 1f, startColor, endColor, Shader.TileMode.CLAMP);
        mPaint.setShader(shader);
        postInvalidate();
    }

    /**
     * 计算当前颜色
     */
    @Override
    protected void computeCurrent() {
        if (mCurrentColor == getColorAtPoint()) {
            return;
        }
        mCurrentColor = getColorAtPoint();
        if (mListener == null) {
            return;
        }
        if (mCurrentColor == baseColor || mCurrentColor == startColor || mCurrentColor == endColor) {
            performHapticFeedback(
                    HapticFeedbackConstants.LONG_PRESS,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            );
        }
        if (mIsRelease) {
            mListener.onColorSelected(mCurrentColor);
        } else {
            mListener.onColorSelecting(mCurrentColor);
        }
    }

    /**
     * 获取点对应的颜色
     */
    private int getColorAtPoint() {
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        if (getWidth() > 0) {
            hsv[2] = mTouchX * 1f / getWidth();
        }
        return Color.HSVToColor(hsv);
    }

    @Override
    public void setPosition(float ratio) {
        if (getMeasuredWidth() > 0) {
            mTouchX = getMeasuredWidth() * ratio;
        }
        mCurrentColor = getColorAtPoint();
        postInvalidate();
    }
}
