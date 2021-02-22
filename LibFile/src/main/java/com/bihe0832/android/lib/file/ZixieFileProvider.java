package com.bihe0832.android.lib.file;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.setDataAndType(Uri.fromFile(file), mine_type);
        } else {
            intent.setDataAndType(ZixieFileProvider.getZixieFileProvider(context, file), mine_type);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
    }
    @NotNull
    public static final String getZixieFilePath(@NotNull Context context) {
        File tempFile = context.getExternalFilesDir(context.getString(R.string.lib_bihe0832_file_folder));
        FileUtils.INSTANCE.checkAndCreateFolder(tempFile.getAbsolutePath());
        if (!tempFile.getAbsolutePath().endsWith(File.separator)) {
            return tempFile.getAbsolutePath() + File.separator;
        } else {
            return tempFile.getAbsolutePath();
        }
    }

    @NotNull
    public static final String getZixieFilePath(@NotNull Context context, String filePath) {
        File zixieFileFolder = context.getExternalFilesDir(context.getString(R.string.lib_bihe0832_file_folder));
        File tempFile = null;
        if (!TextUtils.isEmpty(filePath)) {
            tempFile = new File(zixieFileFolder.getAbsolutePath(), filePath);
        } else {
            tempFile = zixieFileFolder;
        }
        FileUtils.INSTANCE.checkAndCreateFolder(tempFile.getAbsolutePath());
        if (!tempFile.getAbsolutePath().endsWith(File.separator)) {
            return tempFile.getAbsolutePath() + File.separator;
        } else {
            return tempFile.getAbsolutePath();
        }
    }
}
