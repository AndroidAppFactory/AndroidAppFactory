package com.bihe0832.android.common.image.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import android.os.Build;
import android.renderscript.RSRuntimeException;
import android.support.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import com.bihe0832.android.lib.utils.os.BuildUtils;
/**
 * @author hardyshi code@bihe0832.com Created on 2022/3/28.
 */
public class BlurTransformation extends BitmapTransformation {

    private static String STRING_CHARSET_NAME = "UTF-8";
    private static final String ID = "com.bihe0832.glide.blurTransformation";
    private static Charset CHARSET = Charset.forName(STRING_CHARSET_NAME);
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    private static int MAX_RADIUS = 25;
    private static int DEFAULT_DOWN_SAMPLING = 1;

    private Context mContext;
    private BitmapPool mBitmapPool;

    private int mRadius;
    private int mSampling;

    public BlurTransformation(Context context) {
        this(context, Glide.get(context).getBitmapPool(), MAX_RADIUS, DEFAULT_DOWN_SAMPLING);
    }

    public BlurTransformation(Context context, BitmapPool pool) {
        this(context, pool, MAX_RADIUS, DEFAULT_DOWN_SAMPLING);
    }

    public BlurTransformation(Context context, BitmapPool pool, int radius) {
        this(context, pool, radius, DEFAULT_DOWN_SAMPLING);
    }

    public BlurTransformation(Context context, int radius) {
        this(context, Glide.get(context).getBitmapPool(), radius, DEFAULT_DOWN_SAMPLING);
    }

    public BlurTransformation(Context context, int radius, int sampling) {
        this(context, Glide.get(context).getBitmapPool(), radius, sampling);
    }

    public BlurTransformation(Context context, BitmapPool pool, int radius, int sampling) {
        mContext = context.getApplicationContext();
        mBitmapPool = pool;
        mRadius = radius;
        mSampling = sampling;
    }


    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int
            outWidth, int outHeight) {
        Bitmap source = toTransform;

        int width = source.getWidth();
        int height = source.getHeight();
        int scaledWidth = width / mSampling;
        int scaledHeight = height / mSampling;

        Bitmap bitmap = mBitmapPool.get(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        canvas.scale(1 / (float) mSampling, 1 / (float) mSampling);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(source, 0, 0, paint);

        if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            try {
                bitmap = RSBlur.blur(mContext, bitmap, mRadius);
            } catch (RSRuntimeException e) {
                bitmap = FastBlur.blur(bitmap, mRadius, true);
            }
        } else {
            bitmap = FastBlur.blur(bitmap, mRadius, true);
        }

        //return BitmapResource.obtain(bitmap, mBitmapPool);
        return bitmap;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlurTransformation;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }

}