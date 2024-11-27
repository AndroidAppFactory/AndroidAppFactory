/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/16 下午3:08
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/16 下午2:15
 *
 */

package com.bihe0832.android.lib.file.provider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import androidx.core.content.FileProvider;
import androidx.core.content.PermissionChecker;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.file.R;
import com.bihe0832.android.lib.file.action.FileAction;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.ConvertUtils;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import com.bihe0832.android.lib.utils.os.OSUtils;
import java.io.File;
import java.util.Locale;

/**
 * @author zixie code@bihe0832.com Created on 2020-02-30. ZixieFileProvider 提供的Provider时，可以选择自定路径或者使用库默认路径
 *         <p>
 *         如果是选择默认的路径时你需要获取库提供的provider 可以使用接口 {@link ZixieFileProvider#getZixieFileProvider(Context, File)}
 *         <p>
 *         如果是选择自定义的， 需要保证文件保存地址为 {@link ZixieFileProvider#getZixieFilePath(Context)} 获取，或者
 *         使用context.getExternalFilesDir(context.getString(R.string.lib_bihe0832_file_folder))得到
 *         <p>
 *         并同步做如下操作，否则会造成获取provider失败等问题：
 *         <p>
 *         添加以下String值定义：
 *         <p>
 *         lib_bihe0832_file_folder：自定义文件目录 在res/xml添加文件：file_paths.xml，内容为：
 *         <p>
 *         <?xml version="1.0" encoding="utf-8"?>
 *         <paths>
 *         <external-files-path name="download" path="你自定义的文件目录"/>
 *         </paths>
 */
public class ZixieFileProvider extends FileProvider {

    public static final String FOLDER_TEMP = "temp";
    public static final String FOLDER_CACHE = "cache";

    public static final Uri getZixieFileProvider(Context context, File file) {
        Uri uri = null;
        try {
            uri = FileProvider.getUriForFile(context, getZixieFileProviderName(context), file);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                uri = FileProvider.getUriForFile(context, getAAFInnerFileProviderName(context), file);
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }

        return uri;
    }

    public static boolean isZixieFileProvider(Context context, Uri uri) {
        if (uri == null) {
            return false;
        }

        return uri.getAuthority().startsWith(getZixieFileProviderName(context)) || uri.getAuthority()
                .startsWith(getAAFInnerFileProviderName(context));
    }

    public static final String getZixieFileProviderName(Context context) {
        return context.getPackageName() + ".bihe0832.fileprovider";
    }

    static final String getAAFInnerFileProviderName(Context context) {
        return context.getPackageName() + ".aaf.inner.fileprovider";
    }

    public static void setFileUriForIntent(Context context, Intent intent, File file, String mine_type) {
        if (BuildUtils.INSTANCE.getSDK_INT() < Build.VERSION_CODES.N) {
            intent.setDataAndType(Uri.fromFile(file), mine_type);
        } else {
            Uri fileProvider = ZixieFileProvider.getZixieFileProvider(context, file);
            intent.setDataAndType(ZixieFileProvider.getZixieFileProvider(context, file), mine_type);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_STREAM, fileProvider);
        }
    }

    public static final String getZixieFilePath(Context context) {
        return getZixieFilePath(context, context.getResources().getString(R.string.lib_bihe0832_file_folder));
    }

    public static final String getZixieFilePath(Context context, String filePath) {
        String subPath = filePath;
        if (TextUtils.isEmpty(subPath)) {
            subPath = context.getResources().getString(R.string.lib_bihe0832_file_folder);
        }

        String absoluteFilePath = "";
        // context.getExternalFilesDir 返回应用的外部存储目录，适用于存储用户可见的文件
        // 存储位置：通常位于 /storage/emulated/0/Android/data/<package_name>/files/。
        File tempFile = context.getExternalFilesDir(subPath);
        if (tempFile == null) {
            // 描述：context.getFilesDir() 返回应用的内部存储目录,与appInfo.dataDir通常一致，适用于存储应用私有的文件。
            // 存储位置：通常位于 /data/data/<package_name>/files/。
            absoluteFilePath = context.getFilesDir().getAbsolutePath() + File.separator + subPath;
        } else {
            absoluteFilePath = tempFile.getAbsolutePath();
        }
        return FileUtils.INSTANCE.getFolderPathWithSeparator(absoluteFilePath);
    }

    public static final String getZixieTempFolder(Context context) {
        return getZixieFilePath(context, FOLDER_TEMP);
    }

    public static final String getZixieCacheFolder(Context context) {
        return getZixieFilePath(context, FOLDER_CACHE);
    }


    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return uri != null && uri.getAuthority().startsWith("com.google.android.apps.photos.content");
    }

    /**
     * Get the value of the data column for this Uri. This is useful for MediaStore Uris, and other file-based
     * ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (IllegalArgumentException ex) {
            ZLog.d(String.format(Locale.getDefault(), "getDataColumn: _data - [%s]", ex.getMessage()));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "";
    }


    public static String getPath(final Context ctx, final Uri uri, boolean needReadFileByPath) {
        if (uri == null || uri.getScheme() == null) {
            ZLog.e("uri is null");
            return "";
        }
        ZLog.e("uri.getScheme()：" + uri.getScheme());
        Context context = ctx.getApplicationContext();
        // DocumentProvider
        if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context,
                uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    if (OSUtils.isAndroidQVersion()) {
                        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + split[1];
                    } else {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), ConvertUtils.parseLong(id, 0L));

                String path = getDataColumn(context, contentUri, null, null);
                if (PermissionChecker.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PermissionChecker.PERMISSION_GRANTED || !needReadFileByPath) {
                    if (!TextUtils.isEmpty(path)) {
                        return path;
                    }
                }
                return FileAction.INSTANCE.copyFileToFolder(context, uri,
                        ZixieFileProvider.getZixieCacheFolder(context));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                String path = getDataColumn(context, contentUri, selection, selectionArgs);
                if (PermissionChecker.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PermissionChecker.PERMISSION_GRANTED || !needReadFileByPath) {
                    if (!TextUtils.isEmpty(path)) {
                        return path;
                    }
                }
                return FileAction.INSTANCE.copyFileToFolder(context, uri,
                        ZixieFileProvider.getZixieCacheFolder(context));
            } else {
                return FileAction.INSTANCE.copyFileToFolder(context, uri,
                        ZixieFileProvider.getZixieCacheFolder(context));
            }
        } else if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())) {
            if (PermissionChecker.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PermissionChecker.PERMISSION_GRANTED || !needReadFileByPath) {
                String path = getDataColumn(context, uri, null, null);
                if (!TextUtils.isEmpty(path)) {
                    return path;
                }
            }
            return FileAction.INSTANCE.copyFileToFolder(context, uri, ZixieFileProvider.getZixieCacheFolder(context));
        } else if (ContentResolver.SCHEME_FILE.equalsIgnoreCase(uri.getScheme())) {
            //此uri为文件，并且path不为空(保存在沙盒内的文件可以随意访问，外部文件path则为空)
            return uri.getPath();
        }
        return "";
    }

    /**
     * 将uri转换为file uri类型为file的直接转换出路径 uri类型为content的将对应的文件复制到沙盒内的cache目录下进行操作
     *
     * @param context 上下文
     * @param uri uri
     * @return file
     */
    public static File uriToFile(Context context, Uri uri, boolean needReadFileByPath) {
        String filePath = getPath(context, uri, needReadFileByPath);
        return new File(filePath);
    }

    public static File uriToFile(Context context, Uri uri) {
        return uriToFile(context, uri, true);
    }
}
