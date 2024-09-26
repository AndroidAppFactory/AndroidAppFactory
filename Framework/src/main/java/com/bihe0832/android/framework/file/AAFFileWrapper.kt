package com.bihe0832.android.framework.file

import android.os.Environment
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.R
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import java.io.File

/**
 *
 *   所有cache目录的内容，都会在清除缓存时清空，但不会自动清空
 *   temp目录的内容仅保留30天，超过30天会自动清除
 *   user目录保存与具体用户相关的所有信息
 */
object AAFFileWrapper {

    const val FILE_DELETE_DUREATION = 30 * 24 * 60 * 60 * 1000L

    init {
        FileUtils.deleteOldAsync(File(getTempFolder()), FILE_DELETE_DUREATION)
    }

    fun clear() {
        FileUtils.deleteDirectory(File(getCacheFolder()))
        FileUtils.deleteDirectory(File(getTempFolder()))
    }

    // 需要授权存储卡权限
    fun getShareFolder(fileType: String, subDir: String): String {
        val documentsDirectory = Environment.getExternalStoragePublicDirectory(fileType)
        val sharedDirectory = File(documentsDirectory, subDir)
        return FileUtils.getFolderPathWithSeparator(sharedDirectory.absolutePath)
    }

    fun getShareFolder(subDir: String): String {
        return getShareFolder(Environment.DIRECTORY_DOWNLOADS, subDir)
    }

    fun getShareFolder(): String {
        return getShareFolder(
            Environment.DIRECTORY_DOWNLOADS,
            ZixieContext.applicationContext!!.resources.getString(R.string.lib_bihe0832_file_folder),
        )
    }

    fun getFolder(): String {
        return FileUtils.getFolderPathWithSeparator(ZixieContext.getZixieFolder())
    }

    fun getFolder(name: String): String {
        return FileUtils.getFolderPathWithSeparator(ZixieContext.getZixieFolder() + name)
    }

    fun getCacheFolder(): String {
        return ZixieFileProvider.getZixieCacheFolder(ZixieContext.applicationContext)
    }

    fun getCacheFolder(name: String): String {
        return FileUtils.getFolderPathWithSeparator(getCacheFolder() + name)
    }

    fun getTempFolder(): String {
        return ZixieFileProvider.getZixieTempFolder(ZixieContext.applicationContext)
    }

    fun getTempFolder(name: String): String {
        val file = getTempFolder() + name + File.separator
        FileUtils.checkAndCreateFolder(file)
        return file
    }

    fun getMediaFolder(): String {
        return getFolder("media")
    }

    fun getMediaTempFolder(): String {
        return getTempFolder("media")
    }

    fun getTempImagePath(ext: String = ".jpg"): String {
        return getMediaTempFolder() + System.currentTimeMillis() + ext
    }

    fun getTempVideoPath(ext: String = ".mp4"): String {
        return getMediaTempFolder() + System.currentTimeMillis() + ext
    }

    fun getMediaCacheFolder(): String {
        return getCacheFolder("media")
    }

    fun getCacheImagePath(ext: String = ".jpg"): String {
        return getMediaCacheFolder() + System.currentTimeMillis() + ext
    }

    fun getCacheVideoPath(ext: String = ".mp4"): String {
        return getMediaCacheFolder() + System.currentTimeMillis() + ext
    }

    fun getFileFolder(): String {
        return getFolder("file")
    }

    fun getFileTempFolder(): String {
        return getTempFolder("file")
    }

    fun getFileCacheFolder(): String {
        return getCacheFolder("file")
    }
}
