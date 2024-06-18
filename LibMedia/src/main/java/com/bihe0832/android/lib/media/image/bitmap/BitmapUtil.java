package com.bihe0832.android.lib.media.image.bitmap;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.file.provider.ZixieFileProvider;
import com.bihe0832.android.lib.log.ZLog;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class BitmapUtil {

    public static final String TAG = "BitmapUtil";

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
        bm.compress(bm.hasAlpha() ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 读取图片，按照缩放比保持长宽比例返回bitmap对象
     * <p>
     *
     * @param scale 缩放比例(1到10, 为2时，长和宽均缩放至原来的2分之1，为3时缩放至3分之1，以此类推)
     * @return Bitmap
     */
    public static Bitmap getLocalBitmap(Context context, int res, int scale) {
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
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            BitmapFactory.decodeFile(filePath, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return options;
    }

    public static BitmapFactory.Options getLocalBitmapOptions(ContentResolver contentResolver, Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        InputStream input = null;
        try {
            options.inJustDecodeBounds = true;
            options.inDither = true;
            options.inPurgeable = true;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
            input = contentResolver.openInputStream(uri);
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


    private static void calculateAndResetInSampleSize(int maxWidth, int maxHeight, BitmapFactory.Options options) {
        int size = BitmapTransUtils.calculateInSampleSize(maxWidth, maxHeight, options.outWidth, options.outHeight);
        options.inSampleSize = size;
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

    /**
     * 读取一个缩放后的图片，限定图片大小，避免OOM
     *
     * @param uri 图片uri，支持“file://”、“content://”
     * @param maxWidth 最大允许宽度
     * @param maxHeight 最大允许高度
     * @return 返回一个缩放后的Bitmap，失败则返回null
     */
    public static Bitmap getLocalBitmap(Context context, Uri uri, int maxWidth, int maxHeight) {
        if (context == null) {
            return null;
        }
        return getLocalBitmap(context.getContentResolver(), uri, maxWidth, maxHeight);
    }

    public static Bitmap getLocalBitmap(String localPath, int maxWidth, int maxHeight) {
        File file = new File(localPath);
        if (file.exists()) {
            try {
                BitmapFactory.Options options = getLocalBitmapOptions(localPath);
                calculateAndResetInSampleSize(maxWidth, maxHeight, options);
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeFile(localPath, options);

                int orientation = BitmapTransUtils.getExifOrientationTag(localPath);
                Bitmap resizedBitmap = BitmapTransUtils.rotateBitmapByOrientation(bitmap, orientation);
                if (maxWidth > 0 && maxHeight > 0 && (maxWidth < resizedBitmap.getWidth()
                        || maxHeight < resizedBitmap.getHeight())) {
                    // 缩放 Bitmap 对象
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(resizedBitmap, maxWidth, maxHeight, false);
                    // 释放资源
                    resizedBitmap.recycle();
                    return scaledBitmap;
                } else {
                    return resizedBitmap;
                }
            } catch (Exception error) {
                error.printStackTrace();
            }
        }
        return null;
    }


    public static Bitmap getLocalBitmap(ContentResolver contentResolver, Uri uri, int maxWidth, int maxHeight) {

        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_CONTENT.equals(scheme) || ContentResolver.SCHEME_FILE.equals(scheme)) {
            InputStream input = null;

            try {
                input = contentResolver.openInputStream(uri);
                BitmapFactory.Options options = getLocalBitmapOptions(contentResolver, uri);
                calculateAndResetInSampleSize(maxWidth, maxHeight, options);
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
                int orientation = BitmapTransUtils.getExifOrientationTag(input);
                Bitmap resizedBitmap = BitmapTransUtils.rotateBitmapByOrientation(bitmap, orientation);
                if (maxWidth > 0 && maxHeight > 0 && (maxWidth < resizedBitmap.getWidth()
                        || maxHeight < resizedBitmap.getHeight())) {
                    // 缩放 Bitmap 对象
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(resizedBitmap, maxWidth, maxHeight, false);
                    // 释放资源
                    resizedBitmap.recycle();
                    return scaledBitmap;
                } else {
                    return resizedBitmap;
                }
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
        } else if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            Log.e("readBitmapData", "Unable to close content: " + uri);
        } else {
            Log.e("readBitmapData", "Unable to close content: " + uri);
        }
        return null;
    }


    public static Bitmap resizeAndCenterBitmap(Bitmap source, int targetWidth, int targetHeight, int color,
            ScaleToFit stf) {
        Bitmap output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawColor(color);

        Matrix matrix = new Matrix();
        RectF sourceRect = new RectF(0, 0, source.getWidth(), source.getHeight());
        RectF targetRect = new RectF(0, 0, targetWidth, targetHeight);
        matrix.setRectToRect(sourceRect, targetRect, stf);

        float scaleX = targetRect.width() / sourceRect.width();
        float scaleY = targetRect.height() / sourceRect.height();
        float scale = Math.min(scaleX, scaleY);

        matrix.setScale(scale, scale);

        float dx = (targetWidth - source.getWidth() * scale) / 2;
        float dy = (targetHeight - source.getHeight() * scale) / 2;
        matrix.postTranslate(dx, dy);

        canvas.drawBitmap(source, matrix, new Paint(Paint.FILTER_BITMAP_FLAG));
        return output;
    }

    public static String transImageFileToRequiredSize(String sourceFile, String targetFile, int targetWidth,
            int targetHeight, int fillColor) {
        try {
            Bitmap source = BitmapUtil.getLocalBitmap(sourceFile, targetWidth, targetHeight);
            Bitmap output = BitmapUtil.resizeAndCenterBitmap(source, targetWidth, targetHeight, fillColor,
                    Matrix.ScaleToFit.CENTER);
            return BitmapUtil.saveBitmapWithPath(output, targetFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Bitmap getViewBitmapData(View view) {
        if (view.getMeasuredWidth() > 0 && view.getMeasuredHeight() > 0) {
            Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas chartCanvas = new Canvas(bitmap);
            view.draw(chartCanvas);
            return bitmap;
        }
        return null;
    }

    public static String getViewBitmap(View view) {
        String filePath = BitmapUtil.saveBitmap(view.getContext(), getViewBitmapData(view));
        return filePath;
    }

    public static Bitmap getRemoteBitmap(String urlString, int maxWidth, int maxHeight) {
        try {
            // 创建 URL 对象
            URL url = new URL(urlString);
            // 打开连接并获取输入流
            InputStream inputStream = url.openConnection().getInputStream();
            // 将输入流解码为 Bitmap 对象
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            // 计算采样率
            calculateAndResetInSampleSize(maxWidth, maxHeight, options);

            // 重新打开输入流并解码为 Bitmap 对象
            inputStream = url.openConnection().getInputStream();
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            if (maxWidth > 0 && maxHeight > 0) {
                // 缩放 Bitmap 对象
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, maxHeight, false);
                // 释放资源
                bitmap.recycle();
                return scaledBitmap;
            } else {
                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 把bitmap保存到本地
     *
     * @return
     */
    public static String saveBitmap(Context context, Bitmap bitmap) {
        String packageName = context.getPackageName();
        String filePath = packageName + "_pic_" + System.currentTimeMillis();
        if (bitmap.hasAlpha()) {
            filePath = filePath + ".png";
        } else {
            filePath = filePath + ".jpg";
        }
        return saveBitmapWithName(context, bitmap, filePath);
    }

    /**
     * 把bitmap保存到本地
     *
     * @return
     */
    public static String saveBitmapWithName(Context context, Bitmap bitmap, String fileName) {
        return saveBitmapWithName(context, bitmap, fileName, true);

    }

    /**
     * 把bitmap保存到本地
     *
     * @return
     */
    public static String saveBitmapWithName(Context context, Bitmap bitmap, String fileName, boolean forceNew) {
        if (null == context) {
            return "";
        }
        String filePath = ZixieFileProvider.getZixieTempFolder(context) + fileName;
        return saveBitmapWithPath(bitmap, filePath, forceNew);
    }


    public static String saveBitmapWithPath(Bitmap bitmap, String filePath) {
        return saveBitmapWithPath(bitmap, filePath, true);

    }

    /**
     * 把bitmap保存到本地
     *
     * @return
     */
    public static String saveBitmapWithPath(Bitmap bitmap, String filePath, boolean forceNew) {
        return saveBitmapWithPath(bitmap, bitmap.hasAlpha() ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG,
                100, filePath, forceNew);
    }

    /**
     * 把bitmap保存到本地
     *
     * @return
     */
    public static String saveBitmapWithPath(Bitmap bitmap, CompressFormat format, int quality, String filePath,
            boolean forceNew) {
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
                bitmap.compress(format, quality, outputStream);
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
}
