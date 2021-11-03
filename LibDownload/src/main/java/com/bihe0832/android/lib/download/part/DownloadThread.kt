package com.bihe0832.android.lib.download.part

import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadItem.TAG
import com.bihe0832.android.lib.download.DownloadPartInfo
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.core.DownloadManager
import com.bihe0832.android.lib.download.core.logHeaderFields
import com.bihe0832.android.lib.download.core.upateRequestInfo
import com.bihe0832.android.lib.download.dabase.DownloadInfoDBManager
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.HTTPRequestUtils
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-10.
 * Description: 下载引擎的具体实现
 *
 */
//最小分片长度
const val DOWNLOAD_MIN_SIZE = 1024 * 50

//回调长度
const val DOWNLOAD_BUFFER_SIZE = 1024 * 8

//默认分片长度
const val DOWNLOAD_PART_SIZE = 1024 * 1024 * 10

class DownloadThread(private val mDownloadPartInfo: DownloadPartInfo) : Thread() {

    private var retryTimes = 0

    fun getDownloadPartInfo(): DownloadPartInfo {
        return mDownloadPartInfo
    }

    override fun run() {
        do {
            ZLog.e(TAG, "分片下载 分片信息:$mDownloadPartInfo")
            var newStart = when {
                mDownloadPartInfo.partEnd < 1 -> {
                    0
                }
                mDownloadPartInfo.partFinished > DOWNLOAD_BUFFER_SIZE -> {
                    if (retryTimes == 0) {
                        ZLog.e(TAG, "分片下载回退进度 第${mDownloadPartInfo.partID}分片: $mDownloadPartInfo")
                        mDownloadPartInfo.partStart + mDownloadPartInfo.partFinished - DOWNLOAD_BUFFER_SIZE
                    } else {
                        mDownloadPartInfo.partStart + mDownloadPartInfo.partFinished
                    }
                }
                else -> {
                    mDownloadPartInfo.partStart
                }
            }
            try {
                if (!startDownload(newStart)) {
                    break
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.partID}分片下载异常 $retryTimes！！！！: $e")
                DownloadInfoDBManager.updateDownloadFinished(mDownloadPartInfo.downloadPartID, mDownloadPartInfo.partFinished)
                sleep(3)
                if (retryTimes < 3) {
                    retryTimes++
                } else {
                    ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.partID}分片下载失败 $retryTimes！！！！: $e")
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                    break
                }
            }
        } while (true)
    }

    //return 是否需要重试
    private fun startDownload(finalStart: Long): Boolean {
        if (TextUtils.isEmpty(mDownloadPartInfo.finalFileName)) {
            ZLog.e("分片下载  分片信息错误，错误的本地路径：$mDownloadPartInfo")
            return false
        }

        val file = File(mDownloadPartInfo.finalFileName)
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        var availableSpace = FileUtils.getDirectoryAvailableSpace(file.parentFile.absolutePath)
        ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.partID}分片存储空间检查, path: ${file.parentFile.absolutePath}, availableSpace: $availableSpace, need: ${mDownloadPartInfo.partEnd - finalStart} ")
        if (mDownloadPartInfo.partEnd - finalStart > availableSpace) {
            mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
            ZLog.e(TAG, "分片下载失败 第${mDownloadPartInfo.partID}分片下载异常 $retryTimes！！！！存储空间不足, availableSpace: $availableSpace, need: ${mDownloadPartInfo.partEnd - finalStart} ")
            return false
        }
        if (mDownloadPartInfo.partEnd > 0) {
            if (finalStart < mDownloadPartInfo.partEnd) {
                ZLog.e(TAG, "分片下载开始 第${mDownloadPartInfo.partID}分片: start: ${mDownloadPartInfo.partStart}, finalStart : $finalStart end: ${mDownloadPartInfo.partEnd}")
            } else {
                ZLog.e(TAG, "分片下载开始 第${mDownloadPartInfo.partID}分片: 分片已经下载结束")
                mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
            }
        } else {
            ZLog.d(TAG, "分片下载 第${mDownloadPartInfo.partID}：分片长度异常，从头下载")
        }

        var randomAccessFile = RandomAccessFile(file, "rwd")
        randomAccessFile.seek(finalStart)

        val url = URL(mDownloadPartInfo.downloadURL)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            upateRequestInfo()
            if (mDownloadPartInfo.canDownloadByPart()) {
                ZLog.d(TAG, "分片下载 第${mDownloadPartInfo.partID}分片下载：params bytes=$finalStart-${mDownloadPartInfo.partEnd}")
                setRequestProperty("Range", "bytes=${finalStart}-${mDownloadPartInfo.partEnd}")
            }
        }
        var time = System.currentTimeMillis()
        connection.connect()
        ZLog.w(TAG, "分片下载 第${mDownloadPartInfo.partID}分片，请求用时: ${System.currentTimeMillis() - time} ~~~~~~~~~~~~~")
        if (DownloadManager.isDebug()) {
            connection.logHeaderFields("分片下载数据 第${mDownloadPartInfo.partID}分片")
        }

        var serverContentLength = HTTPRequestUtils.getContentLength(connection)
        ZLog.e(TAG, "~~~~~~~~~~~~~ 分片信息 第${mDownloadPartInfo.partID}分片 ~~~~~~~~~~~~~")
        ZLog.e(TAG, "分片下载数据 第${mDownloadPartInfo.partID}分片: getContentType:${connection.contentType}")
        ZLog.e(TAG, "分片下载数据 第${mDownloadPartInfo.partID}分片: responseCode:${connection.responseCode}")
        ZLog.e(TAG, "分片下载数据 第${mDownloadPartInfo.partID}分片: contentLength: start ${finalStart}, end ${mDownloadPartInfo.partEnd}, bytes=$finalStart-${mDownloadPartInfo.partEnd}")
        ZLog.e(TAG, "分片下载数据 第${mDownloadPartInfo.partID}分片: contentLength: from server ${serverContentLength}, local ${mDownloadPartInfo.partEnd - finalStart} ")
        ZLog.e(TAG, "分片下载数据 第${mDownloadPartInfo.partID}分片: finished ${mDownloadPartInfo.partFinished}, finished before: ${mDownloadPartInfo.partFinishedBefore} \n")

        if (connection.responseCode == HttpURLConnection.HTTP_OK || connection.responseCode == HttpURLConnection.HTTP_PARTIAL || connection.responseCode == 416) {
            if (mDownloadPartInfo.partEnd > 0 && abs(serverContentLength - (mDownloadPartInfo.partEnd - finalStart)) > 2) {
                ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.partID}分片长度 错误 ！！！")
                if (mDownloadPartInfo.partFinished > mDownloadPartInfo.partEnd - mDownloadPartInfo.partStart) {
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
                } else {
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                }
            } else {
                val inputStream = connection.inputStream
                val partSize = DOWNLOAD_BUFFER_SIZE
                val data = ByteArray(partSize)
                var len = -1
                var hasDownloadLength = 0L
                var lastUpdateTime = 0L
                while (inputStream.read(data).also { len = it } !== -1) {
                    if (mDownloadPartInfo.partStatus > DownloadStatus.STATUS_DOWNLOADING) {
                        DownloadInfoDBManager.updateDownloadFinished(mDownloadPartInfo.downloadPartID, hasDownloadLength + mDownloadPartInfo.partFinishedBefore)
                        break
                    }
                    if (mDownloadPartInfo.partStatus != DownloadStatus.STATUS_DOWNLOADING) {
                        mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOADING
                    }

                    // 读取成功,写入文件
                    randomAccessFile.write(data, 0, len)
                    hasDownloadLength += len

                    if (mDownloadPartInfo.canDownloadByPart() && mDownloadPartInfo.partFinished + len > mDownloadPartInfo.partEnd - mDownloadPartInfo.partStart) {
                        ZLog.e(TAG, "分片下载数据 第${mDownloadPartInfo.partID}分片累积下载超长！！！分片长度：${mDownloadPartInfo.partEnd - mDownloadPartInfo.partStart}, 累积下载长度：${mDownloadPartInfo.partFinished + len}")
                        ZLog.e(TAG, "分片下载数据 第${mDownloadPartInfo.partID}分片累积下载超长！！！${mDownloadPartInfo}")
                    } else {
                        mDownloadPartInfo.partFinished = mDownloadPartInfo.partFinished + len
                        if (mDownloadPartInfo.canDownloadByPart() && System.currentTimeMillis() - lastUpdateTime > 10 * 1000) {
                            //  if(isDebug) ZLog.e("分片下载数据保存 - ${mDownloadPartInfo.downloadPartID}：实际下载:${FileUtils.getFileLength(len.toLong())}")
                            DownloadInfoDBManager.updateDownloadFinished(mDownloadPartInfo.downloadPartID, hasDownloadLength + mDownloadPartInfo.partFinishedBefore)
                            lastUpdateTime = System.currentTimeMillis()
                        }
                    }

                    if (retryTimes > 0) {
                        ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.partID}分片重试次数将被重置")
                        retryTimes = 0
                    }
                }
                ZLog.e(TAG, "分片下载数据 第${mDownloadPartInfo.partID}分片结束：分片长度：${mDownloadPartInfo.partEnd - mDownloadPartInfo.partStart}, 本次本地计算长度:${mDownloadPartInfo.partEnd - finalStart} ;本次服务器下发长度: $serverContentLength")
                ZLog.e(TAG, "分片下载数据 第${mDownloadPartInfo.partID}分片结束：分片长度：${mDownloadPartInfo.partEnd - mDownloadPartInfo.partStart}, 本次实际下载长度:${hasDownloadLength} ;累积下载长度: ${mDownloadPartInfo.partFinished}")
                if (mDownloadPartInfo.canDownloadByPart()) {
                    DownloadInfoDBManager.updateDownloadFinished(mDownloadPartInfo.downloadPartID, hasDownloadLength + mDownloadPartInfo.partFinishedBefore)
                }
                if (mDownloadPartInfo.canDownloadByPart()) {
                    if (hasDownloadLength >= mDownloadPartInfo.partEnd - finalStart) {
                        ZLog.e(TAG, "分片下载数据 第${mDownloadPartInfo.partID}分片下载数据修正: 本次实际下载：$hasDownloadLength 本次计划下载大小：${mDownloadPartInfo.partEnd - finalStart}")
                        mDownloadPartInfo.partFinished = mDownloadPartInfo.partEnd - mDownloadPartInfo.partStart
                        mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
                    } else {
                        mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                    }
                } else {
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
                }
                DownloadInfoDBManager.updateDownloadFinished(mDownloadPartInfo.downloadPartID, mDownloadPartInfo.partFinished)

                ZLog.e(TAG, "分片下载数据 第${mDownloadPartInfo.partID}分片下载结束: $mDownloadPartInfo")

                try {
                    inputStream.close()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }

                try {
                    connection.disconnect()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                try {
                    randomAccessFile.close()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            return true
        }

        return false
    }
}