package com.bihe0832.android.lib.ui.textview.hint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;


public class HintTextView extends AppCompatTextView {

    private Paint mPaint;

    private int mWidth;
    private LinearGradient mGradient = null;

    private int[] mColors = null;
    private float[] mPosition = null;

    private Shader.TileMode mTile = Shader.TileMode.CLAMP;
    private Matrix mMatrix = null;
    /**
     * 渐变的速度
     */
    private int deltaX = 0;

    public HintTextView(Context context) {
        super(context, null);
        mPaint = getPaint();
    }

    public HintTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = getPaint();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mWidth == 0) {
            mWidth = getMeasuredWidth();
            if (null != mColors) {
                mGradient = new LinearGradient(0, 0, mWidth, 0, mColors, mPosition, mTile);
            }
            //颜色渐变器
            mMatrix = new Matrix();
        }
    }

    public void setLinearGradientColors(int[] colors) {
        mColors = colors;
    }

    public void setLinearGradientPositions(float[] positions) {
        mPosition = positions;
    }

    public void setLinearGradientTileMode(Shader.TileMode tileMode) {
        mTile = tileMode;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mMatrix != null) {
            deltaX += mWidth / 8;
            if (deltaX > 2 * mWidth) {
                deltaX = -mWidth;
            }
            //通过矩阵的平移实现
            mMatrix.setTranslate(deltaX, 0);
            if (mGradient != null) {
                mPaint.setShader(mGradient);
                mGradient.setLocalMatrix(mMatrix);
            }
        }
        postInvalidateDelayed(100);
    }
}