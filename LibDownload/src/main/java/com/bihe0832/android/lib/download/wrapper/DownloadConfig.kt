package com.bihe0832.android.lib.download.wrapper

import android.content.Context
import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadErrorCode
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog

/**
 * 配置文件下载工具类
 *
 * 专门用于下载配置文件（如 JSON、XML、TXT 等文本配置），提供以下功能：
 * - 自动下载远程配置文件
 * - 支持本地缓存（优先使用本地缓存）
 * - MD5 校验确保配置完整性
 * - 自动解析文件内容并回调
 * - 高优先级下载（配置文件通常需要优先获取）
 * - 4G 网络自动下载（配置文件通常较小）
 *
 * 使用场景：
 * 1. 应用启动时拉取远程配置
 * 2. 动态更新业务配置
 * 3. 热更新配置文件
 * 4. A/B 测试配置下载
 *
 * 回调类型说明：
 * - TYPE_NEW: 从云端重新拉取的配置
 * - TYPE_NEW_LOCAL: 使用本地缓存的配置
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-10.
 * Description: 配置文件下载工具类，简化配置文件的下载和缓存管理
 */
object DownloadConfig {

    interface ResponseHandler {
        companion object {
            const val ERROR_CONFIG = -1
            const val ERROR_CDN_MD5 = -2
            const val ERROR_EXCEPTION = -3
            const val ERROR_OTHERS = -3
            const val ERROR_DATA_EMPTY = -4

            //云端重新拉取
            const val TYPE_NEW = 1

            //使用本地缓存
            const val TYPE_NEW_LOCAL = 2
        }

        fun onSuccess(type: Int, response: String)
        fun onFailed(errorCode: Int, msg: String)
    }

    //直接下载，不显示进度，4G下载直接下载
    fun download(
        context: Context,
        url: String,
        cacheFilePath: String,
        md5: String,
        downloadListener: ResponseHandler
    ) {
        startDownloadConfig(context, url, cacheFilePath, md5, downloadListener)
    }

    private fun startDownloadConfig(
        context: Context,
        url: String,
        cacheFilePath: String,
        md5: String,
        downloadListener: ResponseHandler
    ) {
        if (TextUtils.isEmpty(url)) {
            downloadListener.onFailed(ResponseHandler.ERROR_CONFIG, "url is bad")
        }
        DownloadFileUtils.startDownload(context, DownloadItem().apply {
            setNotificationVisibility(false)
            downloadPriority = DownloadItem.MAX_DOWNLOAD_PRIORITY
            downloadURL = url
            contentMD5 = md5
            actionKey = DownloadFileUtils.DOWNLOAD_ACTION_KEY_CONFIG
            isDownloadWhenUseMobile = true
            setShouldForceReDownload(TextUtils.isEmpty(md5))
            fileFolder = cacheFilePath
            isNeedRecord = false
            this.downloadListener = object : SimpleDownloadListener() {
                override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                    if (DownloadErrorCode.ERR_MD5_BAD == errorCode) {
                        downloadListener.onFailed(ResponseHandler.ERROR_CDN_MD5, "$errorCode $msg")
                    } else {
                        downloadListener.onFailed(ResponseHandler.ERROR_OTHERS, "$errorCode $msg")
                    }
                }

                override fun onComplete(filePath: String, item: DownloadItem): String {
                    try {
                        FileUtils.getFileContent(filePath).let {
                            if (it.isNotEmpty()) {
                                if (item.status == DownloadStatus.STATUS_HAS_DOWNLOAD) {
                                    downloadListener.onSuccess(ResponseHandler.TYPE_NEW_LOCAL, it)
                                } else {
                                    downloadListener.onSuccess(ResponseHandler.TYPE_NEW, it)
                                }
                            } else {
                                downloadListener.onFailed(ResponseHandler.ERROR_DATA_EMPTY, it)
                            }
                        }
                    } catch (e: Exception) {
                        downloadListener.onFailed(
                            ResponseHandler.ERROR_EXCEPTION,
                            e.message.toString()
                        )
                    }
                    return filePath
                }

                override fun onProgress(item: DownloadItem) {
                    ZLog.d("onProgress :$item")
                }
            }
        })
    }
}