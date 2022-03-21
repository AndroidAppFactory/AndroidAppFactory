package com.bihe0832.android.lib.text.span;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.style.ReplacementSpan;

public class ZixieTextRadiusBackgroundSpan extends ReplacementSpan {

    private final static int DEFAULT_REDIUS = 8;
    private final static int DEFAULT_PADDING_TOP = 8;
    private final static int DEFAULT_PADDING_LEFT = 12;


    private int mTextDataLength;
    private int mBackgroundColor = Color.TRANSPARENT;
    private int mRadius = DEFAULT_REDIUS;
    private int mPaddingTop = DEFAULT_PADDING_TOP;
    private int mPaddingLeft = DEFAULT_PADDING_LEFT;
    private int mTextSize = 0;
    private int mTextColor = 0;
    private Typeface mTypeface = null;


    public ZixieTextRadiusBackgroundSpan(int bgColor) {
        this(bgColor, DEFAULT_REDIUS);
    }

    public ZixieTextRadiusBackgroundSpan(int bgColor, int radius) {
        this(bgColor, radius, 0);
    }

    public ZixieTextRadiusBackgroundSpan(int bgColor, int radius, int textColor) {
        this(bgColor, radius, DEFAULT_PADDING_LEFT, DEFAULT_PADDING_TOP, 0, textColor, null);
    }

    public ZixieTextRadiusBackgroundSpan(int bgColor, int radius, int textColor, Typeface mTypeface) {
        this(bgColor, radius, DEFAULT_PADDING_LEFT, DEFAULT_PADDING_TOP, 0, textColor, mTypeface);
    }

    public ZixieTextRadiusBackgroundSpan(int bgColor, int radius, int paddingLeft, int paddingTop) {
        this(bgColor, radius, paddingLeft, paddingTop, 0, 0, null);

    }

    public ZixieTextRadiusBackgroundSpan(int bgColor, int radius, int paddingLeft, int paddingTop, int textSize, int textColor, Typeface typeface) {
        initData(bgColor, radius, paddingLeft, paddingTop, textSize, textColor, typeface);
    }

    private void initData(int bgColor, int radius, int paddingLeft, int paddingTop, int textSize, int textColor, Typeface typeface) {
        mBackgroundColor = bgColor;
        mRadius = radius;
        mPaddingTop = paddingTop;
        mPaddingLeft = paddingLeft;
        mTextSize = textSize;
        mTextColor = textColor;
        mTypeface = typeface;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        Paint.FontMetricsInt fms = paint.getFontMetricsInt();


        if (mTextSize > 0 && paint.getTextSize() >= mTextSize) {
            paint.setTextSize(mTextSize);
        }
        mTextDataLength = (int) (paint.measureText(text, start, end) + 2 * mRadius + 2 * mPaddingLeft);
        return mTextDataLength;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        int color = paint.getColor();
        Typeface typeface = paint.getTypeface();
        Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        int textHeight = fm.descent - fm.ascent;
        int cent = fm.descent + fm.ascent;
        //字号过大，cent为负数，字号过小，cent为正
        int halfCenter = cent / 2;
        if (cent < 0) {
            halfCenter = (-cent) / 2;
        }

        int borderMinTop = top + cent;
        if (cent < 0) {
            borderMinTop = top + halfCenter;
        }
        int borderMaxBottom = y + cent;
        if (cent < 0) {
            borderMaxBottom = y + halfCenter;
        }

        canvas.save();
        canvas.drawLine(x + mRadius + mPaddingLeft, borderMinTop, 100, borderMinTop, paint);
        paint.setColor(Color.BLUE);
        canvas.drawLine(x + mRadius + mPaddingLeft, borderMaxBottom, 100, borderMaxBottom, paint);

        if (mTextSize > 0 && paint.getTextSize() >= mTextSize) {
            paint.setTextSize(mTextSize);
        }
        paint.setColor(mBackgroundColor);//设置背景颜色
        paint.setAntiAlias(true);// 设置画笔的锯齿效果

        int space = (borderMaxBottom - borderMinTop - textHeight) / 2;

        int textBottom = y;
        if (space > 0) {
            textBottom = borderMaxBottom - space;
        }


        int readpadding = space;
        if (space > mPaddingTop) {
            readpadding = mPaddingTop;
        }
        int borderRealBottom1;
        if (cent < 0) {
            borderRealBottom1 = textBottom + halfCenter;
        } else {
            borderRealBottom1 = textBottom - halfCenter;
        }
        int borderRealBottom;
        if (readpadding < 0) {
            borderRealBottom = (-readpadding) + borderRealBottom1;
        } else {
            borderRealBottom = readpadding + borderRealBottom1;

        }
        int borderRealTop;
        if (readpadding < 0) {
            borderRealTop = textBottom - textHeight + readpadding;
        } else {
            borderRealTop = textBottom - textHeight - readpadding;
        }

        RectF oval = new RectF(x + mPaddingLeft, borderRealTop, x + mTextDataLength - mPaddingLeft, borderRealBottom);
        //设置文字背景矩形，x为span其实左上角相对整个TextView的x值，y为span左上角相对整个View的y值。paint.ascent()获得文字上边缘，paint.descent()获得文字下边缘
        canvas.drawRoundRect(oval, mRadius, mRadius, paint);//绘制圆角矩形，第二个参数是x半径，第三个参数是y半径

//        canvas.drawLine(x + mRadius + mPaddingLeft, textBottom, 100, textBottom, paint);
//        paint.setColor(Color.BLUE);
//        canvas.drawLine(x + mRadius + mPaddingLeft, y, 100, y, paint);

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
        canvas.drawText(text, start, end, x + mRadius + mPaddingLeft, textBottom, paint);//绘制文字
        canvas.save();
        canvas.restore();
    }
}
