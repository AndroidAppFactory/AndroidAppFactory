/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/16 下午3:08
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/5/26 下午5:42
 *
 */
package com.bihe0832.android.lib.file.mimetype

import android.text.TextUtils
import android.webkit.MimeTypeMap
import com.bihe0832.android.lib.file.FileUtils
import java.io.File
import java.util.concurrent.ConcurrentHashMap

const val FILE_TYPE_ALL = "*/*"
const val FILE_TYPE_IMAGE = "image/*"
const val FILE_TYPE_VIDEO = "video/*"
const val FILE_TYPE_AUDIO = "audio/*"
const val FILE_TYPE_TEXT = "text/*"

object FileMimeTypes {

    private val mMimeTypes = ConcurrentHashMap<String, String>()

    private fun putTypeByExtension(extension: String, type: String) {
        mMimeTypes[extension.toLowerCase()] = type
    }

    fun getTypeByExtension(extension: String): String {
        var type = ""
        if (mMimeTypes.contains(extension.toLowerCase())) {
            type = mMimeTypes[extension.toLowerCase()] ?: ""
        }
        if (TextUtils.isEmpty(type)) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension).let { webkitMimeType ->
                webkitMimeType?.let {
                    putTypeByExtension(extension, webkitMimeType)
                }
                type = webkitMimeType ?: FILE_TYPE_ALL
            }
        }
        return type
    }

    fun getMimeType(filename: String?): String {
        val extension = FileUtils.getExtensionName(filename)
        if (TextUtils.isEmpty(extension)) {
            return FILE_TYPE_ALL
        }
        return getTypeByExtension(extension)
    }

    fun isTextFile(file: String): Boolean {
        return getMimeType(file).startsWith("text")
    }

    fun isImageFile(filename: String): Boolean {
        return isImageFileByExtension(FileUtils.getExtensionName(filename))
    }

    fun isImageFileByExtension(ext: String): Boolean {
        return isImageFileByMimeType(getTypeByExtension(ext))
    }

    fun isImageFileByMimeType(mimeType: String): Boolean {
        return mimeType.startsWith("image/")
    }

    fun isApkFile(filename: String): Boolean {
        return filename.endsWith(".apk")
    }

    fun isArchive(file: String): Boolean {
        return file.endsWith(".zip", ignoreCase = true)
    }

    fun isVideoFile(file: File): Boolean {
        return !file.isDirectory && isVideoFile(file.name)
    }

    fun isVideoFile(path: String): Boolean {
        if (isVideoFileByMimeType(getMimeType(path))) {
            return true
        }
        return isVideoFileByExtension(FileUtils.getExtensionName(path))
    }

    fun isVideoFileByMimeType(mimeType: String): Boolean {
        return mimeType.startsWith("video/")
    }

    fun isVideoFileByExtension(ext: String): Boolean {
        return ext.equals("mp4", ignoreCase = true) || ext.equals("3gp", ignoreCase = true) ||
            ext.equals("avi", ignoreCase = true) || ext.equals("webm", ignoreCase = true) ||
            ext.equals("m4v", ignoreCase = true)
    }

    fun isAudioFile(file: File): Boolean {
        return !file.isDirectory && isAudioFile(file.name)
    }

    fun isAudioFile(path: String): Boolean {
        if (isAudioFileByMimeType(getMimeType(path))) {
            return true
        }
        return isAudioFileByExtension(FileUtils.getExtensionName(path))
    }

    fun isAudioFileByMimeType(mimeType: String): Boolean {
        return mimeType.startsWith("audio/")
    }

    fun isAudioFileByExtension(ext: String): Boolean {
        return ext.equals("mp3", ignoreCase = true) || ext.equals("wma", ignoreCase = true) ||
            ext.equals("flac", ignoreCase = true) || ext.equals("wav", ignoreCase = true) ||
            ext.equals("aac", ignoreCase = true) || ext.equals("ogg", ignoreCase = true) ||
            ext.equals("m4a", ignoreCase = true)
    }
}
