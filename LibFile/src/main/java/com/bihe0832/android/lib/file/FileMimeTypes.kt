/*
 * *
 *  * Created by zixie < code@bihe0832.com > on 2022/5/26 下午5:08
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/5/26 下午5:07
 *
 */
package com.bihe0832.android.lib.file

import android.text.TextUtils
import android.webkit.MimeTypeMap
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object FileMimeTypes {

    private val mMimeTypes = ConcurrentHashMap<String, String>()

    private fun putTypeByExtension(extension: String, type: String) {
        mMimeTypes[extension.toLowerCase()] = type
    }

    private fun getTypeByExtension(extension: String): String {
        return if (mMimeTypes.contains(extension.toLowerCase())) {
            mMimeTypes[extension.toLowerCase()] ?: ""
        } else {
            ""
        }
    }

    fun getMimeType(filename: String?): String {
        val extension = FileUtils.getExtensionName(filename)
        if (TextUtils.isEmpty(extension)) {
            return "*/*"
        }
        getTypeByExtension(extension).let {
            if (TextUtils.isEmpty(it)) {
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1)).let { webkitMimeType ->
                    webkitMimeType?.let {
                        putTypeByExtension(extension, webkitMimeType)
                    }
                    return webkitMimeType ?: "*/*"
                }
            } else {
                return it
            }
        }
    }


    fun isTextFile(file: String): Boolean {
        return getMimeType(file).startsWith("text")
    }

    fun isImageFile(file: String): Boolean {
        val ext = FileUtils.getExtensionName(file)
        return if (getMimeType(file).startsWith("image/")) {
            true
        } else {
            ext.equals("png", ignoreCase = true) || ext.equals("jpg", ignoreCase = true)
                    || ext.equals("jpeg", ignoreCase = true) || ext.equals("gif", ignoreCase = true)
                    || ext.equals("tiff", ignoreCase = true) || ext.equals("tif", ignoreCase = true)
        }
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
        if (getMimeType(path).startsWith("video/")) {
            return true
        }
        val ext = FileUtils.getExtensionName(path)
        return (ext.equals("mp4", ignoreCase = true) || ext.equals("3gp", ignoreCase = true)
                || ext.equals("avi", ignoreCase = true) || ext.equals("webm", ignoreCase = true)
                || ext.equals("m4v", ignoreCase = true))
    }


    fun isAudioFile(file: File): Boolean {
        return !file.isDirectory && isAudioFile(file.name)
    }


    fun isAudioFile(path: String): Boolean {
        if (getMimeType(path).startsWith("audio/")) {
            return true
        }
        val ext = FileUtils.getExtensionName(path)
        return (ext.equals("mp3", ignoreCase = true) || ext.equals("wma", ignoreCase = true) || ext.equals("flac", ignoreCase = true)
                || ext.equals("wav", ignoreCase = true) || ext.equals("aac", ignoreCase = true)
                || ext.equals("ogg", ignoreCase = true) || ext.equals("m4a", ignoreCase = true))
    }
}