package com.bihe0832.android.lib.install;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.file.ZipCompressor;
import com.bihe0832.android.lib.install.obb.OBBFormats;
import com.bihe0832.android.lib.log.ZLog;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2020/9/25.
 * Description: Description
 */
class ObbFileInstall {
    private static final String TAG = "ObbFileInstall";

    static boolean installObbAPKByZip(@NotNull Context context, @NonNull String filePath, String packageName) {
        try {

            if (!FileUtils.INSTANCE.checkStoragePermissions(context)) {
                ZLog.d(TAG + "prepare4InstallObb checkPermissions failed");
                return false;
            }
            File downloadedFile = new File(filePath);
            File obbFolder = OBBFormats.getObbDir(packageName);
            ZipFile zipFile;
            try {
                zipFile = new ZipFile(downloadedFile);
            } catch (Exception e) {
                ZLog.d(TAG + "prepare4InstallObb " + downloadedFile + "maybe not zip file");
                return false;
            }
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            String fileDir = FileUtils.INSTANCE.getZixieFilePath(context)+ "/" + packageName ;
            String dstApkFilePath = "";
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String zipEntryName = zipEntry.getName();
                String filename = new File(zipEntryName).getName();
                if (OBBFormats.isObbFile(filename)) {
                    File dstObbFile = new File(obbFolder, filename);
                    File dstObbParentDir = dstObbFile.getParentFile();
                    if (!dstObbParentDir.exists() && !dstObbParentDir.mkdirs()) {
                        return false;
                    }
                    ZipCompressor.unzip2Dst(zipFile, zipEntry, dstObbFile);
                } else if (APKInstall.isApkFile(filename)) {
                    File dstApkFile = new File(fileDir, filename);
                    File dstApkFileParentDir = dstApkFile.getParentFile();
                    if (!dstApkFileParentDir.exists() && !dstApkFileParentDir.mkdirs()) {
                        return false;
                    }
                    dstApkFilePath = dstApkFile.getAbsolutePath();
                    ZipCompressor.unzip2Dst(zipFile, zipEntry, dstApkFile);
                }
            }
            return APKInstall.installAPK(context, dstApkFilePath);
        } catch (Exception e) {
            ZLog.d(TAG + "prepare4InstallObb failed, for " + e);
            return false;
        }
    }
}
