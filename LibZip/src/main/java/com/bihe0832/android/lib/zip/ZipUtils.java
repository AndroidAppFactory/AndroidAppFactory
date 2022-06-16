package com.bihe0832.android.lib.zip;

import android.text.TextUtils;

import com.bihe0832.android.lib.file.mimetype.FileMimeTypes;
import com.bihe0832.android.lib.file.FileUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZipUtils {
    private static final int BUFFER = 8 * 1024;

    public static boolean isZipFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        try {
            ZipFile zipFile = new ZipFile(filePath);
            return zipFile.isValidZipFile();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean isZipFile(String filePath, boolean justCheckSuffix) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        if(justCheckSuffix){
            return FileMimeTypes.INSTANCE.isArchive(filePath);
        }else {
            try {
                ZipFile zipFile = new ZipFile(filePath);
                return zipFile.isValidZipFile();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

    }

    public static void compress(String srcPathName, String targetFilePath) {
        zip(srcPathName, targetFilePath);
    }

    public static void compress(List<String> paths, String fileName) {
        zip(paths, fileName);
    }

    public static void zip(String srcPathName, String targetFilePath) {
        zip(srcPathName, targetFilePath, "");
    }

    public static void zip(List<String> paths, String fileName) {
        zip(paths, fileName, "");
    }

    public static boolean unCompress(String srcPath, String fileDir) {
        return unzip(srcPath, fileDir);
    }

    public static boolean unzip(String srcPath, String fileDir) {
        try {
            return unzipAll(srcPath, fileDir, null);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean unCompress(String srcPath, String fileName, String fileDir) {
        return unzip(srcPath, fileName, fileDir);
    }

    public static boolean unzip(String srcPath, String fileName, String fileDir) {
        try {
            return unzipFile(srcPath, fileName, fileDir, null);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean unCompressWithOutPath(String zipFileName, String sourceFileName, String dstFolder) {
        return unzipFileWithOutPath(zipFileName, sourceFileName, dstFolder);
    }

    public static boolean unzipFileWithOutPath(String zipFileName, String sourceFileName, String dstFolder) {
        return unzipFile(zipFileName, sourceFileName, dstFolder, FileUtils.INSTANCE.getFileName(sourceFileName), null);
    }

    public static List<String> getFileList(String zipFileName) {
        return getFileList(zipFileName, "");
    }

    public static List<String> getFileList(String zipFileName, String password) {
        if (TextUtils.isEmpty(zipFileName)) {
            return Collections.emptyList();
        }
        ArrayList<String> fileNameList = new ArrayList<>();
        try {
            ZipFile zipFile = new ZipFile(zipFileName);
            if (zipFile.isEncrypted() && password != null) {
                zipFile.setPassword(password);
            }
            for (Object header : zipFile.getFileHeaders()) {
                if (header instanceof FileHeader) {
                    fileNameList.add(((FileHeader) header).getFileName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileNameList;
    }

    private static void zip(String srcPathName, String targetFilePath, String password) {
        if (TextUtils.isEmpty(srcPathName) || TextUtils.isEmpty(targetFilePath)) {
            return;
        }
        try {
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(5);
            if (password.length() > 0) {
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(Zip4jConstants.COMP_AES_ENC);
                parameters.setAesKeyStrength(Zip4jConstants.DEFLATE_LEVEL_FAST);
                parameters.setPassword(password);
            }
            ZipFile zipFile = new ZipFile(targetFilePath);
            zipFile.setRunInThread(false);
            File targetFile = new File(srcPathName);
            if (targetFile.isDirectory()) {
                zipFile.addFolder(targetFile, parameters);
            } else {
                zipFile.addFile(targetFile, parameters);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean zip(List<String> list, String targetFilePath, String password) {
        if (list == null || list.size() == 0 || TextUtils.isEmpty(targetFilePath)) {
            return false;
        }
        try {
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(5);
            if (password.length() > 0) {
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(Zip4jConstants.COMP_AES_ENC);
                parameters.setAesKeyStrength(Zip4jConstants.DEFLATE_LEVEL_FAST);
                parameters.setPassword(password);
            }
            ZipFile zipFile = new ZipFile(targetFilePath);
            zipFile.setRunInThread(false);
            ArrayList<File> fileList = new ArrayList<>();
            for (String filePath : list) {
                fileList.add(new File(filePath));
            }
            zipFile.addFiles(fileList, parameters);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private static boolean unzipAll(String zipFileName, String dstFile, char[] password) {
        if (TextUtils.isEmpty(zipFileName) || TextUtils.isEmpty(dstFile)) {
            return false;
        }
        try {
            ZipFile zipFile = new ZipFile(zipFileName);
            if (zipFile.isEncrypted() && password != null) {
                zipFile.setPassword(password);
            }
            zipFile.setRunInThread(false);
            zipFile.extractAll(dstFile);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean unzipFile(String zipFileName, String sourceFileName, String dstFolder, char[] password) {
        return unzipFile(zipFileName, sourceFileName, dstFolder, null, password);
    }

    private static boolean unzipFile(String zipFileName, String sourceFileName, String dstFolder, String destFileName, char[] password) {
        if (TextUtils.isEmpty(zipFileName) || TextUtils.isEmpty(sourceFileName) || TextUtils.isEmpty(dstFolder)) {
            return false;
        }
        try {
            ZipFile zipFile = new ZipFile(zipFileName);
            if (zipFile.isEncrypted() && password != null) {
                zipFile.setPassword(password);
            }
            zipFile.setRunInThread(false);
            for (Object header : zipFile.getFileHeaders()) {
                if (header instanceof FileHeader && ((FileHeader) header).getFileName().equalsIgnoreCase(sourceFileName)) {
                    zipFile.extractFile((FileHeader) header, dstFolder, null, destFileName);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}