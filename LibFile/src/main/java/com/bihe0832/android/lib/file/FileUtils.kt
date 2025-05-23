package com.bihe0832.android.lib.file

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.StatFs
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.bihe0832.android.lib.file.action.FileAction
import com.bihe0832.android.lib.file.content.FileContent
import com.bihe0832.android.lib.file.content.FileContentPattern
import com.bihe0832.android.lib.file.content.FileName
import com.bihe0832.android.lib.file.content.RandomAccessFileUtils
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.utils.encrypt.HexUtils
import com.bihe0832.android.lib.utils.encrypt.messagedigest.MD5
import com.bihe0832.android.lib.utils.encrypt.messagedigest.MessageDigestUtils
import com.bihe0832.android.lib.utils.encrypt.messagedigest.SHA256
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

    val ILLEGAL_FILENAME_CHARS = charArrayOf(
        34.toChar(),
        60.toChar(),
        62.toChar(),
        124.toChar(),
        0.toChar(),
        1.toChar(),
        2.toChar(),
        3.toChar(),
        4.toChar(),
        5.toChar(),
        6.toChar(),
        7.toChar(),
        8.toChar(),
        9.toChar(),
        10.toChar(),
        11.toChar(),
        12.toChar(),
        13.toChar(),
        14.toChar(),
        15.toChar(),
        16.toChar(),
        17.toChar(),
        18.toChar(),
        19.toChar(),
        20.toChar(),
        21.toChar(),
        22.toChar(),
        23.toChar(),
        24.toChar(),
        25.toChar(),
        26.toChar(),
        27.toChar(),
        28.toChar(),
        29.toChar(),
        30.toChar(),
        31.toChar(),
        58.toChar(),
        42.toChar(),
        63.toChar(),
        92.toChar(),
        47.toChar(),
    )

    const val SPACE_KB = 1024.0
    const val SPACE_MB = 1024 * SPACE_KB
    const val SPACE_GB = 1024 * SPACE_MB
    const val SPACE_TB = 1024 * SPACE_GB

    fun checkStoragePermissions(context: Context?): Boolean {
        context?.let {
            return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        }
        return false
    }

    fun getDirectoryTotalSpace(filePath: String): Long {
        return try {
            val mStatFs = StatFs(filePath)
            mStatFs.totalBytes
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            0L
        }
    }

    fun getDirectoryAvailableSpace(filePath: String): Long {
        return try {
            val mStatFs = StatFs(filePath)
            mStatFs.availableBytes
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            0L
        }
    }

    fun checkAndCreateFolder(path: String): Boolean {
        return FileAction.checkAndCreateFolder(path)
    }

    fun getFolderPathWithSeparator(path: String): String {
        return FileAction.getFolderPathWithSeparator(path)
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
     * @param ignoreDigestCheck 当fileMD5 为空时，认为文件存在还是不存在
     *
     *  true 文件存在
     *  false 文件不存在
     */
    fun checkFileExist(
        filePath: String,
        fileLength: Long,
        fileMD5: String,
        ignoreDigestCheck: Boolean,
    ): Boolean {
        return checkFileExist(filePath, fileLength, fileMD5, "", ignoreDigestCheck)
    }

    fun checkFileExist(
        filePath: String,
        fileLength: Long,
        fileMD5: String,
        fileSHA256: String,
        ignoreDigestCheck: Boolean,
    ): Boolean {
        return if (TextUtils.isEmpty(fileMD5)) {
            checkFileExistByMessageDigest(
                filePath,
                fileLength,
                fileSHA256,
                SHA256.MESSAGE_DIGEST_TYPE_SHA256,
                ignoreDigestCheck,
            )
        } else {
            checkFileExistByMessageDigest(
                filePath,
                fileLength,
                fileMD5,
                MD5.MESSAGE_DIGEST_TYPE_MD5,
                ignoreDigestCheck,
            )
        }
    }

    fun checkFileExistByMessageDigest(
        paraFilePath: String,
        paraFileLength: Long,
        digestValue: String,
        digestType: String,
        ignoreDigestCheck: Boolean,
    ): Boolean {
        return if (TextUtils.isEmpty(paraFilePath)) {
            false
        } else {
            val file = File(paraFilePath)
            if (!file.exists() || !file.isFile) {
                false
            } else {
                var hasMD5 = !TextUtils.isEmpty(digestValue)
                if (paraFileLength > 0) {
                    if (paraFileLength == file.length()) {
                        if (hasMD5) {
                            MessageDigestUtils.getFileDigestData(paraFilePath, digestType)
                                .equals(digestValue, ignoreCase = true)
                        } else {
                            ignoreDigestCheck
                        }
                    } else {
                        false
                    }
                } else {
                    if (hasMD5) {
                        MessageDigestUtils.getFileDigestData(paraFilePath, digestType)
                            .equals(digestValue, ignoreCase = true)
                    } else {
                        ignoreDigestCheck
                    }
                }
            }
        }
    }

    fun getFileLength(sizeInBytes: Long): String {
        return getFileLength(sizeInBytes, 2)
    }

    fun getFileLength(sizeInBytes: Long, maximumFractionDigitsValues: Int): String {
        val nf: NumberFormat = DecimalFormat("#.#").apply {
            maximumFractionDigits = maximumFractionDigitsValues
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
    fun openFile(
        context: Context,
        actionTitle: String,
        filePath: String,
        fileType: String
    ): Boolean {
        return FileAction.openFile(context, actionTitle, filePath, fileType)
    }

    fun openFile(context: Context, filePath: String): Boolean {
        return FileAction.openFile(context, filePath)
    }

    fun sendFile(context: Context, filePath: String): Boolean {
        return FileAction.sendFile(context, filePath)
    }

    fun sendFile(
        context: Context,
        actionTitle: String,
        filePath: String,
        fileType: String
    ): Boolean {
        return FileAction.sendFile(context, actionTitle, filePath, fileType)
    }

    fun createFile(filePath: String, fileSize: Long): Boolean {
        return RandomAccessFileUtils.createFile(filePath, fileSize)
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

    fun updateModifiedTime(filePath: String): Boolean {
        return FileAction.updateModifiedTime(filePath, System.currentTimeMillis())
    }

    fun copyFile(input: InputStream?, dest: File?): Boolean {
        return FileAction.copyFile(input, dest)
    }

    fun copyFile(context: Context, source: Uri, dest: File): Boolean {
        return FileAction.copyFile(context, source, dest)
    }

    fun copyFile(source: File, dest: File): Boolean {
        return FileAction.copyFile(source, dest, false)
    }

    fun copyFile(srcFile: File, dstFile: File, isMove: Boolean): Boolean {
        return FileAction.copyFile(srcFile, dstFile, isMove)
    }

    fun copyDirectory(src: File, dest: File): Boolean {
        return FileAction.copyDirectory(src, dest, false)
    }

    fun copyDirectory(src: File, dest: File, isMove: Boolean): Boolean {
        return FileAction.copyDirectory(src, dest, isMove)
    }

    fun copyAssetsFileToPath(context: Context?, fromFileName: String, targetPath: String): Boolean {
        return FileAction.copyAssetsFileToPath(context, fromFileName, targetPath)
    }

    fun copyAssetsFolderToFolder(
        context: Context?,
        fromAssetPath: String,
        targetFolder: String,
    ): Boolean {
        return FileAction.copyAssetsFolderToFolder(context, fromAssetPath, targetFolder)
    }

    fun isAssetsExists(context: Context?, filePath: String): Boolean {
        return FileAction.isAssetsExists(context, filePath)
    }

    fun getFileDigestData(filePath: String, digestType: String): String {
        return MessageDigestUtils.getFileDigestData(filePath, digestType)
    }

    fun getFilePartDigestData(
        fileName: String,
        digestType: String,
        start: Long,
        end: Long
    ): String {
        return MessageDigestUtils.getFilePartDigestData(fileName, digestType, start, end)
    }

    fun getFileMD5(filePath: String): String {
        return MD5.getFileMD5(filePath)
    }

    fun getFilePartMD5(fileName: String, start: Long, end: Long): String {
        return MD5.getFilePartMD5(fileName, start, end)
    }

    fun getFileSHA256(filePath: String): String {
        return SHA256.getFileSHA256(filePath)
    }

    fun getFilePartSHA256(fileName: String, start: Long, end: Long): String {
        return SHA256.getFilePartSHA256(fileName, start, end)
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

    fun getFileContent(contetxt: Context, uri: Uri): String {
        return FileContent.getFileContent(contetxt, uri, "UTF-8")
    }

    fun getFileContent(contetxt: Context, uri: Uri, encoding: String, isGzip: Boolean): String {
        return FileContent.getFileContent(contetxt, uri, encoding, isGzip)
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

    fun readDataFromFile(filePath: String?, offset: Long, length: Int): ByteArray {
        return RandomAccessFileUtils.readDataFromFile(filePath, offset, length)
    }

    fun getFileBytes(filePath: String?): ByteArray? {
        return FileContent.getFileBytes(filePath)
    }

    fun isBinaryFile(filePath: String): Boolean {
        return FileContent.isBinaryFile(filePath)
    }

    fun writeDataToFile(
        filePath: String,
        offset: Long,
        bytes: ByteArray,
        replace: Boolean
    ): Boolean {
        return RandomAccessFileUtils.writeDataToFile(filePath, offset, bytes, replace)
    }

    fun writeToFile(filePath: String, data: String, append: Boolean) {
        FileContent.writeToFile(filePath, data, "UTF-8", append)
    }

    fun writeHexToFile(filePath: String, hexData: String, append: Boolean) {
        writeToFile(filePath, HexUtils.hexStr2Bytes(hexData), append)
    }

    fun writeToFile(filePath: String, data: String, encoding: String, append: Boolean) {
        FileContent.writeToFile(filePath, data, encoding, append)
    }

    fun writeToFile(filePath: String, data: ByteArray, append: Boolean) {
        FileContent.writeToFile(filePath, data, append)
    }

    fun mergeFile(firstFile: String, secondFile: String, resultFile: String) {
        FileContent.mergeFile(firstFile, secondFile, resultFile)
    }

    fun findPattern(filePath: String, dataToFind: String): Int {
        return FileContentPattern.findPattern(filePath, dataToFind)
    }

    fun findPattern(fileContent: ByteArray, dataToFind: ByteArray): Int {
        return FileContentPattern.findPattern(fileContent, dataToFind)
    }
}
