package com.bihe0832.android.lib.install;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.file.mimetype.FileMimeTypes;
import com.bihe0832.android.lib.file.provider.ZixieFileProvider;
import com.bihe0832.android.lib.install.apk.APKInstall;
import com.bihe0832.android.lib.install.obb.OBBFormats;
import com.bihe0832.android.lib.install.obb.ObbFileInstall;
import com.bihe0832.android.lib.install.splitapk.SplitApksInstallHelper;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.ui.toast.ToastUtil;
import com.bihe0832.android.lib.utils.intent.IntentUtils;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import com.bihe0832.android.lib.zip.ZipUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import org.jetbrains.annotations.NotNull;


/**
 * Created by zixie on 2017/11/1.
 * <p>
 * 使用InstallUtils的前提是要按照  {@link ZixieFileProvider }的说明 定义好 lib_bihe0832_file_folder 和 zixie_file_paths.xml 或者直接将文件放在
 * {@link ZixieFileProvider#getZixieFilePath(Context)} 的子目录
 * <p>
 * 如果不使用库自定义的fileProvider，请使用 {@link InstallUtils#installAPP(Context, Uri, File)} 安装 }，此时无需关注上述两个定义
 */

public class InstallUtils {

    public static final String TAG = "InstallUtils";

    public static ApkInstallType getFileType(String filepath) {
        if (TextUtils.isEmpty(filepath)) {
            return ApkInstallType.NULL;
        } else if (FileMimeTypes.INSTANCE.isApkFile(filepath)) {
            return ApkInstallType.APK;
        } else {
            File apkFile = new File(filepath);
            if (apkFile.isDirectory()) {
                return getApkInstallTypeByFolder(apkFile);
            } else {
                return getApkInstallTypeByZip(filepath);
            }
        }
    }

    public static boolean hasInstallAPPPermission(final Context context, boolean showToast, boolean autoSettings) {
        boolean haveInstallPermission = true;
        if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.O) {
            //先获取是否有安装未知来源应用的权限
            try {
                haveInstallPermission = context.getPackageManager().canRequestPackageInstalls();
            } catch (Exception e) {
                haveInstallPermission = false;
                e.printStackTrace();
            }
            if (!haveInstallPermission) {
                if (showToast) {
                    ToastUtil.showShort(context, context.getString(R.string.install_permission));
                }
                if (autoSettings) {
                    IntentUtils.startAppSettings(context, Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                }
            }
        }
        return haveInstallPermission;
    }

    public static void uninstallAPP(final Context context, final String packageName) {
        APKInstall.unInstallAPK(context, packageName);
    }

    public static void installAPP(Context context, Uri fileProvider, File file) {
        APKInstall.installAPK(context, fileProvider, file, null);
    }

    public static void installAPP(final Context context, final String filePath) {
        installAPP(context, filePath, "");
    }

    public static void installAPP(final Context context, final String filePath, final String packageName) {
        installAPP(context, filePath, packageName, null);
    }

    public static void installAPP(final Context context, final String filePath, final String packageName,
            final InstallListener listener) {
        if (hasInstallAPPPermission(context, true, true)) {
            ThreadManager.getInstance().start(new Runnable() {
                @Override
                public void run() {
                    installAllAPK(context, filePath, packageName, new InstallListener() {
                        @Override
                        public void onInstallTimeOut() {
                            ZLog.d(TAG + " installAllApk onInstallTimeOut");
                            if (listener != null) {
                                listener.onInstallTimeOut();
                            }
                        }

                        @Override
                        public void onInstallSuccess() {
                            ZLog.d(TAG + " installAllApk onInstallSuccess");
                            if (listener != null) {
                                listener.onInstallSuccess();
                            }
                        }

                        @Override
                        public void onUnCompress() {
                            ZLog.d(TAG + " installAllApk onUnCompress");
                            if (listener != null) {
                                listener.onUnCompress();
                            }
                        }

                        @Override
                        public void onInstallPrepare() {
                            ZLog.d(TAG + " installAllApk onInstallPrepare");
                            if (listener != null) {
                                listener.onInstallPrepare();
                            }
                        }

                        @Override
                        public void onInstallStart() {
                            ZLog.d(TAG + " installAllApk onInstallStart");
                            if (listener != null) {
                                listener.onInstallStart();
                            }
                        }

                        @Override
                        public void onInstallFailed(int errorcode) {
                            ZLog.d(TAG + " installAllApk onInstallFailed : " + errorcode);
                            if (listener != null) {
                                listener.onInstallFailed(errorcode);
                            }
                        }
                    });
                }
            });
        }
    }

    static void installAllAPK(final Context context, final String filePath, final String packageName,
            final InstallListener listener) {
        try {
            final File downloadedFile = new File(filePath);
            ZLog.d(TAG + "installAllApk downloadedFile:" + downloadedFile.getAbsolutePath());
            if (downloadedFile == null || !downloadedFile.exists()) {
                listener.onInstallFailed(InstallErrorCode.FILE_NOT_FOUND);
                return;
            }
            if (FileMimeTypes.INSTANCE.isApkFile(filePath)) {
                APKInstall.installAPK(context, downloadedFile.getAbsolutePath(), packageName, listener);
            } else if (ZipUtils.isZipFile(downloadedFile.getAbsolutePath(), true)) {
                installSpecialAPKByZip(context, filePath, packageName, listener);
            } else {
                if (!downloadedFile.isDirectory()) {
                    APKInstall.installAPK(context, downloadedFile.getAbsolutePath(), packageName, listener);
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

    static void addApkToFilesList(File folder, ArrayList<String> files) {
        if (folder != null && folder.isDirectory() && folder.listFiles() != null) {
            for (File tempFile : folder.listFiles()) {
                addApkToFilesList(tempFile, files);
            }
        } else {
            if (FileMimeTypes.INSTANCE.isApkFile(folder.getName())) {
                files.add(folder.getAbsolutePath());
            }
        }
    }

    static void installSpecialAPKByZip(@NotNull Context context, String zipFilePath, String packageName,
            final InstallListener listener) {
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
            String fileDir = ZixieFileProvider.getZixieCacheFolder(context) + packageName;
            ZLog.d(TAG + "installSpecialAPKByZip start unCompress:");
            listener.onUnCompress();
            ZipUtils.unCompress(zipFilePath, fileDir);
            ZLog.d(TAG + "installSpecialAPKByZip finished unCompress ");
            ArrayList<String> files = new ArrayList<>();
            addApkToFilesList(new File(fileDir), files);
            SplitApksInstallHelper.INSTANCE.installApk(context, files, finalPackageName, listener);
        } else if (apkInstallType == ApkInstallType.APK) {
            String fileDir = ZixieFileProvider.getZixieCacheFolder(context) + packageName;
            ZLog.d(TAG + "installSpecialAPKByZip start unCompress:");
            listener.onUnCompress();
            ZipUtils.unCompress(zipFilePath, fileDir);
            ZLog.d(TAG + "installSpecialAPKByZip finished unCompress ");
            installSpecialAPKByFolder(context, fileDir, finalPackageName, listener);
        } else {
            listener.onInstallFailed(InstallErrorCode.BAD_APK_TYPE);
        }
    }

    static void installSpecialAPKByFolder(@NotNull Context context, String folderPath, String packageName,
            final InstallListener listener) {
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
            ArrayList<String> files = new ArrayList<>();
            addApkToFilesList(new File(folderPath), files);
            SplitApksInstallHelper.INSTANCE.installApk(context, files, finalPackageName, listener);
        } else if (apkInstallType == ApkInstallType.APK) {
            boolean hasInstall = false;
            for (File file2 : new File(folderPath).listFiles()) {
                if (FileMimeTypes.INSTANCE.isApkFile(file2.getAbsolutePath())) {
                    hasInstall = true;
                    APKInstall.installAPK(context, file2.getAbsolutePath(), "", listener);
                    break;
                }
            }
            if (!hasInstall) {
                listener.onInstallFailed(InstallErrorCode.BAD_APK_TYPE);
            }
        } else {
            listener.onInstallFailed(InstallErrorCode.BAD_APK_TYPE);
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
            } else if (FileMimeTypes.INSTANCE.isApkFile(fileName)) {
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
            return ApkInstallType.NULL;
        }
        int apkFileCount = 0;

        LinkedList<File> folderList = new LinkedList<>();
        for (File file2 : apkInstallFile.listFiles()) {
            if (file2.isDirectory()) {
                folderList.add(file2);
            } else {
                if (OBBFormats.isObbFile(file2.getAbsolutePath())) {
                    return ApkInstallType.OBB;
                } else if (FileMimeTypes.INSTANCE.isApkFile(file2.getAbsolutePath())) {
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
                    } else if (FileMimeTypes.INSTANCE.isApkFile(file2.getAbsolutePath())) {
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

    public enum ApkInstallType {
        NULL, APK, OBB, SPLIT_APKS
    }
}
