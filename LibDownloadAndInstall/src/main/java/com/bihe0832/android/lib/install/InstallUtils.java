package com.bihe0832.android.lib.install;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.install.obb.OBBFormats;
import com.bihe0832.android.lib.install.splitapk.SplitApksInstallHelper;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.thread.ThreadManager;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by zixie on 2017/11/1.
 * <p>
 * 使用InstallUtils的前提是要按照  {@link FileUtils }的说明 定义好
 * lib_bihe0832_file_folder 和 zixie_file_paths.xml
 * 如果不使用库自定义的fileProvider，请使用 {@link InstallUtils#installAPP(Context, Uri, File)} 安装 }，此时无需关注上述两个定义
 */

public class InstallUtils {

    public static boolean installAPP(Context context, Uri fileProvider, File file) {
        return APKInstall.realInstallAPK(context, fileProvider, file);
    }

    public static boolean installAPP(final Context context, final String filePath, final String packageName) {
        return installAllAPK(context, filePath, packageName);
    }

    public static boolean installAPP(final Context context, final String filePath) {
        return installAPP(context, filePath, "");
    }

    private static final String TAG = "InstallUtils";
    private static final String ZIP_DOWNLOADED_FILE_SUFFIX = ".zip";

    enum ApkInstallType {
        APK,
        OBB,
        SPLIT_APKS
    }

    static boolean installAllAPK(final Context context, final String filePath, final String packageName) {
        try {
            final File downloadedFile = new File(filePath);
            ZLog.d(TAG + "installAllApk downloadedFile:" + downloadedFile.getAbsolutePath());
            if (downloadedFile == null) {
                return false;
            }
            if (FileUtils.INSTANCE.isZipFile(downloadedFile)) {
                ThreadManager.getInstance().start(new Runnable() {
                    @Override
                    public void run() {
                        boolean result = installSpecialAPK(context, filePath, packageName);
                        ZLog.d(TAG + "installSpecialAPK result:" + result);
                    }
                });
                return true;
            } else {
                if (!downloadedFile.isDirectory()) {
                    return APKInstall.installAPK(context, downloadedFile.getAbsolutePath());
                } else {
                    return SplitApksInstallHelper.INSTANCE.installApk(context, downloadedFile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ZLog.d(TAG + "installAllApk failed:" + e.getMessage());
            return false;
        }
    }

    static boolean installSpecialAPK(@NotNull Context context, @NonNull String zipFilePath, String packageName) {
        String finalPackageName = "";
        if(TextUtils.isEmpty(packageName)){
            finalPackageName = FileUtils.INSTANCE.getFileNameWithoutEx(zipFilePath);
        }else {
            finalPackageName = packageName;
        }
        ApkInstallType apkInstallType = getApkInstallType(new File(zipFilePath));
        if (apkInstallType == ApkInstallType.OBB) {
            return ObbFileInstall.installObbAPKByZip(context, zipFilePath, finalPackageName);
        } else if (apkInstallType == ApkInstallType.SPLIT_APKS) {
            return SplitApkInstall.installSplitAPKByZip(context, zipFilePath, finalPackageName);
        }
        return false;
    }

    static ApkInstallType getApkInstallType(File apkInstallFile) {
        if (apkInstallFile == null || !apkInstallFile.exists()) {
            return ApkInstallType.APK;
        }
        String apkInstallFileName = apkInstallFile.getName();
        ZLog.d(TAG + "getApkInstallType apkInstallFile:" + apkInstallFile + ",apkInstallFileName:" + apkInstallFileName);
        if (APKInstall.isApkFile(apkInstallFileName)) {
            return ApkInstallType.APK;
        } else if (apkInstallFileName.endsWith(ZIP_DOWNLOADED_FILE_SUFFIX)) {
            ZipFile zipFile;
            try {
                zipFile = new ZipFile(apkInstallFile);
                int apkFileCount = 0;
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = entries.nextElement();
                    String zipEntryName = zipEntry.getName();
                    ZLog.d(TAG + "getApkInstallType zipEntryName:" + zipEntryName);
                    String filename = new File(zipEntryName).getName();
                    ZLog.d(TAG + "getApkInstallType filename:" + filename);
                    if (OBBFormats.isObbFile(filename)) {
                        return ApkInstallType.OBB;
                    }

                    if (APKInstall.isApkFile(filename)) {
                        apkFileCount++;
                    }
                }
                ZLog.d(TAG + "getApkInstallType apkFileCount:" + apkFileCount);
                return apkFileCount > 1 ? ApkInstallType.SPLIT_APKS : ApkInstallType.APK;
            } catch (Exception e) {
                ZLog.e(TAG + "getApkInstallType failed:" + e.getMessage());
            }
        }
        return ApkInstallType.APK;
    }

}
