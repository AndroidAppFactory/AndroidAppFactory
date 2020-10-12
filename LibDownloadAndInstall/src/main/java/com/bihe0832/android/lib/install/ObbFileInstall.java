package com.bihe0832.android.lib.install;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.install.obb.OBBFormats;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.zip.ZipUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2020/9/25.
 * Description: Description
 */
class ObbFileInstall {
    private static final String TAG = "ObbFileInstall";

    static boolean installObbAPKByFile(@NotNull Context context, @NonNull String fileDir, String packageName) {
        try {

            if (!FileUtils.INSTANCE.checkStoragePermissions(context)) {
                ZLog.d(TAG + "prepare4InstallObb checkPermissions failed");
                return false;
            }
            File obbFolder = OBBFormats.getObbDir(packageName);
            File[] a = (new File(fileDir)).listFiles();
            String dstApkFilePath = "";
            for (File tempFile : a) {
                if (OBBFormats.isObbFile(tempFile.getName())) {
                    if (!obbFolder.exists() && !obbFolder.mkdirs()) {
                        return false;
                    }
                    File targetObbFile = new File(obbFolder.getAbsolutePath() + "/" + FileUtils.INSTANCE.getFileName(tempFile.getAbsolutePath()));
                    ZLog.d(TAG + "installObbAPKByZip start copyFile");
                    if (targetObbFile.exists()) {
                        targetObbFile.deleteOnExit();
                    }
                    FileUtils.INSTANCE.copyFile(tempFile, targetObbFile);
                    ZLog.d(TAG + "installObbAPKByZip finished copyFile");
                    if (!FileUtils.INSTANCE.checkFileExist(targetObbFile.getAbsolutePath())) {
                        return false;
                    }
                } else if (InstallUtils.isApkFile(tempFile.getName())) {
                    dstApkFilePath = tempFile.getAbsolutePath();
                }
            }
            return APKInstall.installAPK(context, dstApkFilePath);
        } catch (Exception e) {
            ZLog.d(TAG + "prepare4InstallObb failed, for " + e);
            return false;
        }
    }

    static boolean installObbAPKByZip(@NotNull Context context, @NonNull String zipFile, String packageName) {
        try {
            if (!FileUtils.INSTANCE.checkStoragePermissions(context)) {
                ZLog.d(TAG + "prepare4InstallObb checkPermissions failed");
                return false;
            }

            File obbFolder = OBBFormats.getObbDir(packageName);
            File targetFolder = new File(FileUtils.INSTANCE.getZixieFilePath(context) + "/" + packageName);
            if (!targetFolder.exists() && !targetFolder.mkdirs()) {
                return false;
            }
            String dstApkFilePath = "";
            for (String fileName : ZipUtils.getFileList(zipFile)) {
                if (OBBFormats.isObbFile(fileName)) {
                    if (!obbFolder.exists() && !obbFolder.mkdirs()) {
                        return false;
                    }
                    File targetObbFile = new File(obbFolder.getAbsolutePath() + "/" + fileName);
                    targetObbFile.deleteOnExit();
                    ZLog.d(TAG + "installObbAPKByZip unCompress start");
                    ZipUtils.unCompress(zipFile, fileName, obbFolder.getAbsolutePath());
                    ZLog.d(TAG + "installObbAPKByZip unCompress finish");
                    if (!FileUtils.INSTANCE.checkFileExist(targetObbFile.getAbsolutePath())) {
                        return false;
                    }
                } else if (InstallUtils.isApkFile(fileName)) {
                    dstApkFilePath = targetFolder.getAbsolutePath() + "/" + fileName;
                }
                ZipUtils.unCompress(zipFile, fileName, targetFolder.getAbsolutePath());
            }
            ZLog.d(TAG + "installObbAPKByZip unCompress start");
            return APKInstall.installAPK(context, dstApkFilePath);
        } catch (Exception e) {
            ZLog.d(TAG + "prepare4InstallObb failed, for " + e);
            return false;
        }
    }
}
