package com.bihe0832.android.lib.file

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.text.TextUtils
import com.bihe0832.android.lib.utils.encypt.MD5
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.text.DecimalFormat
import java.text.NumberFormat

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-10.
 * 使用FileUtils 提供的Provider时，可以选择自定路径或者使用库默认路径
 * 如果是选择自定义的，需要做如下操作，否则会造成无法安装等问题：
 *      添加以下String值定义：
 *          lib_bihe0832_file_folder：自定义文件目录
 *      在res/xml添加文件：file_paths.xml，内容为：
 *      <?xml version="1.0" encoding="utf-8"?>
 *          <paths>
 *              <external-files-path name="download" path="你自定义的文件目录"/>
 *          </paths>
 * 如果是选择默认的路径，则不需要上述定义；如果此时你需要获取库提供的provider 可以使用接口 {@link getZixieFileProvider}
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

    fun getZixieFileProvider(context: Context, file: File): Uri? {
        return FileProvider.getUriForFile(context, context.packageName + ".bihe0832", file)
    }

    fun getZixieFilePath(context: Context): String {
        return context.getExternalFilesDir(context.getString(R.string.lib_bihe0832_file_folder)).absolutePath
    }

    fun checkStoragePermissions(context: Context): Boolean {
        return PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    fun checkFileExist(filePath: String, fileMD5: String): Boolean {
        return if (TextUtils.isEmpty(filePath)) {
            false
        } else {
            val file = File(filePath)
            if (TextUtils.isEmpty(fileMD5)) {
                file.length() > 0 && file.exists() && file.isFile
            } else {
                getFileMD5(filePath).equals(fileMD5, ignoreCase = true)
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

    fun openFile(context: Context, filePath: String, fileType: String) {
        try { //设置intent的data和Type属性
            File(filePath).let { file ->
                val fileProvider = getZixieFileProvider(context, file)
                Intent(Intent.ACTION_VIEW).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    addCategory("android.intent.category.DEFAULT")
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        setDataAndType(Uri.fromFile(file), fileType)
                    } else {
                        setDataAndType(fileProvider, fileType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    }
                }.let {
                    context.startActivity(it)
                }
            }

        } catch (e: java.lang.Exception) { //当系统没有携带文件打开软件，提示
            e.printStackTrace()
        }
    }

    fun getFileMD5(filePath: String): String {
        return MD5.getFileMD5(filePath)
    }


    fun deleteDirectory(dir: File): Boolean {
        try {
            if (!dir.exists()) {
                return true
            }else{
                if (dir.isDirectory) {
                    val childrens: Array<String> = dir.list()
                    // 递归删除目录中的子目录下
                    for (child in childrens) {
                        val success: Boolean = deleteDirectory(File(dir, child))
                        if (!success) return false
                    }
                    return dir.delete()
                }else{
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
}