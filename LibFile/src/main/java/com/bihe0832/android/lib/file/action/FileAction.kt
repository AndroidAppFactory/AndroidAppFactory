package com.bihe0832.android.lib.file.action

import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.bihe0832.android.lib.utils.time.DateUtil
import java.io.*
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
                            File(ZixieFileProvider.getZixieFilePath(context) + FileUtils.getFileName(filePath))
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
            ZLog.e("FileAction", "  \n !!!========================================  \n \n \n !!! FileAction: The fileAction throw an Exception: ${e.javaClass.name} \n \n \n !!!========================================")
            e.printStackTrace()
            return false
        }
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

    private fun copyFile(source: File, dest: File): Boolean {
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
        FileUtils.checkAndCreateFolder(dstFile.parentFile.absolutePath)
        return if (isMove) {
            srcFile.renameTo(dstFile)
        } else {
            copyFile(srcFile, dstFile)
        }
    }

    fun copyDirectory(src: File, dest: File, isMove: Boolean): Boolean {
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

    private fun copyStream(input: InputStream, output: OutputStream) {
        val buff = ByteArray(1024)
        var read = 0
        do {
            read = input.read(buff)
            if (read > 0) {
                output.write(buff, 0, read)
            }
        } while (read > 0)
    }

    fun copyAssetsFileToPath(context: Context?, fromFileName: String, targetPath: String): Boolean {
        if (context == null) {
            ZLog.e("copyAssetsToSdcard context is null")
            return false
        }
        try {
            FileUtils.checkAndCreateFolder(File(targetPath).parentFile.absolutePath)
            FileUtils.deleteFile(targetPath)
            context.assets.open(fromFileName).use { input ->
                FileOutputStream(targetPath).use { output ->
                    copyStream(input, output)
                    input.close()
                    output.close()
                }
            }
            return true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            ZLog.e("copyAssets2Sdcard exception:$e")
            return false
        }

    }

    fun copyAssetsFolderToFolder(context: Context?, fromAssetPath: String, targetFolder: String): Boolean {
        if (context == null) {
            ZLog.e("copyAssetsFolder context is null")
            return false
        }
        try {
            var source = if (TextUtils.isEmpty(fromAssetPath)) {
                fromAssetPath
            } else {
                fromAssetPath + File.separator
            }

            var target = FileUtils.getFolderPathWithSeparator(targetFolder)
            var res = true
            context.assets.list(fromAssetPath)?.forEach { file: String ->
                var dataArray = context.assets.list(file) ?: emptyArray()
                res = if (dataArray.isNotEmpty()) {
                    res && copyAssetsFolderToFolder(context, "$source$file", "$target$file")
                } else {
                    if (isAssetsExists(context, "$source$file")) {
                        res && copyAssetsFileToPath(context, "$source$file", "$target$file")
                    } else {
                        res
                    }
                }
            }
            return res
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

}