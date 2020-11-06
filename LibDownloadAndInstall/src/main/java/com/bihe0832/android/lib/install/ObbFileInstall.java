package com.bihe0832.android.lib.install;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.install.obb.OBBFormats;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.zip.ZipUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.bihe0832.android.lib.install.InstallErrorCode.PERMISSION_DENY;
import static com.bihe0832.android.lib.install.InstallErrorCode.UNKNOWN_EXCEPTION;
import static com.bihe0832.android.lib.install.InstallErrorCode.UNZIP_FAILED;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2020/9/25.
 * Description: Description
 */
class ObbFileInstall {
    private static final String TAG = "ObbFileInstall";

    static void installObbAPKByFile(@NotNull Context context, @NonNull String fileDir, String packageName, final InstallListener listener) {
        try {

            if (!FileUtils.INSTANCE.checkStoragePermissions(context)) {
                ZLog.d(TAG + "prepare4InstallObb checkPermissions failed");
                listener.onInstallFailed(PERMISSION_DENY);
                return;
            }
            File obbFolder = OBBFormats.getObbDir(packageName);
            File[] a = (new File(fileDir)).listFiles();
            String dstApkFilePath = "";
            for (File tempFile : a) {
                if (OBBFormats.isObbFile(tempFile.getName())) {
                    if (!obbFolder.exists() && !obbFolder.mkdirs()) {
                        listener.onInstallFailed(UNZIP_FAILED);
                        return;
                    }
                    File targetObbFile = new File(obbFolder.getAbsolutePath() + "/" + FileUtils.INSTANCE.getFileName(tempFile.getAbsolutePath()));
                    ZLog.d(TAG + "installObbAPKByZip start copyFile");
                    if (targetObbFile.exists()) {
                        targetObbFile.deleteOnExit();
                    }
                    listener.onInstallPrepare();
                    FileUtils.INSTANCE.copyFile(tempFile, targetObbFile);
                    ZLog.d(TAG + "installObbAPKByZip finished copyFile");
                    if (!FileUtils.INSTANCE.checkFileExist(targetObbFile.getAbsolutePath())) {
                        listener.onInstallFailed(UNZIP_FAILED);
                        return;
                    }
                } else if (InstallUtils.isApkFile(tempFile.getName())) {
                    dstApkFilePath = tempFile.getAbsolutePath();
                }
            }
            APKInstall.installAPK(context, dstApkFilePath, listener);
        } catch (Exception e) {
            ZLog.d(TAG + "prepare4InstallObb failed, for " + e);
            listener.onInstallFailed(UNKNOWN_EXCEPTION);
        }
    }

    static void installObbAPKByZip(@NotNull Context context, @NonNull String zipFile, String packageName, final InstallListener listener) {
        try {
            if (!FileUtils.INSTANCE.checkStoragePermissions(context)) {
                ZLog.d(TAG + "prepare4InstallObb checkPermissions failed");
                listener.onInstallFailed(PERMISSION_DENY);
                return;
            }

            File obbFolder = OBBFormats.getObbDir(packageName);
            File targetFolder = new File(FileUtils.INSTANCE.getZixieFilePath(context) + "/" + packageName);
            if (!targetFolder.exists() && !targetFolder.mkdirs()) {
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
                    File targetObbFile = new File(obbFolder.getAbsolutePath() + "/" + fileName);
                    targetObbFile.deleteOnExit();
                    ZLog.d(TAG + "installObbAPKByZip unCompress start");
                    listener.onUnCompress();
                    ZipUtils.unCompress(zipFile, fileName, obbFolder.getAbsolutePath());
                    ZLog.d(TAG + "installObbAPKByZip unCompress finish");
                    if (!FileUtils.INSTANCE.checkFileExist(targetObbFile.getAbsolutePath())) {
                        listener.onInstallFailed(UNZIP_FAILED);
                        return;
                    }
                } else if (InstallUtils.isApkFile(fileName)) {
                    dstApkFilePath = targetFolder.getAbsolutePath() + "/" + fileName;
                }
                listener.onUnCompress();
                ZipUtils.unCompress(zipFile, fileName, targetFolder.getAbsolutePath());
            }
            APKInstall.installAPK(context, dstApkFilePath, listener);
        } catch (Exception e) {
            ZLog.d(TAG + "prepare4InstallObb failed, for " + e);
            listener.onInstallFailed(UNKNOWN_EXCEPTION);
        }
    }
}
