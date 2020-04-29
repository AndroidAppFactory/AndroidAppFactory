package com.bihe0832.android.lib.file

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import android.text.TextUtils
import com.bihe0832.android.lib.utils.encypt.MD5
import java.io.File

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020-01-10.
 * 使用FileUtils 提供的Provider时，可以选择自定路径或者使用库默认路径
 * 如果是选择自定义的，需要做如下操作，否则会造成无法安装等问题：
 *      添加以下String值定义：
 *          lib_bihe0832_file_folder：自定义文件目录
 *      在res/xml添加文件：zixie_file_paths.xml，内容为：
 *      <?xml version="1.0" encoding="utf-8"?>
 *          <paths>
 *              <external-files-path name="zixie_download" path="你自定义的文件目录"/>
 *          </paths>
 * 如果是选择默认的路径，则不需要上述定义；如果此时你需要获取库提供的provider 可以使用接口 {@link getZixieFileProvider}
 */
object FileUtils {

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
                        setDataAndType(fileProvider, "text/plain")
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

    fun deleteFile(filePath: String): Boolean {
        try {
            return File(filePath).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}