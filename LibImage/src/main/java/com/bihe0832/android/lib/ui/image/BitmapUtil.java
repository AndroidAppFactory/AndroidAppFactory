package com.bihe0832.android.lib.ui.image;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.file.ZixieFileProvider;
import com.bihe0832.android.lib.log.ZLog;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class BitmapUtil {

    private static final String TAG = "BitmapUtil";

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
        bm.compress(CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 读取图片，按照缩放比保持长宽比例返回bitmap对象
     * <p>
     *
     * @param scale 缩放比例(1到10, 为2时，长和宽均缩放至原来的2分之1，为3时缩放至3分之1，以此类推)
     * @return Bitmap
     */
    public synchronized static Bitmap getLocalBitmap(Context context, int res, int scale) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inSampleSize = scale;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeResource(context.getResources(), res, options);
        } catch (Exception e) {
            return null;
        }
    }

    public static BitmapFactory.Options getLocalBitmapOptions(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            options.inJustDecodeBounds = true;
            options.inDither = true;
            options.inPurgeable = true;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
            BitmapFactory.decodeFile(filePath, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return options;
    }

    public static Bitmap getLocalBitmap(String localPath, int reqWidth, int reqHeight, boolean centerInside) {
        Bitmap bitmap = null;
        File file = new File(localPath);
        if (file.exists()) {
            try {
                BitmapFactory.Options options = getLocalBitmapOptions(localPath);
                calculateInSampleSize(reqWidth, reqHeight, options, centerInside);
                bitmap = BitmapFactory.decodeFile(localPath, options);
            } catch (Exception error) {
                error.printStackTrace();
            }
        }
        return bitmap;
    }

    public static Bitmap getLocalBitmap(String localPath) {
        Bitmap bitmap = null;
        File file = new File(localPath);
        if (file.exists()) {
            try {
                bitmap = BitmapFactory.decodeFile(localPath);
            } catch (Exception error) {
                error.printStackTrace();
            }
        }
        return bitmap;
    }


    public static BitmapFactory.Options getLocalBitmapOptions(ContentResolver context, Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        InputStream input = null;
        try {
            options.inJustDecodeBounds = true;
            options.inDither = true;
            options.inPurgeable = true;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
            input = context.openInputStream(uri);
            BitmapFactory.decodeStream(input, null, options);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != input) {
                    input.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return options;
    }

    public static Bitmap getLocalBitmap(ContentResolver context, Uri uri, int reqWidth, int reqHeight,
            boolean centerInside) {
        Bitmap bitmap = null;
        InputStream input = null;
        try {
            BitmapFactory.Options options = getLocalBitmapOptions(context, uri);
            calculateInSampleSize(reqWidth, reqHeight, options, centerInside);
            input = context.openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(input, null, options);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != input) {
                    input.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bitmap;
    }

    public static Bitmap getLocalBitmap(ContentResolver context, Uri uri) {
        Bitmap bitmap = null;
        InputStream input = null;
        try {
            input = context.openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != input) {
                    input.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bitmap;
    }


    public static String getViewBitmap(View view) {
        String filePath = "";
        Bitmap mAccBitmap = Bitmap
                .createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas chartCanvas = new Canvas(mAccBitmap);
        view.draw(chartCanvas);
        filePath = BitmapUtil.saveBitmap(view.getContext(), mAccBitmap);
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
            e.printStackTrace();
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
            e.printStackTrace();
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
    public static String saveBitmap(Context context, Bitmap bitmap) {
        String packageName = context.getPackageName();
        String filePath = packageName + "_pic_" + System.currentTimeMillis() + ".png";
        return saveBitmapWithName(context, bitmap, filePath);
    }

    /**
     * 把bitmap保存到本地
     *
     * @return
     */
    public static String saveBitmapWithName(Context context, Bitmap bitmap, String fileName) {
        return saveBitmapWithName(context, bitmap, CompressFormat.PNG, fileName, true);

    }

    /**
     * 把bitmap保存到本地
     *
     * @return
     */
    public static String saveBitmapWithName(Context context, Bitmap bitmap, CompressFormat format, String fileName,
            boolean forceNew) {
        if (null == context) {
            return "";
        }
        String filePath = ZixieFileProvider.getZixieFilePath(context) + "temp" + File.separator + fileName;
        return saveBitmapWithPath(context, bitmap, format, filePath, forceNew);
    }


    public static String saveBitmapWithPath(Context context, Bitmap bitmap, String filePath) {
        return saveBitmapWithPath(context, bitmap, CompressFormat.PNG,filePath,true);
        
    }
    /**
     * 把bitmap保存到本地
     *
     * @return
     */
    public static String saveBitmapWithPath(Context context, Bitmap bitmap, CompressFormat format, String filePath,
            boolean forceNew) {
        if (null == context) {
            return "";
        }
        if (null == bitmap) {
            return "";
        }

        if (!TextUtils.isEmpty(filePath)) {
            ZLog.e(TAG, "filePath = " + filePath);
            try {
                File file = new File(filePath);
                if (file.exists()) {
                    if (forceNew) {
                        file.delete();
                        file.createNewFile();
                    }
                } else {
                    FileUtils.INSTANCE.checkAndCreateFolder(file.getParent());
                    file.createNewFile();
                }
                FileOutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(format, 100, outputStream);
                outputStream.flush();
                outputStream.close();
                return filePath;
            } catch (Exception ignore) {
                ignore.printStackTrace();
                return "";
            }
        } else {
            return "";
        }
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

        if (bottomBitmap == null || bottomBitmap.isRecycled()
                || iconBitmap == null || iconBitmap.isRecycled()) {
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

    public static Bitmap compress(Bitmap image, long targetSize) {

        final int size_length = 10;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(CompressFormat.JPEG, 100, baos);
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
                    + size_length * targetSize
                    + "; options:" + options);
            //重置baos即清空baos
            baos.reset();
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            //这里压缩options%，把压缩后的数据存放到baos中
            image.compress(CompressFormat.JPEG, options, baos);
            ZLog.d(TAG, "compress end source length " + baos.toByteArray().length + "; target length:"
                    + size_length * targetSize
                    + "; options:" + options);
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm 需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {

        Bitmap resultBitmap = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            resultBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == resultBitmap) {
            resultBitmap = bm;
        }
        if (bm != resultBitmap) {
            bm.recycle();
        }
        return resultBitmap;
    }
}
