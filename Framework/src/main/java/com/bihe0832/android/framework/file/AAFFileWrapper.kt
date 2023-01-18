package com.bihe0832.android.framework.file

import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.file.FileUtils
import java.io.File

/**
 *
 *   所有cache目录的内容，都会在清除缓存时清空
 *   temp目录的内容仅保留30天，超过30天会自动清除
 *   user目录保存与具体用户相关的所有信息
 */
object AAFFileWrapper {

    const val FILE_DELETE_DUREATION = 30 * 24 * 60 * 60 * 1000L

    init {
        FileUtils.deleteOldAsync(File(getTempFolder()), FILE_DELETE_DUREATION)
    }

    fun getFolder(): String {
        return FileUtils.getFolderPathWithSeparator(ZixieContext.getZixieFolder())
    }

    fun getCacheFolder(): String {
        return FileUtils.getFolderPathWithSeparator(getFolder() + "cache")
    }

    fun getTempFolder(): String {
        return FileUtils.getFolderPathWithSeparator(getFolder() + "temp")
    }

    fun getMediaTempFolder(): String {
        return FileUtils.getFolderPathWithSeparator(getTempFolder() + "media")
    }

    fun getMediaCacheFolder(): String {
        return FileUtils.getFolderPathWithSeparator(getCacheFolder() + "media")
    }

    fun getFileTempFolder(): String {
        return FileUtils.getFolderPathWithSeparator(getTempFolder() + "file")
    }

    fun getFileCacheFolder(): String {
        return FileUtils.getFolderPathWithSeparator(getCacheFolder() + "file")
    }

    fun getSoundTempFolder(): String {
        return FileUtils.getFolderPathWithSeparator(getCacheFolder() + "sound")
    }

    fun getSoundCacheFolder(): String {
        return FileUtils.getFolderPathWithSeparator(getCacheFolder() + "sound")
    }

    fun getImageTempFolder(): String {
        return FileUtils.getFolderPathWithSeparator(getCacheFolder() + "image")
    }

    fun getImageCacheFolder(): String {
        return FileUtils.getFolderPathWithSeparator(getCacheFolder() + "image")
    }

    fun getVideoTempFolder(): String {
        return FileUtils.getFolderPathWithSeparator(getCacheFolder() + "video")
    }

    fun getVideoCacheFolder(): String {
        return FileUtils.getFolderPathWithSeparator(getCacheFolder() + "video" + File.separator)
    }
}