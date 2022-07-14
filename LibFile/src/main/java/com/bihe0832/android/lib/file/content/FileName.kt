package com.bihe0832.android.lib.file.content

import android.text.TextUtils
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.utils.encrypt.MD5
import java.io.*
import java.nio.charset.Charset
import java.util.*
import java.util.zip.GZIPInputStream


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-10.
 */
object FileName {

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
            val split = filePath.lastIndexOf(File.separator)
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
            val split = filename.lastIndexOf(File.separator)
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
            if (Arrays.binarySearch(FileUtils.ILLEGAL_FILENAME_CHARS, c) >= 0) {
                return true
            }
        }
        return false
    }

}