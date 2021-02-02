@file:JvmName("OBBFormats")

package com.bihe0832.android.lib.install.obb

import android.os.Environment
import com.bihe0832.android.lib.log.ZLog
import java.io.File

private const val OBB_FORMATS_DEBUG = true
private const val OBB_FORMATS_TAG = "OBBFormats"

private const val OBB_FILE_TYPE_MAIN = "main"
private const val OBB_FILE_TYPE_PATCH = "patch"

private const val OBB_FILENAME_SUFFIX = ".obb"

fun isObbFile(filename: String): Boolean {
    return filename.endsWith(OBB_FILENAME_SUFFIX, ignoreCase = true).also {
        if (!it && OBB_FORMATS_DEBUG) {
            ZLog.d("[$OBB_FORMATS_TAG] $filename is not endsWith $OBB_FILENAME_SUFFIX")
        }
    }
}


fun getObbDir(packageName: String): File {
    return File(Environment.getExternalStorageDirectory(), "Android/obb/$packageName/")
}


private fun isValidObbFileType(fileType: String) =
        fileType.equals(OBB_FILE_TYPE_MAIN, ignoreCase = true) ||
                fileType.equals(OBB_FILE_TYPE_PATCH, ignoreCase = true)
