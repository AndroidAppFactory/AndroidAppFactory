package com.bihe0832.android.lib.ui.image;

import static android.os.Environment.DIRECTORY_PICTURES;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import com.bihe0832.android.lib.log.ZLog;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class BitmapUtil {

    /**
     * 返回bitmap的数组大小
     *
     * @param bm
     * @return
     */
    public static byte[] bitmap2Bytes(Bitmap bm) {
        if (bm == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }


    public static Bitmap getLoacalBitmap(String localPath) {
        Bitmap bitmap = null;
        File file = new File(localPath);
        if (file.exists()) {
            try {
                bitmap = BitmapFactory.decodeFile(localPath);
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * 读取图片，按照缩放比保持长宽比例返回bitmap对象
     * <p>
     *
     * @param scale 缩放比例(1到10, 为2时，长和宽均缩放至原来的2分之1，为3时缩放至3分之1，以此类推)
     * @return Bitmap
     */
    public synchronized static Bitmap getLoacalBitmap(Context context, int res, int scale) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inSampleSize = scale;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            return BitmapFactory.decodeResource(context.getResources(), res, options);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getViewBitmap(View view) {
        String filePath = "";
        Bitmap mAccBitmap = Bitmap
                .createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas chartCanvas = new Canvas(mAccBitmap);
        view.draw(chartCanvas);
        filePath = BitmapUtil.saveBitmapToSdCard(view.getContext(), mAccBitmap);
        return filePath;
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
    public static Bitmap getImageBitmapWithLayer(ImageView view, int color) {
        Bitmap originalBitmap = ((BitmapDrawable) (view).getDrawable()).getBitmap();
        return getBitmapWithLayer(originalBitmap, color);
    }

    public static Bitmap getBitmapWithLayer(Bitmap originalBitmap, int color) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        Bitmap updatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(updatedBitmap);

        canvas.drawBitmap(originalBitmap, 0, 0, null);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        canvas.drawRect(0, 0, width, width, paint);
        return updatedBitmap;
    }

    public static Bitmap getRemoteBitmap(String imgUrl, int width, int height) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(imgUrl);
            InputStream is = url.openStream();
            // 将InputStream变为Bitmap
            bitmap = getRemoteBitmap(is, width, height);
            is.close();
        } catch (Exception e) {
            // ignore
        }
        return bitmap;
    }

    private static Bitmap getRemoteBitmap(InputStream is, int width, int height) {
        BufferedInputStream stream = new BufferedInputStream(is);
        stream.mark(4 * 1024);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(stream, null, options);
        calculateInSampleSize(width, height, options, true);
        try {
            stream.reset();
        } catch (Exception e) {
            // ignore
        }
        return BitmapFactory.decodeStream(stream, null, options);
    }

    private static void calculateInSampleSize(int reqWidth, int reqHeight, BitmapFactory.Options options,
            boolean centerInside) {
        calculateInSampleSize(reqWidth, reqHeight, options.outWidth, options.outHeight, options,
                centerInside);
    }

    //根据width 和 height 与 reqWidth 和 reqHeight 的差异，计算出如果缩放到一样大，使用的 BitmapFactory.Options
    public static void calculateInSampleSize(int reqWidth, int reqHeight, int width, int height,
            BitmapFactory.Options options, boolean centerInside) {
        int sampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio;
            final int widthRatio;
            if (reqHeight == 0) {
                sampleSize = (int) Math.floor((float) width / (float) reqWidth);
            } else if (reqWidth == 0) {
                sampleSize = (int) Math.floor((float) height / (float) reqHeight);
            } else {
                heightRatio = (int) Math.floor((float) height / (float) reqHeight);
                widthRatio = (int) Math.floor((float) width / (float) reqWidth);
                sampleSize = centerInside
                        ? Math.max(heightRatio, widthRatio)
                        : Math.min(heightRatio, widthRatio);
            }
        }
        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
    }


    /**
     * 把bitmap保存到本地
     *
     * @return
     */
    public static String saveBitmapToSdCard(Context context, Bitmap bitmap) {
        String packageName = context.getPackageName();
        String filePath = packageName + "_pic_" + System.currentTimeMillis() + ".png";
        return saveBitmapToSdCard(context, bitmap, filePath);
    }

    /**
     * 把bitmap保存到本地
     *
     * @return
     */
    public static String saveBitmapToSdCard(Context context, Bitmap bitmap, String fileName) {
        if (null == context) {
            return null;
        }
        if (null == bitmap) {
            return null;
        }

        String filePath = null;
        File externalFilesDir = context.getExternalFilesDir(DIRECTORY_PICTURES);
        String dir = null;
        if (null != externalFilesDir) {
            dir = externalFilesDir.getAbsolutePath();
        }
        if (!TextUtils.isEmpty(dir)) {
            if (!dir.endsWith(File.separator)) {
                filePath = dir + File.separator + fileName;
            } else {
                filePath = dir + fileName;
            }
            ZLog.e("BitmapUtil", "filePath = " + filePath);
            try {
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();

                FileOutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
            } catch (Exception ignore) {
            }
        }
        return filePath;
    }

    /**
     * 把两个位图覆盖合成为一个位图，上下拼接
     *
     * @param isBaseMax 是否以高度大的位图为准，true则小图等比拉伸，false则大图等比压缩
     * @return
     */
    public static Bitmap mergeBitmapLine(Bitmap topBitmap, Bitmap bottomBitmap, boolean isBaseMax) {

        if (topBitmap == null || topBitmap.isRecycled()
                || bottomBitmap == null || bottomBitmap.isRecycled()) {
            ZLog.d("BitmapUtil", "topBitmap=" + topBitmap + ";bottomBitmap=" + bottomBitmap);
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

        if (bottomBitmap == null || bottomBitmap.isRecycled()
                || iconBitmap == null || iconBitmap.isRecycled()) {
            ZLog.d("BitmapUtil", "topBitmap=" + bottomBitmap + ";bottomBitmap=" + iconBitmap);
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
}
