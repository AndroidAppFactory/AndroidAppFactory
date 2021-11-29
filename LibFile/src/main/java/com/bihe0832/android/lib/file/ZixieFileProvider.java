package com.bihe0832.android.lib.file;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import java.io.File;
import org.jetbrains.annotations.NotNull;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2020-02-30.
 *         ZixieFileProvider 提供的Provider时，可以选择自定路径或者使用库默认路径
 *
 *         如果是选择默认的路径时你需要获取库提供的provider 可以使用接口 {@link ZixieFileProvider#getZixieFileProvider(Context, File)}
 *
 *         如果是选择自定义的，
 *         需要保证文件保存地址为 {@link ZixieFileProvider#getZixieFilePath(Context)} 获取，或者
 *         使用context.getExternalFilesDir(context.getString(R.string.lib_bihe0832_file_folder))得到
 *
 *         并同步做如下操作，否则会造成获取provider失败等问题：
 *
 *         添加以下String值定义：
 *
 *         lib_bihe0832_file_folder：自定义文件目录
 *         在res/xml添加文件：file_paths.xml，内容为：
 *
 *         <?xml version="1.0" encoding="utf-8"?>
 *         <paths>
 *         <external-files-path name="download" path="你自定义的文件目录"/>
 *         </paths>
 */
public class ZixieFileProvider extends FileProvider {

    @NotNull
    public static final Uri getZixieFileProvider(@NotNull Context context, @NotNull File file) {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".bihe0832", file);
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

    @NotNull
    public static final String getZixieFilePath(@NotNull Context context) {
        return getZixieFilePath(context, context.getResources().getString(R.string.lib_bihe0832_file_folder));
    }

    @NotNull
    public static final String getZixieFilePath(@NotNull Context context, String filePath) {
        String subPath = filePath;
        if (TextUtils.isEmpty(subPath)) {
            subPath = context.getResources().getString(R.string.lib_bihe0832_file_folder);
        }

        String absoluteFilePath = "";
        File tempFile = context.getExternalFilesDir(subPath);
        if (tempFile == null) {
            absoluteFilePath = context.getFilesDir().getAbsolutePath() + File.separator + subPath;
        } else {
            absoluteFilePath = tempFile.getAbsolutePath();
        }

        FileUtils.INSTANCE.checkAndCreateFolder(absoluteFilePath);
        if (!absoluteFilePath.endsWith(File.separator)) {
            return absoluteFilePath + File.separator;
        } else {
            return absoluteFilePath;
        }
    }

    public static final String getZixiePhotosPath(@NotNull Context context) {
        String filePath = getZixieFilePath(context) + "pictures" + File.separator;
        if (Build.VERSION.SDK_INT >= 30) {
            //android 11以上，将文件创建在公有目录
            filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
        }

        FileUtils.INSTANCE.checkAndCreateFolder(filePath);
        if (filePath.endsWith(File.separator)) {
            return filePath;
        } else {
            return filePath + File.separator;
        }
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
}
