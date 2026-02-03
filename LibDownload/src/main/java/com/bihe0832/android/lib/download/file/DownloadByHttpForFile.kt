package com.bihe0832.android.lib.download.file

import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadClientConfig
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_FILE_RENAME_FAILED
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_MD5_BAD
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadItem.TAG
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.core.DownloadExceptionAnalyzer
import com.bihe0832.android.lib.download.range.DownloadByHttpForRange
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.encrypt.messagedigest.MD5
import com.bihe0832.android.lib.utils.encrypt.messagedigest.SHA256
import java.io.File


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-10.
 * Description: 文件下载引擎的具体实现
 *
 */


class DownloadByHttpForFile(
    innerDownloadListener: DownloadListener,
    maxNum: Int,
    isDebug: Boolean = false,
    downloadClientConfig: DownloadClientConfig = DownloadClientConfig.createDefault()
) : DownloadByHttpForRange(innerDownloadListener, maxNum, isDebug, downloadClientConfig) {

    fun startDownload(info: DownloadItem) {
        ZLog.e(TAG, "开始下载:${info}")
        try {
            startDownload(info, DownloadItem.TYPE_FILE, 0, info.contentLength, 0)
        } catch (e: Throwable) {
            e.printStackTrace()
            if (info.status != DownloadStatus.STATUS_DOWNLOAD_PAUSED) {
                val errorCode = DownloadExceptionAnalyzer.analyzeException(e)
                notifyDownloadFailed(
                    info, errorCode, "download with exception: ${e.javaClass.simpleName}: ${e.message}"
                )
            }
        }
    }

    override fun notifyDownloadAfterFinish(downloadInfo: DownloadItem) {
        closeDownload(downloadInfo.downloadID, finishDownload = true, clearDownloadHistory = false)
        val downloadFile = downloadInfo.filePath
        ThreadManager.getInstance().start {
            try {
                val oldfile = File(downloadFile)
                ZLog.e(TAG, " oldfile:$oldfile")
                ZLog.e(TAG, " oldfile length:" + oldfile.length())
                if (TextUtils.isEmpty(downloadInfo.contentMD5)) {
                    if (TextUtils.isEmpty(downloadInfo.contentSHA256)) {
                        notifyDownloadSucc(downloadInfo)
                    } else {
                        val sha256 = SHA256.getFileSHA256(downloadFile)
                        ZLog.e(TAG, " file SHA256:$sha256")
                        ZLog.e(TAG, " para SHA256:" + downloadInfo.contentSHA256)
                        if (sha256.equals(downloadInfo.contentSHA256, ignoreCase = true)) {
                            notifyDownloadSucc(downloadInfo)
                        } else {
                            notifyDownloadFailed(
                                downloadInfo,
                                ERR_MD5_BAD,
                                "Sorry! the file SHA256 is bad"
                            )
                            if (downloadInfo.isForceDeleteBad) {
                                deleteFile(downloadInfo)
                            }
                        }
                    }
                } else {
                    val md5 = MD5.getFileMD5(downloadFile)
                    ZLog.e(TAG, " file md5:$md5")
                    ZLog.e(TAG, " para md5:" + downloadInfo.contentMD5)
                    if (md5.equals(downloadInfo.contentMD5, ignoreCase = true)) {
                        notifyDownloadSucc(downloadInfo)
                    } else {
                        notifyDownloadFailed(
                            downloadInfo,
                            ERR_MD5_BAD,
                            "Sorry! the file md5 is bad"
                        )
                        if (downloadInfo.isForceDeleteBad) {
                            deleteFile(downloadInfo)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                notifyDownloadFailed(
                    downloadInfo,
                    ERR_FILE_RENAME_FAILED,
                    "Sorry! the file can't be renamed"
                )
            }
        }
    }


}