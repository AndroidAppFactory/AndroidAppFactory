package com.bihe0832.android.lib.media;

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
import com.bihe0832.android.lib.file.action.FileAction;
import com.bihe0832.android.lib.file.mimetype.FileMimeTypes;
import com.bihe0832.android.lib.file.provider.ZixieFileProvider;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import com.bihe0832.android.lib.utils.os.OSUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import org.jetbrains.annotations.NotNull;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2019-12-17.
 *         Description: Description
 */
public class Media {

    public static Uri getRealPathUri(String mimeType) {
        boolean status = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (FileMimeTypes.INSTANCE.isImageFileByMimeType(mimeType)) {
            if (status) {
                return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else {
                return MediaStore.Images.Media.INTERNAL_CONTENT_URI;
            }
        } else if (FileMimeTypes.INSTANCE.isVideoFileByMimeType(mimeType)) {
            if (status) {
                return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else {
                return MediaStore.Video.Media.INTERNAL_CONTENT_URI;
            }
        } else if (FileMimeTypes.INSTANCE.isAudioFileByMimeType(mimeType)) {
            if (status) {
                return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            } else {
                return MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
            }
        } else {
            return MediaStore.Files.getContentUri("external");
        }
    }

    public static final String getFolderType(String mimeType) {
        String folderType = Environment.DIRECTORY_DCIM;
        if (FileMimeTypes.INSTANCE.isImageFileByMimeType(mimeType)) {
            folderType = Environment.DIRECTORY_PICTURES;
        } else if (FileMimeTypes.INSTANCE.isVideoFileByMimeType(mimeType)) {
            folderType = Environment.DIRECTORY_MOVIES;
        } else if (FileMimeTypes.INSTANCE.isAudioFileByMimeType(mimeType)) {
            folderType = Environment.DIRECTORY_MUSIC;
        }
        return folderType;
    }

    public static final String getZixieMediaPath(@NotNull Context context, String mimeType, String subDir) {
        String folderType = Environment.DIRECTORY_DCIM;
        if (TextUtils.isEmpty(subDir)) {
            folderType = getFolderType(mimeType);
        }
        String filePath = Environment.getExternalStoragePublicDirectory(folderType).getPath();
        if (TextUtils.isEmpty(filePath)) {
            //android 11以上，将文件创建在公有目录
            filePath = ZixieFileProvider.getZixieFilePath(context) + folderType;
        }
        if (TextUtils.isEmpty(subDir)) {
            return FileUtils.INSTANCE.getFolderPathWithSeparator(filePath) + subDir;
        } else {
            return FileUtils.INSTANCE.getFolderPathWithSeparator(
                    FileUtils.INSTANCE.getFolderPathWithSeparator(filePath) + subDir);
        }
    }

    private static void updateContentValues(ContentValues contentValues, String mimeType, String subDir, File file) {
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, mimeType);//文件类型
        if (!TextUtils.isEmpty(subDir)) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_DCIM + File.separator + subDir);

        }
        if (FileMimeTypes.INSTANCE.isImageFileByMimeType(mimeType) && OSUtils.isAndroidQVersion()) {
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);
        }
        String fileName = file.getName();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis());
        contentValues.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
        contentValues.put(MediaStore.MediaColumns.TITLE, fileName);
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.SIZE, file.length());
    }

    private static String writeToPhotos(ContentResolver contentResolver, ContentValues contentValues, Uri targetUri,
            String sourceFile) {
        try {
            FileInputStream inputStream = new FileInputStream(sourceFile);
            OutputStream outputStream = contentResolver.openOutputStream(targetUri);
            FileAction.INSTANCE.copyFile(inputStream, outputStream);
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        if (OSUtils.isAndroidQVersion()) {
            contentValues.clear();
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
            int result = contentResolver.update(targetUri, contentValues, null, null);
            if (result == 0) {
                String selection = MediaStore.Images.Media._ID + " = ?";
                String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(targetUri))};
                contentResolver.update(getRealPathUri(FileMimeTypes.INSTANCE.getMimeType(sourceFile)), contentValues,
                        selection, selectionArgs);
            }
        }
        return targetUri.toString();
    }

    public static Uri createUriAboveAndroidQ(Context context, String fileMimeType, String subFilePath, String name) {
        ContentValues contentValues = new ContentValues();//内容
        ContentResolver resolver = context.getApplicationContext().getContentResolver();//内容解析器
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name);//文件名
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, fileMimeType);//文件类型
        if (OSUtils.isAndroidQVersion()) {
            contentValues.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        }
        if (!TextUtils.isEmpty(subFilePath)) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_DCIM + File.separator + subFilePath);
        }
        return resolver.insert(getRealPathUri(fileMimeType), contentValues);
    }

    /**
     * AndroidQ以下创建用于保存拍照的照片的uri，(沙盒目录/pictures/subFilePath)
     * 拍照传入的intent中
     * Android7以下：file类型的uri
     * Android7以上：content类型的uri
     *
     * @param context activity
     * @param name 文件名
     * @param subFilePath 子文件夹
     * @return content uri
     */
    public static Uri createUriBelowAndroidQ(Context context, String subFilePath, String name) {
        String picFolder = FileUtils.INSTANCE.getFolderPathWithSeparator(
                ZixieFileProvider.getZixieCacheFolder(context) + subFilePath);
        if (FileUtils.INSTANCE.checkAndCreateFolder(picFolder)) {
            File picture = new File(picFolder, name);
            if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.N) {
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

    public static String addToPhotos(Context context, String filePath, String subDir, boolean isSameName) {
        if (!FileUtils.INSTANCE.checkFileExist(filePath)) {
            return "";
        }
        String targetPath = "";
        File image = new File(filePath);
        String mimeType = FileMimeTypes.INSTANCE.getMimeType(filePath);
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        updateContentValues(contentValues, mimeType, subDir, image);
        Uri targetUri = contentResolver.insert(getRealPathUri(mimeType), contentValues);
        if (targetUri != null) {
            targetPath = writeToPhotos(contentResolver, contentValues, targetUri, filePath);
        } else {
            try {
                String path = getZixieMediaPath(context, mimeType, subDir);
                if (isSameName) {
                    path = path + FileUtils.INSTANCE.getFileName(filePath);
                } else {
                    path = path + System.currentTimeMillis() + "." + FileUtils.INSTANCE.getExtensionName(filePath);
                }
                File newFile = new File(path);
                FileUtils.INSTANCE.copyFile(image, newFile, false);
                ContentValues newValues = new ContentValues();
                updateContentValues(newValues, mimeType, subDir, image);
                targetPath = contentResolver.insert(getRealPathUri(mimeType), newValues).toString();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    if (FileMimeTypes.INSTANCE.isImageFileByMimeType(mimeType)) {
                        targetPath = MediaStore.Images.Media.insertImage(context.getContentResolver(), filePath, "",
                                "");
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }
        try {
            MediaScannerConnection.scanFile(context, new String[]{getZixieMediaPath(context, mimeType, subDir)}, null,
                    null);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, targetUri));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return targetPath;
    }

    public static String addToPhotos(Context context, String imagePath) {
        return addToPhotos(context, imagePath, "", false);
    }

    public static boolean removeFromPhotos(Context context, String contentUriString) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            int count = contentResolver.delete(Uri.parse(contentUriString), null, null);
            return count == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static File uriToFile(Context context, Uri uri, boolean needReadFileByPath) {
        return ZixieFileProvider.uriToFile(context, uri, needReadFileByPath);
    }

    public static File uriToFile(Context context, Uri uri) {
        return ZixieFileProvider.uriToFile(context, uri, true);
    }
}
