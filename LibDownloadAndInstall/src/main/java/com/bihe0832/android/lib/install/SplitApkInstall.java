package com.bihe0832.android.lib.install;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.file.ZipCompressor;
import com.bihe0832.android.lib.install.obb.OBBFormats;
import com.bihe0832.android.lib.install.splitapk.SplitApksInstallHelper;
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
class SplitApkInstall {
    private static final String TAG = "ObbFileInstall";

    static boolean installSplitAPKByZip(@NotNull Context context, @NonNull String filePath, String packageName) {
        try {
            final File downloadedFile = new File(filePath);
            ZipFile zipFile;
            try {
                zipFile = new ZipFile(downloadedFile);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            String fileDir = FileUtils.INSTANCE.getZixieFilePath(context) + "/Download/" + packageName + "/";
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String zipEntryName = zipEntry.getName();
                String filename = new File(zipEntryName).getName();
                File dstApkFile = new File(fileDir, filename);
                File dstApkParentDir = dstApkFile.getParentFile();
                if (!dstApkParentDir.exists() && !dstApkParentDir.mkdirs()) {
                    return false;
                }
                if (!APKInstall.isApkFile(filename)) {
                    continue;
                }
                ZipCompressor.unzip2Dst(zipFile, zipEntry, dstApkFile);
            }
            return SplitApksInstallHelper.INSTANCE.installApk(context, new File(fileDir));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
