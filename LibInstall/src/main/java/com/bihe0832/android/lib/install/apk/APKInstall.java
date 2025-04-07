package com.bihe0832.android.lib.install.apk;

import static com.bihe0832.android.lib.install.InstallErrorCode.FILE_NOT_FOUND;
import static com.bihe0832.android.lib.install.InstallErrorCode.START_SYSTEM_INSTALL_EXCEPTION;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.bihe0832.android.lib.file.provider.ZixieFileProvider;
import com.bihe0832.android.lib.install.InstallListener;
import com.bihe0832.android.lib.install.splitapk.SplitApksInstallHelper;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import java.io.File;
import java.util.ArrayList;

/**
 * @author zixie code@bihe0832.com Created on 2020/9/25. Description: Description
 */
public class APKInstall {

    private static String INSTALL_TYPE = "application/vnd.android.package-archive";


    public static void installAPK(Context context, String filePath, String finalPackageName,
            final InstallListener listener) {
        if (!TextUtils.isEmpty(filePath) && !TextUtils.isEmpty(finalPackageName)) {
            try {
                ArrayList<String> fileList = new ArrayList<>();
                fileList.add(filePath);
                SplitApksInstallHelper.INSTANCE.installApk(context, fileList, finalPackageName, listener);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                listener.onInstallFailed(START_SYSTEM_INSTALL_EXCEPTION);
            }
        } else {
            File file = new File(filePath);
            Uri fileProvider = ZixieFileProvider.getZixieFileProvider(context, file);
            installAPK(context, fileProvider, file, listener);
        }
    }

    public static void installAPK(Context context, Uri fileProvider, File file, final InstallListener listener) {
        if (file != null && file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                if (BuildUtils.INSTANCE.getSDK_INT() < Build.VERSION_CODES.N) {
                    intent.setDataAndType(Uri.fromFile(file), INSTALL_TYPE);
                } else {
                    intent.setDataAndType(fileProvider, INSTALL_TYPE);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                if (BuildUtils.INSTANCE.getSDK_INT() > Build.VERSION_CODES.O && !context.getPackageManager()
                        .canRequestPackageInstalls()) {
                    Log.e("InstallUtils", "app don't hava install permission");
                    Log.e("InstallUtils", "app don't hava install permission");
                    Log.e("InstallUtils", "app don't hava install permission");
                }
                context.startActivity(intent);
                if (null != listener) {
                    listener.onInstallStart();
                }
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                if (null != listener) {
                    listener.onInstallFailed(START_SYSTEM_INSTALL_EXCEPTION);
                }
            }
        } else {
            if (null != listener) {
                listener.onInstallFailed(FILE_NOT_FOUND);
            }
        }
    }

    public static void unInstallAPK(Context context, String packageName) {

        try {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}
