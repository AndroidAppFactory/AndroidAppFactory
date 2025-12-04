package com.bihe0832.android.lib.install.obb;

import static com.bihe0832.android.lib.install.InstallErrorCode.FILE_NOT_FOUND;
import static com.bihe0832.android.lib.install.InstallErrorCode.PERMISSION_DENY;
import static com.bihe0832.android.lib.install.InstallErrorCode.UNKNOWN_EXCEPTION;
import static com.bihe0832.android.lib.install.InstallErrorCode.UNZIP_FAILED;
import static com.bihe0832.android.lib.install.InstallUtils.TAG;

import android.content.Context;
import android.text.TextUtils;

import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.file.mimetype.FileMimeTypes;
import com.bihe0832.android.lib.file.provider.ZixieFileProvider;
import com.bihe0832.android.lib.install.InstallListener;
import com.bihe0832.android.lib.install.apk.APKInstall;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.zip.ZipUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author zixie code@bihe0832.com Created on 2020/9/25. Description: Description
 */
public class ObbFileInstall {

    public static void installObbAPKByFile(@NotNull Context context, String fileDir, String packageName, int delaySeconds,
                                           final InstallListener listener) {
        try {

            if (!FileUtils.INSTANCE.checkStoragePermissions(context)) {
                ZLog.d(TAG, "prepare4InstallObb checkPermissions failed");
                listener.onInstallFailed(PERMISSION_DENY);
                return;
            }
            File obbFolder = OBBFormats.getObbDir(packageName);
            if (!obbFolder.exists() && !obbFolder.mkdirs()) {
                listener.onInstallFailed(UNZIP_FAILED);
                return;
            }
            listener.onInstallPrepare();
            String result = prepareInstallOBB(new File(fileDir), obbFolder, listener);
            if (!TextUtils.isEmpty(result)) {
                if (result.startsWith(ZixieFileProvider.getZixieCacheFolder(context))) {
                    APKInstall.installAPK(context, result, listener);
                } else {
                    String realInstallPath =
                            ZixieFileProvider.getZixieCacheFolder(context) + FileUtils.INSTANCE.getFileName(result);
                    ZLog.d(TAG, "installObbAPKByFile start copy apk File");
                    FileUtils.INSTANCE.copyFile(new File(result), new File(realInstallPath));
                    ZLog.d(TAG, "installObbAPKByFile finished copy apk File");
                    listener.onInstallStart();
                    APKInstall.installAPK(context, realInstallPath, packageName, delaySeconds, listener);
                }
            }
        } catch (Exception e) {
            ZLog.d(TAG, "prepare4InstallObb failed, for " + e);
            listener.onInstallFailed(UNKNOWN_EXCEPTION);
        }
    }

    static String prepareInstallOBB(final File fileDir, File obbFolder, final InstallListener listener) {
        if (fileDir == null || !fileDir.exists()) {
            listener.onInstallFailed(FILE_NOT_FOUND);
            return "";
        }
        String dstApkFilePath = "";
        for (File file2 : fileDir.listFiles()) {
            if (file2.isDirectory()) {
                String result = prepareInstallOBB(file2, obbFolder, listener);
                if (!TextUtils.isEmpty(result)) {
                    dstApkFilePath = result;
                }
            } else {
                if (OBBFormats.isObbFile(file2.getAbsolutePath())) {
                    File targetObbFile = new File(obbFolder.getAbsolutePath() + "/" + FileUtils.INSTANCE.getFileName(
                            file2.getAbsolutePath()));
                    ZLog.d(TAG, "installObbAPKByFile start copy obb File");
                    if (targetObbFile.exists()) {
                        targetObbFile.deleteOnExit();
                    }
                    FileUtils.INSTANCE.copyFile(file2, targetObbFile);
                    ZLog.d(TAG, "installObbAPKByFile finished copy obb File");
                    if (!FileUtils.INSTANCE.checkFileExist(targetObbFile.getAbsolutePath())) {
                        listener.onInstallFailed(UNZIP_FAILED);
                        return "";
                    }
                } else if (FileMimeTypes.INSTANCE.isApkFile(file2.getAbsolutePath())) {
                    dstApkFilePath = file2.getAbsolutePath();
                }
            }
        }
        return dstApkFilePath;

    }

    public static void installObbAPKByZip(@NotNull Context context, String zipFile, String packageName, int delaySeconds,
                                          final InstallListener listener) {
        try {
            if (!FileUtils.INSTANCE.checkStoragePermissions(context)) {
                ZLog.d(TAG, "prepare4InstallObb checkPermissions failed");
                listener.onInstallFailed(PERMISSION_DENY);
                return;
            }

            File obbFolder = OBBFormats.getObbDir(packageName);
            File targetAPKFolder = new File(ZixieFileProvider.getZixieCacheFolder(context) + packageName);
            if (!targetAPKFolder.exists() && !targetAPKFolder.mkdirs()) {
                listener.onInstallFailed(UNZIP_FAILED);
                return;
            }
            String dstApkFilePath = "";
            for (String fileName : ZipUtils.getFileList(zipFile)) {
                if (OBBFormats.isObbFile(fileName)) {
                    if (!obbFolder.exists() && !obbFolder.mkdirs()) {
                        listener.onInstallFailed(UNZIP_FAILED);
                        return;
                    }
                    File newTargetObbFile = new File(
                            obbFolder.getAbsolutePath() + "/" + FileUtils.INSTANCE.getFileName(fileName));
                    newTargetObbFile.deleteOnExit();
                    ZLog.d(TAG, " installObbAPKByZip unCompress to " + newTargetObbFile.getAbsolutePath()
                            + " obb start");
                    listener.onUnCompress();
                    ZipUtils.unCompressWithOutPath(zipFile, fileName, obbFolder.getAbsolutePath());
                    ZLog.d(TAG, " installObbAPKByZip unCompress obb finish");
                    if (!FileUtils.INSTANCE.checkFileExist(newTargetObbFile.getAbsolutePath())) {
                        listener.onInstallFailed(UNZIP_FAILED);
                        return;
                    }
                } else if (FileMimeTypes.INSTANCE.isApkFile(fileName)) {
                    ZLog.d(TAG, " installObbAPKByZip unCompress apk start");
                    listener.onUnCompress();
                    ZipUtils.unCompressWithOutPath(zipFile, fileName, targetAPKFolder.getAbsolutePath());
                    ZLog.d(TAG, " installObbAPKByZip unCompress apk finish");
                    dstApkFilePath = targetAPKFolder.getAbsolutePath() + "/" + FileUtils.INSTANCE.getFileName(fileName);
                }
            }
            listener.onInstallStart();
            APKInstall.installAPK(context, dstApkFilePath, packageName, delaySeconds, listener);
        } catch (Exception e) {
            ZLog.d(TAG, "prepare4InstallObb failed, for " + e);
            listener.onInstallFailed(UNKNOWN_EXCEPTION);
        }
    }
}
