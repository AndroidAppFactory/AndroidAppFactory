package com.bihe0832.android.lib.download.part

import com.bihe0832.android.lib.download.DownloadItem.TAG
import com.bihe0832.android.lib.download.DownloadPartInfo
import com.bihe0832.android.lib.download.DownloadStatus
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
const val DOWNLOAD_PART_SIZE = 1024 * 1024 * 2

class DownloadThread(private val mDownloadPartInfo: DownloadPartInfo) : Thread() {

    fun getDownloadPartInfo(): DownloadPartInfo {
        return mDownloadPartInfo
    }

    override fun run() {
        var times = 0
        do {
            ZLog.e(TAG, "run:$mDownloadPartInfo")
            if (mDownloadPartInfo.partFinished > DOWNLOAD_BUFFER_SIZE) {
                mDownloadPartInfo.partFinished = mDownloadPartInfo.partFinished - DOWNLOAD_BUFFER_SIZE
            } else {
                mDownloadPartInfo.partFinished = 0
            }
            var newStart = mDownloadPartInfo.partStart + mDownloadPartInfo.partFinished
            try {
                if (!startDownload(newStart)) {
                    break
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.partID}分片下载异常！！！！: $e")
                sleep(3)
                if (times < 3) {
                    times++
                } else {
                    ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.partID}分片下载失败！！！！: $e")
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                    break
                }
            }
        } while (true)
    }

    //return 是否需要重试
    private fun startDownload(finalStart: Long): Boolean {

        val file = File(mDownloadPartInfo.finalFileName)
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        var randomAccessFile = RandomAccessFile(file, "rwd")
        if (finalStart < mDownloadPartInfo.partEnd) {
            ZLog.d("分片下载 第${mDownloadPartInfo.partID}：start: $mDownloadPartInfo.start, finalStart : $finalStart end: ${mDownloadPartInfo.partEnd}")
        } else {
            ZLog.d("分片下载 第${mDownloadPartInfo.partID}：已经分片结束")
            mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
        }

        val url = URL(mDownloadPartInfo.downloadURL)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            upateRequestInfo()
            ZLog.d("第${mDownloadPartInfo.partID}分片下载：params bytes=$finalStart-${mDownloadPartInfo.partEnd}")
            setRequestProperty("Range", "bytes=${finalStart}-${mDownloadPartInfo.partEnd}")

        }
        connection.connect()
        randomAccessFile.seek(finalStart)
        var contentLength = HTTPRequestUtils.getContentLength(connection)
        ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.partID}：getContentType:${connection.contentType}")
        ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.partID}：params bytes=$finalStart-${mDownloadPartInfo.partEnd} getContentLength:${contentLength}")
        ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.partID}：responseCode:${connection.responseCode}")
        ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.partID}：contentLength: start ${finalStart}, end ${mDownloadPartInfo.partEnd} ")
        ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.partID}：contentLength from server ${contentLength}, local ${mDownloadPartInfo.partEnd - finalStart} ")
        ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.partID}：finished ${mDownloadPartInfo.partFinished}, finished before: ${mDownloadPartInfo.partFinishedBefore} ")


        if (connection.responseCode == HttpURLConnection.HTTP_OK || connection.responseCode == HttpURLConnection.HTTP_PARTIAL || connection.responseCode == 416) {
            val length = HTTPRequestUtils.getContentLength(connection)
            if (abs(length - (mDownloadPartInfo.partEnd - finalStart)) > 2) {
                ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.partID}分片长度 错误 ！！！")
                if (mDownloadPartInfo.partFinished > mDownloadPartInfo.partEnd - mDownloadPartInfo.partStart) {
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
                } else {
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                }
            } else {
                val inputStream = url.openStream()
//                val inputStream = connection.inputStream
                val partSize = DOWNLOAD_BUFFER_SIZE
                val data = ByteArray(partSize)
                var len = -1
                var hasdownloadLength = 0L
                while (inputStream.read(data).also { len = it } !== -1) {
                    if (mDownloadPartInfo.partStatus > DownloadStatus.STATUS_DOWNLOADING) {
                        break
                    }
                    if (mDownloadPartInfo.partStatus != DownloadStatus.STATUS_DOWNLOADING) {
                        mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOADING
                    }
                    // 读取成功,写入文件
                    randomAccessFile.write(data, 0, len)
                    mDownloadPartInfo.partFinished = mDownloadPartInfo.partFinished + len
                    hasdownloadLength += len
                    if (hasdownloadLength % (partSize * 10) < partSize && mDownloadPartInfo.canDownloadByPart()) {
                        //  if(isDebug) ZLog.e("分片下载数据保存 - ${mDownloadPartInfo.downloadPartID}：实际下载:${FileUtils.getFileLength(len.toLong())}")
                        DownloadInfoDBManager.updateDownloadFinished(mDownloadPartInfo.downloadPartID, hasdownloadLength + mDownloadPartInfo.partFinishedBefore)
                    }
                }
                if (mDownloadPartInfo.canDownloadByPart()) {
                    DownloadInfoDBManager.updateDownloadFinished(mDownloadPartInfo.downloadPartID, hasdownloadLength + mDownloadPartInfo.partFinishedBefore)
                }
                ZLog.e("分片下载数据保存 - ${mDownloadPartInfo.downloadPartID}：实际下载:${FileUtils.getFileLength(hasdownloadLength)}")
                ZLog.e(TAG, "分片下载数据 - ${mDownloadPartInfo.downloadID}：第${mDownloadPartInfo.partID}分片结束：实际下载:${FileUtils.getFileLength(hasdownloadLength)} ;分片完成: ${FileUtils.getFileLength(mDownloadPartInfo.partFinished)}, 计划下载 ${FileUtils.getFileLength(length)},")
                if (hasdownloadLength >= mDownloadPartInfo.partEnd - finalStart) {
                    mDownloadPartInfo.partFinished = mDownloadPartInfo.partEnd - mDownloadPartInfo.partStart + 1
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
                }
                ZLog.e(TAG, "第${mDownloadPartInfo.partID}分片下载结束：partStatus: ${mDownloadPartInfo.partStatus}")
                try {
                    inputStream.close()
                    connection.disconnect()
                    randomAccessFile.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            return true
        }
        return false
    }
}