package com.bihe0832.android.lib.install;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.bihe0832.android.lib.file.FileUtils;

import java.io.File;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2020/9/25.
 * Description: Description
 */
class APKInstall {
    private static String INSTALL_TYPE = "application/vnd.android.package-archive";


    static boolean installAPK(Context context, String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                Uri fileProvider = FileUtils.INSTANCE.getZixieFileProvider(context, file);
                return realInstallAPK(context, fileProvider, file);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    static boolean realInstallAPK(Context context, Uri fileProvider, File file) {
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
