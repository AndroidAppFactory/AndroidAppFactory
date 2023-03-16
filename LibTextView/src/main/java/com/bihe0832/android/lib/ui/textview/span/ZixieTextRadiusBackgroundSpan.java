package com.bihe0832.android.lib.ui.textview.span;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.style.ReplacementSpan;

public class ZixieTextRadiusBackgroundSpan extends ReplacementSpan {

    private final static int DEFAULT_REDIUS = 8;
    private final static int DEFAULT_MARGIN = 12;
    private final static int DEFAULT_PADDING_LEFT = 12;
    private final static int DEFAULT_PADDING_TOP = 8;
    private final static float DEFAULT_TEXT_SIZE = 0f;
    private final static int DEFAULT_TEXT_COLOR = 0;


    private int mTextDataLength;
    private int mBackgroundColor = Color.TRANSPARENT;
    private int mRadius = DEFAULT_REDIUS;
    private int mStrokeWidth = 0;
    private int mStrokeColor = Color.TRANSPARENT;
    private int mMargin = DEFAULT_PADDING_LEFT;
    private int mPaddingLeft = DEFAULT_PADDING_LEFT;
    private int mPaddingTop = DEFAULT_PADDING_TOP;
    private float mTextSize = DEFAULT_TEXT_SIZE;
    private int mTextColor = DEFAULT_TEXT_COLOR;
    private Typeface mTypeface = null;


    public ZixieTextRadiusBackgroundSpan(int bgColor) {
        this(bgColor, DEFAULT_REDIUS);
    }

    public ZixieTextRadiusBackgroundSpan(int bgColor, int radius) {
        this(bgColor, radius, DEFAULT_TEXT_SIZE);
    }

    public ZixieTextRadiusBackgroundSpan(int bgColor, int radius, float textSize) {
        this(bgColor, bgColor, 0, radius, DEFAULT_MARGIN, DEFAULT_PADDING_LEFT, DEFAULT_PADDING_TOP, textSize, DEFAULT_TEXT_COLOR,
                null);
    }

    public ZixieTextRadiusBackgroundSpan(int bgColor, int radius, int paddingLeft, int paddingTop) {
        this(bgColor, bgColor, 0, radius, DEFAULT_MARGIN, paddingLeft, paddingTop, DEFAULT_TEXT_SIZE, DEFAULT_TEXT_COLOR, null);
    }

    public ZixieTextRadiusBackgroundSpan(int bgColor, int radius, int textSize, int textColor, Typeface mTypeface) {
        this(bgColor, bgColor, 0, radius, DEFAULT_MARGIN, DEFAULT_PADDING_LEFT, DEFAULT_PADDING_TOP, textSize, textColor,
                mTypeface);
    }

    public ZixieTextRadiusBackgroundSpan(int bgColor,
                                         int strokeColor,
                                         int strokeWidth,
                                         int radius,
                                         int margin,
                                         int paddingLeft,
                                         int paddingTop,
                                         float textSize,
                                         int textColor, Typeface typeface) {
        initData(bgColor, strokeColor, strokeWidth, radius, margin, paddingLeft, paddingTop, textSize, textColor, typeface);
    }

    private void initData(
            int bgColor,
            int strokeColor,
            int strokeWidth,
            int radius,
            int margin,
            int paddingLeft,
            int paddingTop,
            float textSize,
            int textColor,
            Typeface typeface) {
        mBackgroundColor = bgColor;
        mStrokeColor = strokeColor;
        mStrokeWidth = strokeWidth;
        mRadius = radius;
        mMargin = margin;
        mPaddingLeft = paddingLeft;
        mPaddingTop = paddingTop;
        mTextSize = textSize;
        mTextColor = textColor;
        mTypeface = typeface;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        float textSize = paint.getTextSize();
        if (mTextSize > 0 && paint.getTextSize() >= mTextSize) {
            paint.setTextSize(mTextSize);
        }
        mTextDataLength = (int) (paint.measureText(text, start, end) + 2 * mRadius + 2 * mPaddingLeft + 2 * mMargin);
        paint.setTextSize(textSize);
        return mTextDataLength;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom,
                     Paint paint) {
        canvas.save();
        int color = paint.getColor();
        if (mTextSize > 0 && paint.getTextSize() >= mTextSize) {
            paint.setTextSize(mTextSize);
        }
        Typeface typeface = paint.getTypeface();
        Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        float textTop = fm.top;//为基线到字体上边框的距离,即上图中的top
        float textBottom = fm.bottom;//为基线到字体下边框的距离,即上图中的bottom
        float viewCenterY = top + (bottom - top) / 2;
        int baseLineY = (int) (viewCenterY - textTop / 2 - textBottom / 2);//基线中间点的y轴计算公式
        //paint.ascent()获得文字上边缘，paint.descent()获得文字下边缘
        int textHeight = fm.descent - fm.ascent;

        float totalExtraSpace = bottom - top - textHeight;
        float borderMaxSpace = totalExtraSpace / 2;
        if (borderMaxSpace < 0) {
            borderMaxSpace = 0;
        }

        float extraSpace = 0;
        if (borderMaxSpace > mPaddingTop) {
            extraSpace = borderMaxSpace - mPaddingTop;
        }

        float borderRealTop = top + extraSpace;
        float borderRealBottom = bottom - extraSpace;
        RectF oval = new RectF(x + mMargin, borderRealTop, x + mTextDataLength - mMargin, borderRealBottom);
        //设置文字背景矩形，x为span其左上角相对整个TextView的x值，y为span左上角相对整个View的y值。
        paint.setColor(mBackgroundColor);//设置背景颜色
        paint.setAntiAlias(true);// 设置画笔的锯齿效果
        canvas.drawRoundRect(oval, mRadius, mRadius, paint);//绘制圆角矩形，第二个参数是x半径，第三个参数是y半径
        if (mStrokeWidth > 0) {
            Paint strokePaint = new Paint();
            paint.setAntiAlias(true);
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(mStrokeWidth);
            strokePaint.setColor(mStrokeColor);
            canvas.drawRoundRect(oval, mRadius, mRadius, strokePaint);
        }

        if (mTextColor != 0) {
            paint.setColor(mTextColor);
        } else {
            paint.setColor(color);
        }

        if (mTypeface != null) {
            paint.setTypeface(mTypeface);
        } else {
            paint.setTypeface(typeface);
        }
        canvas.drawText(text.toString().substring(start, end), x + mMargin + mRadius + mPaddingLeft, baseLineY, paint);

        canvas.save();
        canvas.restore();
    }
}
