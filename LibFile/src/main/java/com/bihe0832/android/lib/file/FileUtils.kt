package com.bihe0832.android.lib.file

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.StatFs
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.encrypt.MD5
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.utils.time.DateUtil
import java.io.*
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import java.util.zip.GZIPInputStream


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

    fun checkFileExist(filePath: String): Boolean {
        return if (TextUtils.isEmpty(filePath)) {
            false
        } else {
            val file = File(filePath)
            file.length() > 0 && file.exists() && file.isFile
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

    fun checkStoragePermissions(context: Context): Boolean {
        return PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
    }

    fun checkFileExist(filePath: String, fileMD5: String): Boolean {
        return checkFileExist(filePath, 0, fileMD5)
    }

    fun checkFileExist(filePath: String, fileLength: Long, fileMD5: String): Boolean {
        return if (TextUtils.isEmpty(filePath)) {
            false
        } else {
            val file = File(filePath)
            var hasMD5 = !TextUtils.isEmpty(fileMD5)
            if (!file.exists() || !file.isFile) {
                false
            } else {
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
                        false
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

    /**
     * 仅能打开 [ZixieFileProvider.getZixieFilePath] 对应目录下的文件
     */
    fun openFile(context: Context, filePath: String, fileType: String): Boolean {
        return fileAction(context, Intent.ACTION_VIEW, filePath, fileType)
    }

    fun sendFile(context: Context, filePath: String, fileType: String): Boolean {
        return fileAction(context, Intent.ACTION_SEND, filePath, fileType)
    }

    fun fileAction(context: Context, action: String, filePath: String): Boolean {
        return fileAction(context, action, filePath, "*/*")
    }

    fun fileAction(context: Context, action: String, filePath: String, fileType: String): Boolean {
        try { //设置intent的data和Type属性
            ZLog.d("fileAction sourceFile:$filePath")
            var sourceFile = File(filePath)
            var targetFile = sourceFile
            if (BuildUtils.SDK_INT >= Build.VERSION_CODES.N && sourceFile.isFile && sourceFile.exists()) {
                val fileProvider = try {
                    ZixieFileProvider.getZixieFileProvider(context, File(filePath))
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                if (fileProvider == null) {
                    ZLog.e("fileAction targetFile dont has zixie FileProvider")
                    targetFile =
                            File(ZixieFileProvider.getZixieFilePath(context) + getFileName(filePath))
                    copyFile(sourceFile, targetFile)
                }
            }
            ZLog.d("fileAction targetFile:${targetFile.absolutePath}")
            Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addCategory("android.intent.category.DEFAULT")
                ZixieFileProvider.setFileUriForIntent(context, this, targetFile, fileType)
            }.let {
                context.startActivity(it)
            }
            return true

        } catch (e: java.lang.Exception) { //当系统没有携带文件打开软件，提示
            e.printStackTrace()
            return false
        }
    }

    fun getFileMD5(filePath: String): String {
        return MD5.getFileMD5(filePath)
    }

    fun deleteOldAsync(dir: File, duration: Long) {
        ThreadManager.getInstance().start {
            deleteOld(dir, duration)
        }
    }

    fun deleteOld(dir: File, duration: Long) {
        dir.listFiles().forEach { tempFile ->
            var lastModify = tempFile.lastModified()
            ZLog.w("File", "File $tempFile Date is ${DateUtil.getDateEN(lastModify)}")
            if (tempFile.exists() && System.currentTimeMillis() - lastModify > duration) {
                var result = if (tempFile.isDirectory) {
                    deleteDirectory(tempFile)
                } else {
                    deleteFile(tempFile.absolutePath)
                }
                ZLog.w("File", "File tempFile has delete: $result")
            }
        }
    }

    fun deleteDirectory(dir: File): Boolean {
        try {
            if (!dir.exists()) {
                return true
            } else {
                if (dir.isDirectory) {
                    val childrens: Array<String> = dir.list()
                    // 递归删除目录中的子目录下
                    for (child in childrens) {
                        val success: Boolean = deleteDirectory(File(dir, child))
                        if (!success) return false
                    }
                    return dir.delete()
                } else {
                    return deleteFile(dir.absolutePath)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun deleteFile(filePath: String): Boolean {
        try {
            return File(filePath).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }


    fun copyFile(source: File, dest: File): Boolean {
        var inputChannel: FileChannel? = null
        var outputChannel: FileChannel? = null
        try {
            inputChannel = FileInputStream(source).getChannel()
            outputChannel = FileOutputStream(dest).getChannel()
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size())
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            try {
                inputChannel?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                outputChannel?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun copyFile(srcFile: File, dstFile: File, isMove: Boolean): Boolean {
        return if (isMove) {
            srcFile.renameTo(dstFile)
        } else {
            copyFile(srcFile, dstFile)
        }
    }

    fun copyDirectory(src: File, dest: File) {
        copyDirectory(src, dest, false)
    }

    fun copyDirectory(src: File, dest: File, isMove: Boolean) {
        try {
            if (src.isDirectory) {
                if (!dest.exists()) {
                    dest.mkdir()
                }
                val files = src.list()
                for (file in files) {
                    val srcFile = File(src, file)
                    val destFile = File(dest, file)
                    // 递归复制
                    copyDirectory(srcFile, destFile, isMove)
                }
            } else {
                copyFile(src, dest, isMove)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun getExtensionName(filename: String?): String {
        filename?.let {
            val dot = filename.lastIndexOf('.')
            if (dot > -1 && dot < filename.length - 1) {
                return filename.substring(dot + 1)
            }
        }
        return ""
    }

    fun getFileName(filePath: String?): String {
        filePath?.let {
            val split = filePath.lastIndexOf('/')
            return if (split > -1) {
                filePath.substring(split + 1)
            } else {
                filePath
            }
        }
        return ""
    }

    fun getFileNameWithoutEx(filename: String?): String {
        filename?.let {
            val dot = filename.lastIndexOf('.')
            val split = filename.lastIndexOf('/')
            if (split < dot) {
                if (dot > -1 && dot < filename.length) {
                    return if (split > -1) {
                        filename.substring(split + 1, dot)
                    } else {
                        filename.substring(0, dot)
                    }
                }
            }
        }
        return ""
    }

    fun isInvalidFilename(fileName: String): Boolean {
        if (TextUtils.isEmpty(fileName)) {
            return false
        }
        val size = fileName.length
        var c: Char
        for (i in 0 until size) {
            c = fileName[i]
            if (Arrays.binarySearch(ILLEGAL_FILENAME_CHARS, c) >= 0) {
                return true
            }
        }
        return false
    }

    fun getFileContent(filePath: String?): String {
        return getFileContent(filePath, "UTF-8")
    }

    fun getFileContent(filePath: String?, isGzip: Boolean): String {
        return getFileContent(filePath, "UTF-8", isGzip)
    }

    fun getFileContent(filePath: String?, encoding: String): String {
        return getFileContent(filePath, encoding, false)
    }


    fun writeToFile(filePath: String, data: String, append: Boolean) {
        var fileOutputStream: FileOutputStream? = null
        try {
            val file = File(filePath)
            if (!checkFileExist(filePath)) {
                file.createNewFile()
            }

            //建立数据的输出通道
            fileOutputStream = FileOutputStream(file, append)
            fileOutputStream.write(data.toByteArray(Charset.forName("ISO-8859-1")))
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                fileOutputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun getFileContent(filePath: String?, encoding: String, isGzip: Boolean): String {
        val sb = StringBuffer()
        filePath?.let { it ->
            if (checkFileExist(it)) {
                var fis: InputStream? = null
                var br: BufferedReader? = null
                try {
                    fis = if (isGzip) {
                        GZIPInputStream(FileInputStream(File(it)))
                    } else {
                        FileInputStream(File(it))
                    }

                    br = BufferedReader(InputStreamReader(fis, encoding))
                    var line: String?
                    while (br.readLine().also { line = it } != null) {
                        sb.append(line + System.lineSeparator())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        fis?.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        br?.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return sb.toString()
    }

    fun getFileBytes(filePath: String?): ByteArray? {

        filePath?.let { it ->
            if (checkFileExist(it)) {
                var buf: BufferedInputStream? = null
                try {
                    val file = File(filePath)
                    val size: Int = file.length().toInt()
                    val bytes = ByteArray(size)
                    buf = BufferedInputStream(FileInputStream(file))
                    buf.read(bytes, 0, bytes.size)
                    return bytes
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        buf?.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return null

    }

    fun getRealFilePath(context: Context, uri: Uri?): String {
        if (null == uri) {
            return ""
        }
        val scheme: String? = uri.getScheme()
        var data: String? = null

        try {
            if (scheme == null) {
                data = uri.getPath()
            } else if (ContentResolver.SCHEME_FILE == scheme) {
                data = uri.getPath()
            } else if (ContentResolver.SCHEME_CONTENT == scheme) {
                val cursor: Cursor? = context.getContentResolver()
                        .query(uri, arrayOf(MediaStore.Images.ImageColumns.DATA), null, null, null)
                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        val index: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                        if (index > -1) {
                            data = cursor.getString(index)
                        }
                    }
                    cursor.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return data ?: ""
    }

    fun isBinaryFile(filePath: String): Boolean {
        getFileBytes(filePath)?.let {
            for (b in it) {
                if (b < 0x09) {
                    return true
                }
            }
        }
        return false
    }

}