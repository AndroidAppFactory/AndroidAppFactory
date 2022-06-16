package com.bihe0832.android.app.file

import android.net.Uri
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import java.io.File

/**
 *
 *   所有cache目录的内容，都会在清除缓存时清空
 *   temp目录的内容仅保留30天，超过30天会自动清除
 *   user目录保存与具体用户相关的所有信息
 *
 *   目前暂时没有区分个人与会话，后续，个人录制、拍摄的单独存储，会话收到的以会话为单位分组存储，所有KH相关的IM群组一律都设置为临时存储
 */
object AAFFileUtils {

    const val FILE_DELETE_DUREATION = 30 * 24 * 60 * 60 * 1000L

    init {
        FileUtils.deleteOldAsync(File(getTempFolder()), FILE_DELETE_DUREATION)
    }

    fun getFolder(): String {
        val file = ZixieContext.getZixieFolder() + File.separator
        FileUtils.checkAndCreateFolder(file)
        return file
    }

    fun getCacheFolder(): String {
        val file = getFolder() + "cache" + File.separator
        FileUtils.checkAndCreateFolder(file)
        return file
    }


    fun getTempFolder(): String {
        val file = getFolder() + "temp" + File.separator
        FileUtils.checkAndCreateFolder(file)
        return file
    }


    fun getMediaTempFolder(): String {
        val file = getTempFolder() + "media" + File.separator
        FileUtils.checkAndCreateFolder(file)
        return file
    }

    fun getMediaCacheFolder(): String {
        val file = getCacheFolder() + "media" + File.separator
        FileUtils.checkAndCreateFolder(file)
        return file
    }

    fun getFileTempFolder(): String {
        val file = getTempFolder() + "file" + File.separator
        FileUtils.checkAndCreateFolder(file)
        return file
    }

    fun getFileCacheFolder(): String {
        val file = getCacheFolder() + "file" + File.separator
        FileUtils.checkAndCreateFolder(file)
        return file
    }

    fun getSoundTempFolder(): String {
        val file = getCacheFolder() + "sound" + File.separator
        FileUtils.checkAndCreateFolder(file)
        return file
    }

    fun getSoundCacheFolder(): String {
        val file = getCacheFolder() + "sound" + File.separator
        FileUtils.checkAndCreateFolder(file)
        return file
    }

    fun getImageTempFolder(): String {
        val file = getCacheFolder() + "image" + File.separator
        FileUtils.checkAndCreateFolder(file)
        return file
    }

    fun getImageCacheFolder(): String {
        val file = getCacheFolder() + "image" + File.separator
        FileUtils.checkAndCreateFolder(file)
        return file
    }

    fun getVideoTempFolder(): String {
        val file = getCacheFolder() + "video" + File.separator
        FileUtils.checkAndCreateFolder(file)
        return file
    }

    fun getVideoCacheFolder(): String {
        val file = getCacheFolder() + "video" + File.separator
        FileUtils.checkAndCreateFolder(file)
        return file
    }

    fun getPathFromUri(uri: Uri?): String {
        uri?.let {
            return ZixieFileProvider.uriToFile(ZixieContext.applicationContext, uri).absolutePath
        }
        return ""
    }

    fun getUriFromPath(path: String?): Uri? {
        try {
            return ZixieFileProvider.getZixieFileProvider(ZixieContext.applicationContext!!, File(path))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}