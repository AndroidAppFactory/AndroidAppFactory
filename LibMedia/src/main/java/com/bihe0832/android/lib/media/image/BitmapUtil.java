package com.bihe0832.android.lib.media.image;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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


    private static void calculateAndResetInSampleSize(int reqWidth, int reqHeight, BitmapFactory.Options options,
            boolean centerInside) {
        int size = BitmapTransUtils.calculateInSampleSize(reqWidth, reqHeight, options.outWidth, options.outHeight,
                centerInside);
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
     * @param reqWidth 最大允许宽度
     * @param reqHeight 最大允许高度
     * @return 返回一个缩放后的Bitmap，失败则返回null
     */
    public static Bitmap getLocalBitmap(Context context, Uri uri, int reqWidth, int reqHeight, boolean centerInside) {
        if (context == null) {
            return null;
        }
        return getLocalBitmap(context.getContentResolver(), uri, reqWidth, reqHeight, centerInside);
    }

    public static Bitmap getLocalBitmap(String localPath, int reqWidth, int reqHeight, boolean centerInside) {
        File file = new File(localPath);
        if (file.exists()) {
            try {
                BitmapFactory.Options options = getLocalBitmapOptions(localPath);
                calculateAndResetInSampleSize(reqWidth, reqHeight, options, centerInside);
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeFile(localPath, options);

                Bitmap resizedBitmap = bitmap;
                int angle = BitmapTransUtils.getPictureRotateAngel(localPath);
                if (angle > 0) {
                    resizedBitmap = BitmapTransUtils.rotateBitmapByDegree(bitmap, angle);
                }
                if (reqWidth > 0 && reqHeight > 0 && (reqWidth < resizedBitmap.getWidth()
                        || reqHeight < resizedBitmap.getHeight())) {
                    // 缩放 Bitmap 对象
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(resizedBitmap, reqWidth, reqHeight, false);
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


    public static Bitmap getLocalBitmap(ContentResolver contentResolver, Uri uri, int reqWidth, int reqHeight,
            boolean centerInside) {

        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_CONTENT.equals(scheme) || ContentResolver.SCHEME_FILE.equals(scheme)) {
            InputStream input = null;

            try {
                input = contentResolver.openInputStream(uri);
                BitmapFactory.Options options = getLocalBitmapOptions(contentResolver, uri);
                calculateAndResetInSampleSize(reqWidth, reqHeight, options, centerInside);
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
                if (reqWidth > 0 && reqHeight > 0 && (reqWidth < bitmap.getWidth() || reqHeight < bitmap.getHeight())) {
                    // 缩放 Bitmap 对象
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, false);
                    // 释放资源
                    bitmap.recycle();
                    return scaledBitmap;
                } else {
                    return bitmap;
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

    public static Bitmap getRemoteBitmap(String urlString, int reqWidth, int reqHeight) {
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
            calculateAndResetInSampleSize(reqWidth, reqHeight, options, true);

            // 重新打开输入流并解码为 Bitmap 对象
            inputStream = url.openConnection().getInputStream();
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            if (reqWidth > 0 && reqHeight > 0) {
                // 缩放 Bitmap 对象
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, false);
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
        String filePath = packageName + "_pic_" + System.currentTimeMillis() + ".png";
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
    public static String saveBitmapWithName(Context context, Bitmap bitmap, String fileName,
            boolean forceNew) {
        if (null == context) {
            return "";
        }
        String filePath = ZixieFileProvider.getZixieTempFolder(context) + fileName;
        return saveBitmapWithPath(context, bitmap, filePath, forceNew);
    }


    public static String saveBitmapWithPath(Context context, Bitmap bitmap, String filePath) {
        return saveBitmapWithPath(context, bitmap, filePath, true);

    }

    /**
     * 把bitmap保存到本地
     *
     * @return
     */
    public static String saveBitmapWithPath(Context context, Bitmap bitmap, String filePath, boolean forceNew) {
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
                bitmap.compress(bitmap.hasAlpha() ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100,
                        outputStream);
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
