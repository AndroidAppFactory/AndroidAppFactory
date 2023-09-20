package com.bihe0832.android.lib.media;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.file.provider.ZixieFileProvider;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.jetbrains.annotations.NotNull;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2019-12-17.
 *         Description: Description
 */
public class Media {

    public static final String getZixiePhotosPath(@NotNull Context context) {
        String filePath = ZixieFileProvider.getZixieFilePath(context) + File.separator + Environment.DIRECTORY_PICTURES;
        if (BuildUtils.INSTANCE.getSDK_INT() >= 30) {
            //android 11以上，将文件创建在公有目录
            filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
        }

        return FileUtils.INSTANCE.getFolderPathWithSeparator(filePath);
    }


    public static void addPicToPhotos(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), filePath, "", "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Uri uri = Uri.parse(filePath);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }


    public static boolean addVideoToPhotos(Context context, String filePath) {
        File file = new File(filePath);
        if (FileUtils.INSTANCE.checkFileExist(filePath)) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.TITLE, file.getName());
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
            values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis());
            values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
            values.put(MediaStore.MediaColumns.SIZE, file.length());
            values.put(MediaStore.MediaColumns.MIME_TYPE, "video/*");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES);
                values.put(MediaStore.MediaColumns.IS_PENDING, 1);
            }
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

            try {
                InputStream inputStream = new FileInputStream(filePath);
                OutputStream outputStream = contentResolver.openOutputStream(uri);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                inputStream.close();
                outputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear();
                values.put(MediaStore.MediaColumns.IS_PENDING, 0);
                int result = contentResolver.update(uri, values, null, null);
                return result > 0;
            } else {
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * AndroidQ以上创建用于保存相片的uri，(公有目录/pictures/filePath)
     *
     * @param activity activity
     * @param name 文件名
     * @param filePath 子文件夹
     * @return uri
     */
    public static Uri createImageUriAboveAndroidQ(Activity activity, String filePath, String name) {
        ContentValues contentValues = new ContentValues();//内容
        ContentResolver resolver = activity.getContentResolver();//内容解析器
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name);//文件名
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/*");//文件类型
        if (filePath != null && !filePath.equals("")) {
            //存放子文件夹
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + filePath);
        } else {
            //存放picture目录
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
        }
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
    }

    /**
     * AndroidQ以下创建用于保存拍照的照片的uri，(沙盒目录/pictures/filePath)
     * 拍照传入的intent中
     * Android7以下：file类型的uri
     * Android7以上：content类型的uri
     *
     * @param activity activity
     * @param name 文件名
     * @param filePath 子文件夹
     * @return content uri
     */
    public static Uri createImageUriForCameraBelowAndroidQ(Activity activity, String filePath, String name) {
        File picFolder = new File(getZixiePhotosPath(activity) + filePath);
        if (FileUtils.INSTANCE.checkAndCreateFolder(picFolder.getAbsolutePath())) {
            File picture = new File(picFolder, name);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //适配Android7以上的path转uri，该方法得到的uri为content类型的
                return ZixieFileProvider.getZixieFileProvider(activity, picture);
            } else {
                //Android7以下，该方法得到的uri为file类型的
                return Uri.fromFile(picture);
            }
        } else {
            return null;
        }
    }

    /**
     * AndroidQ以下创建用于保存裁剪的uri，(沙盒目录/pictures/filePath)
     * 裁剪传入intent的uri跟拍照不同
     * 在AndroidQ以下统一使用file类型的uri，所以统一用Uri.fromFile()方法返回
     *
     * @param Context context
     * @param name 文件名
     * @param filePath 子文件夹
     * @return file uri
     */
    public static Uri createImageUriForCropBelowAndroidQ(Context context, String filePath, String name) {
        File childDir = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + filePath);
        if (FileUtils.INSTANCE.checkAndCreateFolder(childDir.getAbsolutePath())) {
            File picture = new File(childDir, name);
            return Uri.fromFile(picture);
        } else {
            return null;
        }
    }
}
