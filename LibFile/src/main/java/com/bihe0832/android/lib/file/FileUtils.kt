package com.bihe0832.android.lib.file

import android.text.TextUtils
import com.bihe0832.android.lib.utils.encypt.MD5
import java.io.File

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020-01-10.
 * Description: Description
 *
 */

object FileUtils {

    fun checkFileExist(filePath: String): Boolean {
        return if (TextUtils.isEmpty(filePath)) {
            false
        } else {
            val file = File(filePath)
            file != null && file.length() > 0 && file.exists() && file.isFile
        }
    }

    fun checkFileExist(filePath: String, fileMD5: String): Boolean {
        return if (TextUtils.isEmpty(filePath)) {
            false
        } else {
            val file = File(filePath)
            if (TextUtils.isEmpty(fileMD5)) {
                file != null && file.length() > 0 && file.exists() && file.isFile
            } else {
                MD5.getMd5(filePath).equals(fileMD5, ignoreCase = true)
            }
        }
    }

    fun getFileMD5(filePath: String) : String{
        return MD5.getFileMD5(filePath)
    }

    fun deleteFile(filePath: String) : Boolean{
        try {
            return File(filePath).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}