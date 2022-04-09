package com.bihe0832.android.lib.text.span;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;


public class ZixieTextImageSpan extends ImageSpan {

    private final static int DEFAULT_MARGIN = 12;
    private int mMargin = DEFAULT_MARGIN;

    public ZixieTextImageSpan(Context context, final int drawableRes) {
        super(context, drawableRes);
    }

    public ZixieTextImageSpan(Context context, final int drawableRes, int margin) {
        super(context, drawableRes);
        mMargin = margin;
    }


    public ZixieTextImageSpan(Context context, final Bitmap map) {
        super(context, map);
    }

    public ZixieTextImageSpan(Context context, final Bitmap map, int margin) {
        super(context, map);
        mMargin = margin;
    }


    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {

        try {
            Paint.FontMetricsInt paintFm = paint.getFontMetricsInt();
            int textHeight = paintFm.descent - paintFm.ascent;

            Drawable b = getDrawable();

            int sourceBitmapWidth = b.getBounds().width();
            int sourceBitmapHeight = b.getBounds().height();
            int newBitmapWidth = sourceBitmapWidth * textHeight / sourceBitmapHeight;

            return newBitmapWidth + 1 + mMargin * 2;
        } catch (Exception e) {
            return super.getSize(paint, text, start, end, fm);
        }
    }


    /**
     * x，要绘制的左边框到textview左边框的距离。
     * y，要替换的文字的基线坐标，即基线到textview上边框的距离。
     * top，顶部位置。
     * bottom，最底部位置。注意，textview中两行之间的行间距是属于上一行的，所以这里bottom是指行间隔的底部位置。
     * paint，画笔，包含了要绘制字体的度量信息。
     * <p>
     * getDrawable获取要绘制的image，getBounds是获取包裹image的矩形框尺寸；
     * y + fm.descent得到字体的descent线坐标；
     * y + fm.ascent得到字体的ascent线坐标；
     * 两者相加除以2就是两条线中线的坐标；
     * b.getBounds().bottom是image的高度（试想把image放到原点），除以2即高度一半；
     * 前面得到的中线坐标减image高度的一半就是image顶部要绘制的目标位置；
     * 最后把目标坐标传递给canvas.translate函数就可以了，至于这个函数的理解先不管了
     */
    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom,
            Paint paint) {
        try {
            Drawable drawable = getDrawable();
            Bitmap b = ((BitmapDrawable) drawable).getBitmap();
            Paint.FontMetricsInt fm = paint.getFontMetricsInt();
            int textHeight = fm.descent - fm.ascent;
            int sourceBitmapWidth = b.getWidth();
            int sourceBitmapHeight = b.getHeight();

            int newBitmapWidth = sourceBitmapWidth * textHeight / sourceBitmapHeight;
            Bitmap bitmapResized = Bitmap.createScaledBitmap(b, newBitmapWidth, textHeight, false);
            canvas.save();
            float transY = y + (fm.descent + fm.ascent) / 2 - textHeight / 2;
            canvas.drawBitmap(bitmapResized, (int) x + mMargin, transY, paint);
            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
