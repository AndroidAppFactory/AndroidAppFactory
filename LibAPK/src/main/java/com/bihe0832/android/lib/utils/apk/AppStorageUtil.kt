package com.bihe0832.android.lib.utils.apk

import android.content.Context
import android.content.pm.PackageManager
import java.io.File

/**
 * @author zixie code@bihe0832.com Created on 2024/11/27.
 * Description: 获取应用占用存储信息
 */

object AppStorageUtil {

    fun getCurrentAppSize(context: Context): Long {
        var appSize: Long = 0
        try {
            appSize += getApplicationSize(context, context.packageName)
            appSize += getCurrentAppDataSize(context) // 数据大小
            appSize += getCurrentAppExternalDirSize(context) // 外部大小
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return appSize
    }

    fun getCurrentApplicationSize(context: Context): Long {
        return getApplicationSize(context, context.packageName)
    }

    fun getCurrentAppCacheSize(context: Context): Long {
        val cacheDir = context.cacheDir
        return getFolderSize(cacheDir)
    }

    fun getCurrentAppDataSize(context: Context): Long {
        val dataDir = File(context.applicationInfo.dataDir)
        return getFolderSize(dataDir)
    }

    fun getCurrentAppExternalDirSize(context: Context): Long {
        val cacheDir = context.getExternalFilesDir("")
        return getFolderSize(cacheDir)
    }

    fun getCurrentAppFolderSizeList(
        context: Context, minSize: Int, showFile: Boolean, needSort: Boolean
    ): Map<String, Long> {
        val data = HashMap<String, Long>()
        updateFolderSizeList(File(context.applicationInfo.dataDir), minSize, showFile, data)
        updateFolderSizeList(context.getExternalFilesDir(""), minSize, showFile, data)
        return if (needSort) {
            data.toList().sortedBy { (key, value) -> value }.reversed().toMap()
        } else {
            data.toList().sortedBy { (key, value) -> key }.reversed().toMap()
        }
    }

    fun getApplicationSize(context: Context, packageName: String): Long {
        val appSize: Long = 0
        try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            return File(appInfo.sourceDir).length()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return appSize
    }


    private fun getFolderSize(dir: File?): Long {
        var size: Long = 0
        if (dir != null && dir.isDirectory) {
            val childFolder = dir.listFiles()
            if (childFolder != null) {
                for (file in childFolder) {
                    size += if (file.isFile) {
                        file.length()
                    } else {
                        getFolderSize(file)
                    }
                }
            }
        }
        return size
    }

    private fun updateFolderSizeList(
        dir: File?, minSize: Int, showFile: Boolean, data: HashMap<String, Long>
    ): Long {
        var size: Long = 0
        if (dir != null && dir.isDirectory) {
            val childFolder = dir.listFiles()
            if (childFolder != null) {
                for (file in childFolder) {
                    if (file.isFile) {
                        if (showFile && file.length() >= minSize) {
                            data[file.absolutePath] = file.length()
                        }
                        size += file.length()
                    } else {
                        size += updateFolderSizeList(file, minSize, showFile, data)
                    }
                }
            }
            if (size >= minSize) {
                data[dir.toString()] = size
            }
        }
        return size
    }

    fun getFolderSizeList(
        dir: File,
        minSize: Int,
        showFile: Boolean,
        needSort: Boolean
    ): Map<String, Long> {
        val data = HashMap<String, Long>()
        updateFolderSizeList(dir, minSize, showFile, data)
        return if (needSort) {
            data.toList().sortedBy { (key, value) -> value }.reversed().toMap()
        } else {
            data.toList().sortedBy { (key, value) -> key }.reversed().toMap()
        }
    }
}