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

import static com.bihe0832.android.lib.install.InstallErrorCode.BAD_APK_TYPE;

/**
 * Created by zixie on 2017/11/1.
 * <p>
 * 使用InstallUtils的前提是要按照  {@link FileUtils }的说明 定义好
 * lib_bihe0832_file_folder 和 zixie_file_paths.xml
 * 如果不使用库自定义的fileProvider，请使用 {@link InstallUtils#installAPP(Context, Uri, File)} 安装 }，此时无需关注上述两个定义
 */

public class InstallUtils {
    private static final String TAG = "InstallUtils";
    private static final String APK_FILE_SUFFIX = ".apk";

    public enum ApkInstallType {
        NULL,
        APK,
        OBB,
        SPLIT_APKS
    }


    public static ApkInstallType getFileType(String filepath) {
        if (TextUtils.isEmpty(filepath)) {
            return ApkInstallType.NULL;
        }
        File apkFile = new File(filepath);
        if (apkFile.isDirectory()) {
            return getApkInstallTypeByFolder(apkFile);
        } else {
            return getApkInstallTypeByZip(filepath);
        }
    }


    public static boolean isApkFile(String filename) {
        return filename.endsWith(APK_FILE_SUFFIX);
    }

    public static void installAPP(Context context, Uri fileProvider, File file) {
        APKInstall.realInstallAPK(context, fileProvider, file, null);
    }

    public static void installAPP(final Context context, final String filePath) {
        installAPP(context, filePath, "");
    }

    public static void installAPP(final Context context, final String filePath, final String packageName) {
        installAllAPK(context, filePath, packageName, null);
    }

    public static void installAPP(final Context context, final String filePath, final String packageName, final InstallListener listener) {
        ThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                installAllAPK(context, filePath, packageName, new InstallListener() {
                    @Override
                    public void onUnCompress() {
                        if (listener != null) {
                            listener.onUnCompress();
                        }
                    }

                    @Override
                    public void onInstallPrepare() {
                        if (listener != null) {
                            listener.onInstallPrepare();
                        }
                    }

                    @Override
                    public void onInstallStart() {
                        if (listener != null) {
                            listener.onInstallStart();
                        }
                    }

                    @Override
                    public void onInstallFailed(int errorcode) {
                        if (listener != null) {
                            listener.onInstallFailed(errorcode);
                        }
                    }
                });
            }
        });
    }


    static void installAllAPK(final Context context, final String filePath, final String packageName, final InstallListener listener) {
        try {
            final File downloadedFile = new File(filePath);
            ZLog.d(TAG + "installAllApk downloadedFile:" + downloadedFile.getAbsolutePath());
            if (downloadedFile == null || !downloadedFile.exists()) {
                listener.onInstallFailed(InstallErrorCode.FILE_NOT_FOUND);
                return;
            }
            if (isApkFile(filePath)) {
                APKInstall.installAPK(context, downloadedFile.getAbsolutePath(), listener);
            } else if (ZipUtils.isZipFile(downloadedFile.getAbsolutePath())) {
                installSpecialAPKByZip(context, filePath, packageName, listener);
            } else {
                if (!downloadedFile.isDirectory()) {
                    APKInstall.installAPK(context, downloadedFile.getAbsolutePath(), listener);
                } else {
                    installSpecialAPKByFolder(context, downloadedFile.getAbsolutePath(), packageName, listener);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ZLog.d(TAG + "installAllApk failed:" + e.getMessage());
            listener.onInstallFailed(InstallErrorCode.UNKNOWN_EXCEPTION);
        }
    }

    static void installSpecialAPKByZip(@NotNull Context context, @NonNull String zipFilePath, String packageName, final InstallListener listener) {
        ZLog.d(TAG + "installSpecialAPKByZip:" + zipFilePath);
        String finalPackageName = "";
        if (TextUtils.isEmpty(packageName)) {
            finalPackageName = FileUtils.INSTANCE.getFileNameWithoutEx(zipFilePath);
        } else {
            finalPackageName = packageName;
        }

        ApkInstallType apkInstallType = getApkInstallTypeByZip(zipFilePath);
        if (apkInstallType == ApkInstallType.OBB) {
            ObbFileInstall.installObbAPKByZip(context, zipFilePath, finalPackageName, listener);
        } else if (apkInstallType == ApkInstallType.SPLIT_APKS) {
            String fileDir = FileUtils.INSTANCE.getZixieFilePath(context) + "/" + packageName;
            ZLog.d(TAG + "installSpecialAPKByZip start unCompress:");
            listener.onUnCompress();
            ZipUtils.unCompress(zipFilePath, fileDir);
            ZLog.d(TAG + "installSpecialAPKByZip finished unCompress ");
            SplitApksInstallHelper.INSTANCE.installApk(context, new File(fileDir), finalPackageName, listener);
        } else {
            listener.onInstallFailed(BAD_APK_TYPE);
        }
    }

    static void installSpecialAPKByFolder(@NotNull Context context, @NonNull String folderPath, String packageName, final InstallListener listener) {
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
            ObbFileInstall.installObbAPKByFile(context, folderPath, finalPackageName, listener);
        } else if (apkInstallType == ApkInstallType.SPLIT_APKS) {
            SplitApksInstallHelper.INSTANCE.installApk(context, new File(folderPath), finalPackageName, listener);
        } else {
            listener.onInstallFailed(BAD_APK_TYPE);
        }
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

        if (apkFileCount > 1) {
            return ApkInstallType.SPLIT_APKS;
        } else if (apkFileCount > 0) {
            return ApkInstallType.APK;
        } else {
            return ApkInstallType.NULL;
        }
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
        if (apkFileCount > 1) {
            return ApkInstallType.SPLIT_APKS;
        } else if (apkFileCount > 0) {
            return ApkInstallType.APK;
        } else {
            return ApkInstallType.NULL;
        }
    }
}
