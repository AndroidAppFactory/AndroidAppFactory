package com.bihe0832.android.lib.file

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.DateUtil
import com.bihe0832.android.lib.utils.encrypt.MD5
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.text.NumberFormat


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-10.
 */
object FileUtils {

    const val SPACE_KB = 1024.0
    const val SPACE_MB = 1024 * SPACE_KB
    const val SPACE_GB = 1024 * SPACE_MB
    const val SPACE_TB = 1024 * SPACE_GB
    const val APK_FILE_SUFFIX = ".apk"

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
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && sourceFile.isFile && sourceFile.exists()) {
                val fileProvider = try {
                    ZixieFileProvider.getZixieFileProvider(context, File(filePath))
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                if (fileProvider == null) {
                    ZLog.e("fileAction targetFile dont has zixie FileProvider")
                    targetFile = File(ZixieFileProvider.getZixieFilePath(context) + getFileName(filePath))
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


    fun copyFile(source: File, dest: File) {
        var inputChannel: FileChannel? = null
        var outputChannel: FileChannel? = null
        try {
            inputChannel = FileInputStream(source).getChannel()
            outputChannel = FileOutputStream(dest).getChannel()
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size())
        } catch (e: Exception) {
            e.printStackTrace()
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

    fun copyDirectory(src: File, dest: File) {
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
                    copyDirectory(srcFile, destFile)
                }
            } else {
                copyFile(src, dest)
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

    fun getFileContent(filename: String?): String {
        var res = ""
        filename?.let { it ->
            if (checkFileExist(it)) {
                var fis: FileInputStream? = null
                try {
                    fis = FileInputStream(File(it))
                    val buffer = ByteArray(fis.available())
                    fis.read(buffer)
                    res = String(buffer, Charset.defaultCharset())
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        fis?.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return res
    }
}