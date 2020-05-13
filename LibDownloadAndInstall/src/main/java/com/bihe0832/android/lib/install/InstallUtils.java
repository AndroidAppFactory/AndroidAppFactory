package com.bihe0832.android.lib.install;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bihe0832.android.lib.file.FileUtils;

import java.io.File;

import static android.content.ContentValues.TAG;

/**
 * Created by zixie on 2017/11/1.
 * <p>
 * 使用InstallUtils的前提是要按照  {@link FileUtils }的说明 定义好
 * lib_bihe0832_file_folder 和 zixie_file_paths.xml
 * 如果不使用库自定义的fileProvider，请使用 {@link InstallUtils#installAPP(Context, Uri, File)} 安装 }，此时无需关注上述两个定义
 */

public class InstallUtils {

    private static String INSTALL_TYPE = "application/vnd.android.package-archive";

    public static boolean installAPP(Context context, Uri fileProvider, File file) {
        return realInstallAPP(context, fileProvider, file);
    }

    public static boolean installAPP(Context context, String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                Uri fileProvider = FileUtils.INSTANCE.getZixieFileProvider(context, file);
                return realInstallAPP(context, fileProvider, file);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean realInstallAPP(Context context, Uri fileProvider, File file) {
        if (file != null && file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    intent.setDataAndType(Uri.fromFile(file), INSTALL_TYPE);
                } else {
                    intent.setDataAndType(fileProvider, INSTALL_TYPE);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O && !context.getPackageManager().canRequestPackageInstalls()) {
                    Log.e("InstallUtils", "app don't hava install permission");
                    Log.e("InstallUtils", "app don't hava install permission");
                    Log.e("InstallUtils", "app don't hava install permission");
                }
                context.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }
}
