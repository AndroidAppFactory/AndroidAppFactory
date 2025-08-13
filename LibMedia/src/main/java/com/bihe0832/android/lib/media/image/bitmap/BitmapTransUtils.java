package com.bihe0832.android.lib.media.image.bitmap;

import static com.bihe0832.android.lib.media.image.bitmap.BitmapUtil.TAG;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
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
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

    //根据width 和 height 与 maxWidth 和 maxHeight 的差异，计算出如果缩放到一样大，使用的 BitmapFactory.Options
    public static int calculateInSampleSize(int maxWidth, int maxHeight, int width, int height) {
        int inSampleSize = 1;
        if (height > maxHeight || width > maxWidth) {
            if (maxHeight == 0) {
                if (maxWidth != 0) {
                    inSampleSize = (int) Math.floor((float) width / (float) maxWidth);
                }
            } else {
                if (maxWidth == 0) {
                    inSampleSize = (int) Math.floor((float) height / (float) maxHeight);
                } else {
                    while ((height / inSampleSize) > maxHeight || (width / inSampleSize) > maxWidth) {
                        inSampleSize *= 2;
                    }
                }
            }
        }
        return inSampleSize;
    }

    public static Bitmap transformBitmap(Bitmap bitmap, Matrix transformMatrix) {
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

    public static Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees, boolean flipX, boolean flipY) {
        try {
            Matrix matrix = new Matrix();

            // Rotate the image back to straight.
            matrix.postRotate(rotationDegrees);

            // Mirror the image along the X or Y axis.
            matrix.postScale(flipX ? -1.0f : 1.0f, flipY ? -1.0f : 1.0f);
            return transformBitmap(bitmap, matrix);
        } catch (OutOfMemoryError error) {
            ZLog.e(TAG, "transformBitmap: " + error);
        }
        return bitmap;
    }

    public static int getPictureRotateAngelByExifOrientation(int orientation) {
        int rotationDegrees = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotationDegrees = 90;
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                rotationDegrees = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotationDegrees = 180;
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotationDegrees = -90;
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                rotationDegrees = -90;
                break;
            case ExifInterface.ORIENTATION_UNDEFINED:
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                // No transformations necessary in this case.
        }
        return rotationDegrees;
    }

    public static boolean getPictureFlipXByExifOrientation(int orientation) {
        boolean flipX = false;
        // See e.g. https://magnushoff.com/articles/jpeg-orientation/ for a detailed explanation on each
        // orientation.
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
            case ExifInterface.ORIENTATION_TRANSPOSE:
            case ExifInterface.ORIENTATION_TRANSVERSE:
                flipX = true;
        }
        return flipX;
    }

    public static boolean getPictureFlipYByExifOrientation(int orientation) {
        return orientation == ExifInterface.ORIENTATION_FLIP_VERTICAL;
    }

    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm 需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        return rotateBitmap(bm, degree, false, false);
    }

    @SuppressLint("NewApi")
    public static int getExifOrientationTag(InputStream inputStream) {
        ExifInterface exif;
        try {
            if (inputStream == null) {
                return 0;
            }
            if (BuildUtils.INSTANCE.getSDK_INT() >= VERSION_CODES.N) {
                exif = new ExifInterface(inputStream);
                return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getExifOrientationTag(String path) {
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            return exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int getExifOrientationTag(ContentResolver resolver, Uri imageUri) {
        // We only support parsing EXIF orientation tag from local file on the device.
        // See also:
        // https://android-developers.googleblog.com/2016/12/introducing-the-exifinterface-support-library.html
        if (!ContentResolver.SCHEME_CONTENT.equals(imageUri.getScheme())
                && !ContentResolver.SCHEME_FILE.equals(imageUri.getScheme())) {
            return 0;
        }
        InputStream inputStream = null;
        try{
             inputStream = resolver.openInputStream(imageUri);
            return getExifOrientationTag(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            ZLog.e("failed to open file to read rotation meta data: " + imageUri);
            return 0;
        }finally {
            if (null != inputStream){
                try {
                    inputStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static Bitmap rotateBitmapByOrientation(Bitmap bm, int orientation) {
        int angle = getPictureRotateAngelByExifOrientation(orientation);
        boolean flipX = getPictureFlipXByExifOrientation(orientation);
        boolean flipY = getPictureFlipYByExifOrientation(orientation);
        if (angle > 0 || flipX || flipY) {
            return rotateBitmap(bm, angle, flipX, flipY);
        } else {
            return bm;
        }
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
