package com.bihe0832.android.lib.media.image;

import static com.bihe0832.android.lib.media.image.BitmapUtil.TAG;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import com.bihe0832.android.lib.log.ZLog;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BitmapTransUtils {

    public static Bitmap compress(Bitmap image, long targetSize) {

        final int size_length = 10;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(image.hasAlpha() ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, baos);
        if (size_length * targetSize > baos.toByteArray().length) {
            return image;
        }
        int options = 100;
        //循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while (options > 1 && baos.toByteArray().length > size_length * targetSize) {
            options = (int) (size_length * targetSize * 100f / baos.toByteArray().length);
            if (options < 0) {
                options = 0;
            }
            ZLog.d(TAG, "compress start source length " + baos.toByteArray().length + "; target length:"
                    + size_length * targetSize + "; options:" + options);
            //重置baos即清空baos
            baos.reset();
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            //这里压缩options%，把压缩后的数据存放到baos中
            image.compress(image.hasAlpha() ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, options, baos);
            ZLog.d(TAG, "compress end source length " + baos.toByteArray().length + "; target length:"
                    + size_length * targetSize + "; options:" + options);
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    //根据width 和 height 与 reqWidth 和 reqHeight 的差异，计算出如果缩放到一样大，使用的 BitmapFactory.Options
    public static int calculateInSampleSize(int reqWidth, int reqHeight, int width, int height, boolean centerInside) {
        int sampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio;
            final int widthRatio;
            if (reqHeight == 0) {
                if (reqWidth != 0) {
                    sampleSize = (int) Math.floor((float) width / (float) reqWidth);
                }
            } else {
                if (reqWidth == 0) {
                    sampleSize = (int) Math.floor((float) height / (float) reqHeight);
                } else {
                    heightRatio = (int) Math.floor((float) height / (float) reqHeight);
                    widthRatio = (int) Math.floor((float) width / (float) reqWidth);
                    sampleSize = centerInside ? Math.max(heightRatio, widthRatio) : Math.min(heightRatio, widthRatio);
                }
            }
        }
        return sampleSize;
    }


    /**
     * 水平镜像
     *
     * @param bmp
     * @return
     */
    public static Bitmap toHorizontalMirror(Bitmap bmp) {
        Matrix matrix = new Matrix();
        matrix.postScale(-1F, 1F);
        return transformBitmap(bmp, matrix);
    }

    public static Bitmap transformBitmap(@NonNull Bitmap bitmap, @NonNull Matrix transformMatrix) {
        try {
            Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), transformMatrix,
                    true);
            if (!bitmap.sameAs(converted)) {
                bitmap = converted;
            }
        } catch (OutOfMemoryError error) {
            ZLog.e(TAG, "transformBitmap: " + error);
        }
        return bitmap;
    }

    public static int getPictureRotateAngel(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }


    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm 需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return transformBitmap(bm, matrix);
    }

    /**
     * 把两个位图覆盖合成为一个位图，上下拼接
     *
     * @param isBaseMax 是否以高度大的位图为准，true则小图等比拉伸，false则大图等比压缩
     * @return
     */
    public static Bitmap mergeBitmapLine(Bitmap topBitmap, Bitmap bottomBitmap, boolean isBaseMax) {
        if (topBitmap == null || topBitmap.isRecycled() || bottomBitmap == null || bottomBitmap.isRecycled()) {
            ZLog.d(TAG, "topBitmap=" + topBitmap + ";bottomBitmap=" + bottomBitmap);
            return null;
        }
        int width = 0;
        if (isBaseMax) {
            width = topBitmap.getWidth() > bottomBitmap.getWidth() ? topBitmap.getWidth() : bottomBitmap.getWidth();
        } else {
            width = topBitmap.getWidth() < bottomBitmap.getWidth() ? topBitmap.getWidth() : bottomBitmap.getWidth();
        }
        Bitmap tempBitmapT = topBitmap;
        Bitmap tempBitmapB = bottomBitmap;

        if (topBitmap.getWidth() != width) {
            tempBitmapT = Bitmap.createScaledBitmap(topBitmap, width,
                    (int) (topBitmap.getHeight() * 1f / topBitmap.getWidth() * width), false);
        } else if (bottomBitmap.getWidth() != width) {
            tempBitmapB = Bitmap.createScaledBitmap(bottomBitmap, width,
                    (int) (bottomBitmap.getHeight() * 1f / bottomBitmap.getWidth() * width), false);
        }

        int height = tempBitmapT.getHeight() + tempBitmapB.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Rect topRect = new Rect(0, 0, tempBitmapT.getWidth(), tempBitmapT.getHeight());
        Rect bottomRect = new Rect(0, 0, tempBitmapB.getWidth(), tempBitmapB.getHeight());

        Rect bottomRectT = new Rect(0, tempBitmapT.getHeight(), width, height);

        canvas.drawBitmap(tempBitmapT, topRect, topRect, null);
        canvas.drawBitmap(tempBitmapB, bottomRect, bottomRectT, null);
        return bitmap;
    }

    /**
     * 把两个位图覆盖合成为一个位图，叠加在一起，小图与大图居中对齐
     *
     * @param isBaseBottom 是否以底图大小为准，true则小图等比拉伸，false则不压缩
     * @return
     */
    public static Bitmap mergeBitmapTogether(Bitmap bottomBitmap, Bitmap iconBitmap, boolean isBaseBottom) {
        if (bottomBitmap == null || bottomBitmap.isRecycled() || iconBitmap == null || iconBitmap.isRecycled()) {
            ZLog.d(TAG, "topBitmap=" + bottomBitmap + ";bottomBitmap=" + iconBitmap);
            return null;
        }
        int width = iconBitmap.getWidth();
        int height = iconBitmap.getHeight();
        if (isBaseBottom) {
            width = bottomBitmap.getWidth() > iconBitmap.getWidth() ? bottomBitmap.getWidth() : iconBitmap.getWidth();
            height = (int) (bottomBitmap.getHeight() * 1f / bottomBitmap.getWidth() * width);
        }
        Bitmap tempBitmapIcon = iconBitmap;
        if (iconBitmap.getWidth() != width) {
            tempBitmapIcon = Bitmap.createScaledBitmap(iconBitmap, width, height, false);
        }

        Bitmap bitmap = Bitmap.createBitmap(bottomBitmap.getWidth(), bottomBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bottomBitmap, 0, 0, null);
        canvas.drawBitmap(tempBitmapIcon, (bottomBitmap.getWidth() - width) / 2,
                (bottomBitmap.getHeight() - height) / 2, null);
        return bitmap;
    }

    public static Bitmap getBitmapWithRound(Bitmap mBitmap, float roundIndex) {
        if (null == mBitmap) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        //设置矩形大小
        Rect rect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        RectF rectf = new RectF(rect);

        // 相当于清屏
        canvas.drawARGB(0, 0, 0, 0);
        //画圆角
        canvas.drawRoundRect(rectf, roundIndex, roundIndex, paint);
        // 取两层绘制，显示上层
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        // 把原生的图片放到这个画布上，使之带有画布的效果
        canvas.drawBitmap(mBitmap, rect, rect, paint);
        return bitmap;
    }

    /**
     * 获取当前View的bitmap并添加一个指定颜色的圆形浮层
     *
     * @param view
     * @param color
     * @return
     */
    public static Bitmap getImageBitmapWithCircleLayer(ImageView view, int color, float startAngle, float sweepAngle) {
        Bitmap originalBitmap = ((BitmapDrawable) (view).getDrawable()).getBitmap();
        return getBitmapWithCircleLayer(originalBitmap, color, startAngle, sweepAngle);
    }

    public static Bitmap getBitmapWithCircleLayer(Bitmap originalBitmap, int color, float startAngle,
            float sweepAngle) {
        if (null == originalBitmap) {
            return null;
        }
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        Bitmap updatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(updatedBitmap);

        canvas.drawBitmap(originalBitmap, 0, 0, null);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        RectF oval2 = new RectF(0, 0, width, width);// 设置个新的长方形，扫描测量
        canvas.drawArc(oval2, startAngle, sweepAngle, true, paint);
        return updatedBitmap;
    }

    /**
     * 获取当前View的bitmap并添加一个指定颜色的浮层
     *
     * @param view
     * @param color
     * @return
     */
    public static Bitmap getImageBitmapWithLayer(ImageView view, int color, boolean isBackground) {
        Bitmap originalBitmap = ((BitmapDrawable) (view).getDrawable()).getBitmap();
        if (null == originalBitmap) {
            originalBitmap = BitmapUtil.getViewBitmapData(view);
        }
        return getBitmapWithLayer(originalBitmap, color, isBackground);
    }

    public static Bitmap getBitmapWithLayer(Bitmap originalBitmap, int color, boolean isBackground) {
        if (null == originalBitmap) {
            return null;
        }
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        Bitmap updatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(updatedBitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);

        if (isBackground) {
            canvas.drawRect(0, 0, width, width, paint);
        }
        canvas.drawBitmap(originalBitmap, 0, 0, null);
        if (!isBackground) {
            canvas.drawRect(0, 0, width, width, paint);
        }

        return updatedBitmap;
    }
}
