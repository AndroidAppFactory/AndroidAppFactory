package com.bihe0832.android.lib.file.action

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.text.TextUtils
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.content.FileName
import com.bihe0832.android.lib.file.mimetype.FileMimeTypes
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.utils.time.DateUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.FileChannel

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-10.
 */
object FileAction {

    /**
     * 仅能打开 [ZixieFileProvider.getZixieFilePath] 对应目录下的文件
     */
    fun openFile(context: Context, filePath: String): Boolean {
        return fileAction(context, Intent.ACTION_VIEW, "", filePath)
    }

    fun openFile(
        context: Context,
        actionTitle: String,
        filePath: String,
        fileType: String
    ): Boolean {
        return fileAction(context, Intent.ACTION_VIEW, actionTitle, filePath, fileType)
    }

    fun sendFile(context: Context, filePath: String): Boolean {
        return fileAction(context, Intent.ACTION_SEND, "", filePath)
    }

    fun sendFile(
        context: Context,
        actionTitle: String,
        filePath: String,
        fileType: String
    ): Boolean {
        return fileAction(context, Intent.ACTION_SEND, actionTitle, filePath, fileType)
    }

    fun fileAction(
        context: Context,
        action: String,
        actionTitle: String,
        filePath: String
    ): Boolean {
        return fileAction(
            context,
            action,
            actionTitle,
            filePath,
            FileMimeTypes.getMimeType(filePath)
        )
    }

    fun fileAction(
        context: Context,
        action: String,
        actionTitle: String,
        filePath: String,
        fileType: String
    ): Boolean {
        try { // 设置intent的data和Type属性
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
                    targetFile = File(
                        ZixieFileProvider.getZixieFilePath(context) + FileName.getFileName(filePath)
                    )
                    copyFile(sourceFile, targetFile)
                }
            }
            ZLog.d("fileAction targetFile:${targetFile.absolutePath}")
            Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addCategory("android.intent.category.DEFAULT")
                ZixieFileProvider.setFileUriForIntent(context, this, targetFile, fileType)
            }.let {
                try {
                    context.startActivity(Intent.createChooser(it, actionTitle))
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    context.startActivity(it)
                }
            }
            return true
        } catch (e: java.lang.Exception) { // 当系统没有携带文件打开软件，提示
            ZLog.e(
                "FileAction",
                "  \n !!!========================================  \n \n \n !!! FileAction: The fileAction throw an Exception: ${e.javaClass.name} \n \n \n !!!========================================",
            )
            e.printStackTrace()
            return false
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
            val result = checkAndCreateFolder(path)
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

    fun deleteOldAsync(dir: File, duration: Long) {
        ThreadManager.getInstance().start {
            deleteOld(dir, duration)
        }
    }

    fun deleteOld(dir: File, duration: Long) {
        dir.listFiles()?.forEach { tempFile ->
            val lastModify = tempFile.lastModified()
            ZLog.w("File", "File $tempFile Date is ${DateUtil.getDateEN(lastModify)}")
            if (tempFile.exists() && System.currentTimeMillis() - lastModify > duration) {
                val result = if (tempFile.isDirectory) {
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

    fun updateModifiedTime(filePath: String, newTimeMillis: Long): Boolean {
        val file = File(filePath)
        return if (file.exists()) {
            file.setLastModified(newTimeMillis)
        } else {
            false
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

    fun copyFile(source: FileInputStream, dest: FileOutputStream): Boolean {
        var result = false
        var inputChannel: FileChannel? = null
        var outputChannel: FileChannel? = null
        try {
            inputChannel = source.getChannel()
            outputChannel = dest.getChannel()
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size())
            result = true
        } catch (e: Exception) {
            e.printStackTrace()
            result = false
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
        return result
    }

    fun copyFile(input: InputStream?, output: OutputStream?): Boolean {
        if (input == null || output == null) {
            return false
        }
        try {
            val buff = ByteArray(1024 * 8)
            var read = 0
            do {
                read = input.read(buff)
                if (read > 0) {
                    output.write(buff, 0, read)
                }
            } while (read > 0)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun copyFile(input: InputStream?, dest: File?): Boolean {
        if (input == null || dest == null) {
            return false
        }
        var result = false
        var fileOutputStream: FileOutputStream? = null
        try {
            FileUtils.checkAndCreateFolder(dest.parentFile.absolutePath)
            fileOutputStream = FileOutputStream(dest)
            result = copyFile(input, fileOutputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fileOutputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return result
    }

    fun copyFileToFolder(context: Context, source: Uri, tartFolder: String): String {
        val resolver = context.contentResolver
        val cursor = resolver.query(source, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            try {
                val fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                val file = File(tartFolder, fileName)
                FileUtils.copyFile(context, source, file).let {
                    if (it) {
                        return file.absolutePath
                    } else {
                        return ""
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                return ""
            }
        }
        return ""
    }

    fun copyFile(context: Context, source: Uri, dest: File): Boolean {
        if (source.path.equals(dest.absolutePath, ignoreCase = true)) {
            return true
        }
        var result = false
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(source)
            result = copyFile(inputStream, dest)
        } catch (e: Exception) {
            e.printStackTrace()
            result = false
        } finally {
            try {
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return result
    }

    fun copyFile(source: File, dest: File): Boolean {
        if (source.absolutePath.equals(dest.absolutePath, ignoreCase = true)) {
            return true
        }
        var result = false
        var fileInputStream: FileInputStream? = null
        var fileOutputStream: FileOutputStream? = null
        try {
            FileUtils.checkAndCreateFolder(dest.parentFile.absolutePath)
            fileInputStream = FileInputStream(source)
            fileOutputStream = FileOutputStream(dest)
            result = copyFile(fileInputStream, fileOutputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            result = false
        } finally {
            try {
                fileInputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                fileInputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return result
    }

    fun copyFile(srcFile: File, dstFile: File, isMove: Boolean): Boolean {
        if (srcFile.absolutePath.equals(dstFile.absolutePath, ignoreCase = true)) {
            return true
        }
        checkAndCreateFolder(dstFile.parentFile.absolutePath)
        return if (isMove) {
            srcFile.renameTo(dstFile)
        } else {
            copyFile(srcFile, dstFile)
        }
    }

    fun copyAssetsFileToPath(context: Context?, fromFileName: String, targetPath: String): Boolean {
        if (context == null) {
            ZLog.e("copyAssetsToSdcard context is null")
            return false
        }
        var inputStream: InputStream? = null
        var result = false
        try {
            deleteFile(targetPath)
            inputStream = context.assets.open(fromFileName)
            result = copyFile(inputStream, File(targetPath))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            ZLog.e("copyAssets2Sdcard exception:$e")
            result = false
        } finally {
            try {
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return result
    }

    fun copyAssetsFolderToFolder(
        context: Context?,
        fromAssetPath: String,
        targetFolder: String
    ): Boolean {
        if (context == null) {
            ZLog.e("copyAssetsFolder context is null")
            return false
        }
        try {
            val source = if (TextUtils.isEmpty(fromAssetPath)) {
                fromAssetPath
            } else {
                if (fromAssetPath.endsWith(File.separator)) {
                    fromAssetPath
                } else {
                    fromAssetPath + File.separator
                }
            }

            val target = FileAction.getFolderPathWithSeparator(targetFolder)

            context.assets.list(source)?.forEach { file: String ->
                val dataArray = context.assets.list(source + file) ?: emptyArray()
                val res = if (dataArray.isNotEmpty()) {
                    copyAssetsFolderToFolder(context, "$source$file", "$target$file")
                } else {
                    if (FileAction.isAssetsExists(context, "$source$file")) {
                        FileAction.copyAssetsFileToPath(context, "$source$file", "$target$file")
                    } else {
                        false
                    }
                }
                if (!res) {
                    return false
                }
            }
            return true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            ZLog.e("copyAssetsFolder exception:$e")
            return false
        }
    }

    fun isAssetsExists(context: Context?, filePath: String): Boolean {
        if (context == null) {
            ZLog.e("isAssetExists context is null")
            return false
        }
        var inputStream: InputStream? = null
        try {
            inputStream = context.assets.open(filePath)
            if (null != inputStream) {
                return true
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return false
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return false
    }

    fun copyDirectory(src: File, dest: File, isMove: Boolean): Boolean {
        try {
            if (src.absolutePath.equals(dest.absolutePath, ignoreCase = true)) {
                return true
            }
            if (src.isDirectory) {
                if (!dest.exists()) {
                    dest.mkdir()
                }
                val files = src.list()
                for (file in files) {
                    val srcFile = File(src, file)
                    val destFile = File(dest, file)
                    // 递归复制
                    copyDirectory(srcFile, destFile, isMove).let {
                        if (!it) {
                            return false
                        }
                    }
                }
                return true
            } else {
                return copyFile(src, dest, isMove)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
