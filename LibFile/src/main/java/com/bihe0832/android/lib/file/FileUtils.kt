package com.bihe0832.android.lib.file

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.StatFs
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import com.bihe0832.android.lib.file.action.FileAction
import com.bihe0832.android.lib.file.content.FileContent
import com.bihe0832.android.lib.file.content.FileName
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.encrypt.MD5
import com.bihe0832.android.lib.utils.encrypt.SHA256
import java.io.File
import java.io.InputStream
import java.text.DecimalFormat
import java.text.NumberFormat


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-10.
 */
object FileUtils {

    val ILLEGAL_FILENAME_CHARS = charArrayOf(34.toChar(), 60.toChar(), 62.toChar(), 124.toChar(), 0.toChar(), 1.toChar(), 2.toChar(), 3.toChar(), 4.toChar(), 5.toChar(), 6.toChar(), 7.toChar(), 8.toChar(), 9.toChar(), 10.toChar(), 11.toChar(), 12.toChar(), 13.toChar(),
            14.toChar(), 15.toChar(), 16.toChar(), 17.toChar(), 18.toChar(), 19.toChar(), 20.toChar(), 21.toChar(), 22.toChar(), 23.toChar(), 24.toChar(), 25.toChar(), 26.toChar(), 27.toChar(), 28.toChar(), 29.toChar(), 30.toChar(), 31.toChar(), 58.toChar(), 42.toChar(), 63.toChar(), 92.toChar(), 47.toChar())


    const val SPACE_KB = 1024.0
    const val SPACE_MB = 1024 * SPACE_KB
    const val SPACE_GB = 1024 * SPACE_MB
    const val SPACE_TB = 1024 * SPACE_GB

    fun checkStoragePermissions(context: Context?): Boolean {
        context?.let {
            return PackageManager.PERMISSION_GRANTED ==
                    ContextCompat.checkSelfPermission(
                            it,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
        }
        return false
    }

    fun getDirectoryAvailableSpace(filePath: String): Long {
        return try {
            val mStatFs = StatFs(filePath)
            val blockSize = mStatFs.blockSizeLong
            val availableBlocks = mStatFs.availableBlocksLong
            (availableBlocks * blockSize)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            0L
        }
    }

    fun checkAndCreateFolder(path: String): Boolean {
        try {
            File(path).let {
                return if (!it.exists()) {
                    it.mkdirs()
                } else {
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun getFolderPathWithSeparator(path: String): String {
        try {
            var result = checkAndCreateFolder(path)
            if (!result) {
                ZLog.e("file $path is bad !!!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return if (path.endsWith(File.separator)) {
            path
        } else {
            path + File.separator
        }
    }

    fun checkFileExist(filePath: String): Boolean {
        return checkFileExist(filePath, "")
    }

    fun checkFileExist(filePath: String, fileMD5: String): Boolean {
        return checkFileExist(filePath, 0, fileMD5, true)
    }

    /**
     *
     * 检查文件是否存在，只有校验通过才算存在
     *
     * @param ignoreWhenMd5IsBad 当fileMD5 为空时，认为文件存在还是不存在
     *
     *  true 文件存在
     *  false 文件不存在
     */
    fun checkFileExist(filePath: String, fileLength: Long, fileMD5: String, ignoreWhenMd5IsBad: Boolean): Boolean {
        return if (TextUtils.isEmpty(filePath)) {
            false
        } else {
            val file = File(filePath)
            if (!file.exists() || !file.isFile) {
                false
            } else {
                var hasMD5 = !TextUtils.isEmpty(fileMD5)
                if (fileLength > 0) {
                    if (fileLength == file.length()) {
                        if (hasMD5) {
                            getFileMD5(filePath).equals(fileMD5, ignoreCase = true)
                        } else {
                            true
                        }
                    } else {
                        false
                    }
                } else {
                    if (hasMD5) {
                        getFileMD5(filePath).equals(fileMD5, ignoreCase = true)
                    } else {
                        ignoreWhenMd5IsBad
                    }
                }
            }
        }
    }

    fun getFileLength(sizeInBytes: Long): String {
        val nf: NumberFormat = DecimalFormat().apply {
            maximumFractionDigits = 2
        }

        return try {
            when {
                sizeInBytes < SPACE_KB -> {
                    nf.format(sizeInBytes) + " B"
                }
                sizeInBytes < SPACE_MB -> {
                    nf.format(sizeInBytes / SPACE_KB) + " KB"
                }
                sizeInBytes < SPACE_GB -> {
                    nf.format(sizeInBytes / SPACE_MB) + " MB"
                }
                sizeInBytes < SPACE_TB -> {
                    nf.format(sizeInBytes / SPACE_GB) + " GB"
                }
                else -> {
                    nf.format(sizeInBytes / SPACE_TB) + " TB"
                }
            }
        } catch (e: java.lang.Exception) {
            "$sizeInBytes B"
        }
    }

    /**
     * 仅能打开 [ZixieFileProvider.getZixieFilePath] 对应目录下的文件
     */
    fun openFile(context: Context, filePath: String, fileType: String): Boolean {
        return FileAction.openFile(context, filePath, fileType)
    }

    fun sendFile(context: Context, filePath: String, fileType: String): Boolean {
        return FileAction.sendFile(context, filePath, fileType)
    }

    fun deleteOldAsync(dir: File, duration: Long) {
        FileAction.deleteOldAsync(dir, duration)
    }

    fun deleteOld(dir: File, duration: Long) {
        FileAction.deleteOld(dir, duration)
    }

    fun deleteDirectory(dir: File): Boolean {
        return FileAction.deleteDirectory(dir)
    }

    fun deleteFile(filePath: String): Boolean {
        return FileAction.deleteFile(filePath)
    }

    fun copyFile(source: File, dest: File): Boolean {
        return FileAction.copyFile(source, dest)
    }

    fun copyFile(srcFile: File, dstFile: File, isMove: Boolean): Boolean {
        return FileAction.copyFile(srcFile, dstFile, isMove)
    }

    fun copyDirectory(src: File, dest: File) {
        FileAction.copyDirectory(src, dest, false)
    }

    fun copyDirectory(src: File, dest: File, isMove: Boolean) {
        FileAction.copyDirectory(src, dest, isMove)
    }

    fun copyAssetsFileToPath(context: Context?, fromFileName: String, targetPath: String): Boolean {
        return FileAction.copyAssetsFileToPath(context, fromFileName, targetPath)
    }

    fun copyAssetsFolderToFolder(context: Context?, fromAssetPath: String, targetFolder: String): Boolean {
        return FileAction.copyAssetsFolderToFolder(context, fromAssetPath, targetFolder)
    }

    fun isAssetsExists(context: Context?, filePath: String): Boolean {
        return FileAction.isAssetsExists(context, filePath)
    }

    fun getFileMD5(filePath: String): String {
        return MD5.getFileMD5(filePath)
    }

    fun getFileSHA256(filePath: String): String {
        return SHA256.getFileSHA256(filePath)
    }

    fun getExtensionName(filename: String?): String {
        return FileName.getExtensionName(filename)
    }

    fun getFileName(filePath: String?): String {
        return FileName.getFileName(filePath)
    }

    fun getFileNameWithoutEx(filename: String?): String {
        return FileName.getFileNameWithoutEx(filename)
    }

    fun isInvalidFilename(fileName: String): Boolean {
        return FileName.isInvalidFilename(fileName)
    }

    fun getFileContent(filePath: String?): String {
        return FileContent.getFileContent(filePath, "UTF-8")
    }

    fun getFileContent(filePath: String?, isGzip: Boolean): String {
        return FileContent.getFileContent(filePath, "UTF-8", isGzip)
    }

    fun getFileContent(filePath: String?, encoding: String): String {
        return FileContent.getFileContent(filePath, encoding, false)
    }

    fun getFileContent(filePath: InputStream?, encoding: String): String {
        return FileContent.getFileContent(filePath, encoding)
    }

    fun getAssetFileContent(context: Context, filePath: String): String {
        return FileContent.getFileContent(context.resources.assets.open(filePath), "UTF-8")
    }

    fun getFileContent(filePath: String?, encoding: String, isGzip: Boolean): String {
        return FileContent.getFileContent(filePath, encoding, isGzip)
    }

    fun getFileBytes(filePath: String?): ByteArray? {
        return FileContent.getFileBytes(filePath)
    }

    fun isBinaryFile(filePath: String): Boolean {
        return FileContent.isBinaryFile(filePath)
    }

    fun writeToFile(filePath: String, data: String, append: Boolean) {
        FileContent.writeToFile(filePath, data, append)
    }
}