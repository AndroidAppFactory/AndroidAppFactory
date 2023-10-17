package com.bihe0832.android.lib.media;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.file.provider.ZixieFileProvider;
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

    public static final String getZixieMediaPath(@NotNull Context context, String folderType) {
        String filePath = Environment.getExternalStoragePublicDirectory(folderType).getPath();
        if (TextUtils.isEmpty(filePath)) {
            //android 11以上，将文件创建在公有目录
            filePath = ZixieFileProvider.getZixieFilePath(context) + folderType;
        }
        return FileUtils.INSTANCE.getFolderPathWithSeparator(filePath);
    }

    private static void writeToPhotos(ContentResolver contentResolver, ContentValues contentValues,
            Uri targetUri, String sourceFile) {
        try {
            InputStream inputStream = new FileInputStream(sourceFile);
            OutputStream outputStream = contentResolver.openOutputStream(targetUri);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear();
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
            int result = contentResolver.update(targetUri, contentValues, null, null);
            if (result == 0) {
                String selection = MediaStore.Images.Media._ID + " = ?";
                String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(targetUri))};
                contentResolver.update(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues, selection,
                        selectionArgs);

            }
        }
    }

    private static void updateContentValues(ContentValues contentValues, File file, String fileType, String subDir) {
        if (fileType.equals(Environment.DIRECTORY_PICTURES)) {
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/*");//文件类型
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);
            }
        } else if (fileType.equals(Environment.DIRECTORY_MOVIES)) {
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "video/*");//文件类型
        }
        String fileName = file.getName();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis());
        contentValues.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
        contentValues.put(MediaStore.MediaColumns.TITLE, fileName);
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.SIZE, file.length());
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + subDir);
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
     * @param context activity
     * @param name 文件名
     * @param filePath 子文件夹
     * @return content uri
     */
    public static Uri createImageUriForCameraBelowAndroidQ(Context context, String filePath, String name) {
        String picFolder = FileUtils.INSTANCE.getFolderPathWithSeparator(
                ZixieFileProvider.getZixieCacheFolder(context) + filePath);
        if (FileUtils.INSTANCE.checkAndCreateFolder(picFolder)) {
            File picture = new File(picFolder, name);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //适配Android7以上的path转uri，该方法得到的uri为content类型的
                return ZixieFileProvider.getZixieFileProvider(context, picture);
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
        String picFolder = FileUtils.INSTANCE.getFolderPathWithSeparator(
                ZixieFileProvider.getZixieCacheFolder(context) + filePath);
        if (FileUtils.INSTANCE.checkAndCreateFolder(picFolder)) {
            File picture = new File(picFolder, name);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //适配Android7以上的path转uri，该方法得到的uri为content类型的
                return ZixieFileProvider.getZixieFileProvider(context, picture);
            } else {
                //Android7以下，该方法得到的uri为file类型的
                return Uri.fromFile(picture);
            }
        } else {
            return null;
        }
    }

    public static void addPicToPhotos(Context context, String imagePath) {
        addPicToPhotos(context, imagePath, Environment.DIRECTORY_PICTURES);
    }

    public static void addPicToPhotos(Context context, String imagePath, String subDir) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        File image = new File(imagePath);
        updateContentValues(contentValues, image, Environment.DIRECTORY_PICTURES, subDir);
        Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (imageUri != null) {
            writeToPhotos(contentResolver, contentValues, imageUri, imagePath);
        } else {
            try {
                String path = getZixieMediaPath(context, subDir) + System.currentTimeMillis()
                        + "." + FileUtils.INSTANCE.getExtensionName(imagePath);
                File newFile = new File(path);
                FileUtils.INSTANCE.copyFile(image, newFile, false);
                ContentValues newValues = new ContentValues();
                updateContentValues(newValues, newFile, Environment.DIRECTORY_PICTURES, subDir);
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, newValues);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    MediaStore.Images.Media.insertImage(context.getContentResolver(), imagePath, "", "");
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }
        try {
            MediaScannerConnection.scanFile(context,
                    new String[]{getZixieMediaPath(context, subDir)}, null, null);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addVideoToPhotos(Context context, String imagePath) {
        addPicToPhotos(context, imagePath, Environment.DIRECTORY_MOVIES);
    }

    public static void addVideoToPhotos(Context context, String filePath, String subDir) {
        File video = new File(filePath);
        if (FileUtils.INSTANCE.checkFileExist(filePath)) {
            ContentResolver contentResolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            updateContentValues(values, video, Environment.DIRECTORY_MOVIES, subDir);
            values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, System.currentTimeMillis());
            Uri uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                writeToPhotos(contentResolver, values, uri, filePath);
            } else {
                try {
                    String path =
                            getZixieMediaPath(context, subDir) + System.currentTimeMillis() + "."
                                    + FileUtils.INSTANCE.getExtensionName(filePath);
                    File newFile = new File(path);
                    FileUtils.INSTANCE.copyFile(video, newFile, false);
                    ContentValues newValues = new ContentValues();
                    updateContentValues(newValues, newFile, Environment.DIRECTORY_MOVIES, subDir);
                    newValues.put(MediaStore.Video.VideoColumns.DATE_TAKEN, System.currentTimeMillis());
                    contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, newValues);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                MediaScannerConnection.scanFile(context,
                        new String[]{getZixieMediaPath(context, subDir)}, null, null);
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
