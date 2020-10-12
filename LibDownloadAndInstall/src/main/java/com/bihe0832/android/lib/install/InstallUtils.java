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
import com.bihe0832.android.lib.zip.ZipUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.LinkedList;

/**
 * Created by zixie on 2017/11/1.
 * <p>
 * 使用InstallUtils的前提是要按照  {@link FileUtils }的说明 定义好
 * lib_bihe0832_file_folder 和 zixie_file_paths.xml
 * 如果不使用库自定义的fileProvider，请使用 {@link InstallUtils#installAPP(Context, Uri, File)} 安装 }，此时无需关注上述两个定义
 */

public class InstallUtils {

    private static final String APK_FILE_SUFFIX = ".apk";

    public static boolean isApkFile(String filename) {
        return filename.endsWith(APK_FILE_SUFFIX);
    }

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
            if (ZipUtils.isZipFile(downloadedFile.getAbsolutePath())) {
                ThreadManager.getInstance().start(new Runnable() {
                    @Override
                    public void run() {
                        boolean result = installSpecialAPKByZip(context, filePath, packageName);
                        ZLog.d(TAG + "installSpecialAPK result:" + result);
                    }
                });
                return true;
            } else {
                if (!downloadedFile.isDirectory()) {
                    return APKInstall.installAPK(context, downloadedFile.getAbsolutePath());
                } else {
                    ThreadManager.getInstance().start(new Runnable() {
                        @Override
                        public void run() {
                            boolean result = installSpecialAPKByFolder(context, downloadedFile.getAbsolutePath(), packageName);
                            ZLog.d(TAG + "installSpecialAPK result:" + result);
                        }
                    });
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ZLog.d(TAG + "installAllApk failed:" + e.getMessage());
            return false;
        }
    }

    static boolean installSpecialAPKByZip(@NotNull Context context, @NonNull String zipFilePath, String packageName) {
        ZLog.d(TAG + "installSpecialAPKByZip:" + zipFilePath);
        String finalPackageName = "";
        if (TextUtils.isEmpty(packageName)) {
            finalPackageName = FileUtils.INSTANCE.getFileNameWithoutEx(zipFilePath);
        } else {
            finalPackageName = packageName;
        }

        ApkInstallType apkInstallType = getApkInstallTypeByZip(zipFilePath);
        if (apkInstallType == ApkInstallType.OBB) {
            return ObbFileInstall.installObbAPKByZip(context, zipFilePath, finalPackageName);
        } else if (apkInstallType == ApkInstallType.SPLIT_APKS) {
            String fileDir = FileUtils.INSTANCE.getZixieFilePath(context) + "/" + packageName;
            ZLog.d(TAG + "installSpecialAPKByZip start unCompress:");
            ZipUtils.unCompress(zipFilePath, fileDir);
            ZLog.d(TAG + "installSpecialAPKByZip finished unCompress ");
            return SplitApksInstallHelper.INSTANCE.installApk(context, new File(fileDir), finalPackageName);
        }
        return false;
    }

    static boolean installSpecialAPKByFolder(@NotNull Context context, @NonNull String folderPath, String packageName) {
        ZLog.d(TAG + "installSpecialAPKByFolder:" + folderPath);
        String finalPackageName = "";
        if (TextUtils.isEmpty(packageName)) {
            finalPackageName = FileUtils.INSTANCE.getFileName(folderPath);
        } else {
            finalPackageName = packageName;
        }

        ApkInstallType apkInstallType = getApkInstallTypeByFolder(new File(folderPath));
        ZLog.d(TAG + "installSpecialAPKByFolder start install:" + folderPath);
        if (apkInstallType == ApkInstallType.OBB) {
            return ObbFileInstall.installObbAPKByFile(context, folderPath, finalPackageName);
        } else if (apkInstallType == ApkInstallType.SPLIT_APKS) {
            return SplitApksInstallHelper.INSTANCE.installApk(context, new File(folderPath), finalPackageName);
        }
        return false;
    }

    static ApkInstallType getApkInstallTypeByZip(String zipFile) {
        if (zipFile == null) {
            return ApkInstallType.APK;
        }
        int apkFileCount = 0;
        for (String fileName : ZipUtils.getFileList(zipFile)) {
            if (OBBFormats.isObbFile(fileName)) {
                return ApkInstallType.OBB;
            } else if (isApkFile(fileName)) {
                if (apkFileCount > 0) {
                    return ApkInstallType.SPLIT_APKS;
                } else {
                    apkFileCount++;
                }
            }
        }
        return apkFileCount > 1 ? ApkInstallType.SPLIT_APKS : ApkInstallType.APK;
    }

    static ApkInstallType getApkInstallTypeByFolder(File apkInstallFile) {
        if (apkInstallFile == null || !apkInstallFile.exists()) {
            return ApkInstallType.APK;
        }
        int apkFileCount = 0;

        LinkedList<File> folderList = new LinkedList<>();
        for (File file2 : apkInstallFile.listFiles()) {
            if (file2.isDirectory()) {
                folderList.add(file2);
            } else {
                if (OBBFormats.isObbFile(file2.getAbsolutePath())) {
                    return ApkInstallType.OBB;
                } else if (isApkFile(file2.getAbsolutePath())) {
                    if (apkFileCount > 0) {
                        return ApkInstallType.SPLIT_APKS;
                    } else {
                        apkFileCount++;
                    }
                }
            }
        }
        File temp_file;
        while (!folderList.isEmpty()) {
            temp_file = folderList.removeFirst();
            for (File file2 : temp_file.listFiles()) {
                if (file2.isDirectory()) {
                    folderList.add(file2);
                } else {
                    if (OBBFormats.isObbFile(file2.getAbsolutePath())) {
                        return ApkInstallType.OBB;
                    } else if (isApkFile(file2.getAbsolutePath())) {
                        if (apkFileCount > 0) {
                            return ApkInstallType.SPLIT_APKS;
                        } else {
                            apkFileCount++;
                        }
                    }
                }
            }
        }
        return apkFileCount > 1 ? ApkInstallType.SPLIT_APKS : ApkInstallType.APK;
    }
}
