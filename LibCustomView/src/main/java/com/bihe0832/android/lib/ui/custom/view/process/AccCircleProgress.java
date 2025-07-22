package com.bihe0832.android.lib.ui.custom.view.process;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.bihe0832.android.lib.ui.custom.view.R;

/**
 * @author zixie code@bihe0832.com
 * Created on 2023/2/4.
 * Description: Description
 */
public class AccCircleProgress extends View {

    private static final String TAG = "AccCircleProgress";

    //画笔
    private Paint mPaint;
    //绘图范围
    private RectF mArcScopeRectF;

    private int mCurrentProcess = 1;
    private int mMax = 100;

    //圆环的宽度
    private float mCircleWidth = 30;
    private int mCircleBackgroundColor = getResources().getColor(R.color.md_theme_outline);

    //圆弧的宽度
    private float mArcWidth = 10;
    private int[] mArcBackgroundColor = null;

    //控件的宽度
    private float mViewWidth = 0;

    //Icon 的大小
    private float mIconWidth = 80;
    private int mIconRes = -1;


    public AccCircleProgress(Context context) {
        this(context, null);
    }

    public AccCircleProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AccCircleProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mArcScopeRectF = new RectF();
    }

    public void serCurrentProcess(int _current) {
        Log.i(TAG, "当前值：" + _current + "，最大值：" + mMax);
        this.mCurrentProcess = _current;
        invalidate();
    }

    public void setMax(int _max) {
        this.mMax = _max;
    }

    public void setArcWidth(float widthPx) {
        this.mArcWidth = widthPx;
    }

    public void setCircleWidth(float mCircleWidth) {
        this.mCircleWidth = mCircleWidth;
    }

    public void setIconWidth(float widthPx) {
        this.mIconWidth = widthPx;
    }

    public void setIconRes(int mIconRes) {
        this.mIconRes = mIconRes;
    }

    public void setCircleBackgroundColor(int mCircleBackgroundColor) {
        this.mCircleBackgroundColor = mCircleBackgroundColor;
    }

    public void setArcBackgroundColor(int[] mArcBackgroundColor) {
        this.mArcBackgroundColor = mArcBackgroundColor;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //getMeasuredWidth获取的是view的原始大小，也就是xml中配置或者代码中设置的大小
        //getWidth获取的是view最终显示的大小，这个大小不一定等于原始大小
        mViewWidth = getMeasuredWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float maxCircleWidth = Math.max(mIconWidth, Math.max(mArcWidth, mCircleWidth));

        //圆弧外圈中心圆半径
        float arcCircleRadius = (mViewWidth - maxCircleWidth) / 2;
        //圆心
        float cx = arcCircleRadius + maxCircleWidth / 2;
        float cy = arcCircleRadius + maxCircleWidth / 2;

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mCircleWidth);
        mPaint.setColor(mCircleBackgroundColor);
        //绘制背景圆
        canvas.drawCircle(cx, cy, arcCircleRadius, mPaint);

        //当前进度对对应的角度
        float angle = mCurrentProcess * 360 / mMax;
        //圆弧外边缘的区域
        mArcScopeRectF.set(cx - arcCircleRadius, cy - arcCircleRadius, cx + arcCircleRadius, cy + arcCircleRadius);
        try {
            Shader arcBackgroundShader = new SweepGradient(cx, cy, mArcBackgroundColor, null);
            mPaint.setShader(arcBackgroundShader);
            Matrix matrix = new Matrix();
            matrix.setRotate(-90, getMeasuredWidth() / 2, getMeasuredHeight() / 2);
            arcBackgroundShader.setLocalMatrix(matrix);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //绘制圆弧
        mPaint.setStrokeWidth(mArcWidth);
        canvas.drawArc(mArcScopeRectF, -90, angle, false, mPaint);
        mPaint.setShader(null);
        //获取图标
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), mIconRes);
        if (bitmap != null) {
            Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            // 图标的目的位置
            double icx = cx - Math.cos(Math.toRadians(angle + 90)) * (arcCircleRadius);
            double icy = cy - Math.sin(Math.toRadians(angle + 90)) * (arcCircleRadius);
            Rect dst = new Rect((int) (icx - mIconWidth / 2), (int) (icy - mIconWidth / 2), (int) (icx + mIconWidth / 2), (int) (icy + mIconWidth / 2));
            canvas.drawBitmap(bitmap, src, dst, mPaint);
        }
    }

}