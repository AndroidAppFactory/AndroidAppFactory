package com.bihe0832.android.lib.color.picker.alpha;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AlphaGridDrawable extends Drawable {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int mSize;
    private final int mColorOdd;
    private final int mColorEven;

    public AlphaGridDrawable() {
        this.mSize = 20;
        this.mColorOdd = 0xFFC2C2C2;
        this.mColorEven = 0xFFFFFFF;
        configurePaint();
    }

    public AlphaGridDrawable(int size, int colorOdd, int colorEven) {
        this.mSize = size;
        this.mColorOdd = colorOdd;
        this.mColorEven = colorEven;
        configurePaint();
    }

    private void configurePaint() {
        Bitmap bitmap = Bitmap.createBitmap(mSize * 2, mSize * 2, Bitmap.Config.ARGB_8888);

        Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bitmapPaint.setStyle(Paint.Style.FILL);

        Canvas canvas = new Canvas(bitmap);

        Rect rect = new Rect(0, 0, mSize, mSize);
        bitmapPaint.setColor(mColorOdd);
        canvas.drawRect(rect, bitmapPaint);

        rect.offset(mSize, mSize);
        canvas.drawRect(rect, bitmapPaint);

        bitmapPaint.setColor(mColorEven);
        rect.offset(-mSize, 0);
        canvas.drawRect(rect, bitmapPaint);

        rect.offset(mSize, -mSize);
        canvas.drawRect(rect, bitmapPaint);

        paint.setShader(new BitmapShader(bitmap, BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT));
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawPaint(paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
