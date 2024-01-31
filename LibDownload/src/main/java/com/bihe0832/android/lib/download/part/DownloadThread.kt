package com.bihe0832.android.lib.download.part

import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadItem.TAG
import com.bihe0832.android.lib.download.DownloadPartInfo
import com.bihe0832.android.lib.download.DownloadStatus
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
 * 如果partEnd == 0 ，兼容不支持分片下载的逻辑，直接一次读取所有内容
 *
 * TAG :
 *  分片下载开始：开始下载时关键日志
 *  分片下载数据：下载详细数据
 *  分片下载 第*分片：分片核心逻辑
 *  分片下载结束： 下载结束以后日志
 */
// 最小分片长度, 50K
const val DOWNLOAD_MIN_SIZE = 1024 * 50

// 回调长度 8K
const val DOWNLOAD_BUFFER_SIZE = 1024 * 8 * 2

// 默认分片长度, 10M
const val DOWNLOAD_PART_SIZE = 1024 * 1024 * 10

// 异常或失败重试次数
const val DOWNLOAD_RETRY_TIMES = 3

class DownloadThread(private val mDownloadPartInfo: DownloadPartInfo) : Thread() {

    // 10秒（弱网）或 10M(高速网络)保存策略
    private val DOWNLOAD_SVAE_TIMER = 10 * 1000
    private val DOWNLOAD_SVAE_SIZE = DOWNLOAD_BUFFER_SIZE * 125 * 5

    private var retryTimes = 0

    fun getDownloadPartInfo(): DownloadPartInfo {
        return mDownloadPartInfo
    }

    override fun run() {
        ZLog.e(TAG, "分片下载准备 分片信息:$mDownloadPartInfo")

        if (TextUtils.isEmpty(mDownloadPartInfo.finalFileName)) {
            ZLog.e("分片下载开始  分片信息错误，错误的本地路径：$mDownloadPartInfo")
            mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
            return
        }

        if (mDownloadPartInfo.partFinished > 0 && mDownloadPartInfo.partStart + mDownloadPartInfo.partFinished == mDownloadPartInfo.partEnd) {
            ZLog.e(TAG, "分片下载开始 第${mDownloadPartInfo.downloadPartID}分片: 分片此前已经下载结束")
            mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
            return
        }

        val file = File(mDownloadPartInfo.finalFileName)
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        val randomAccessFile = RandomAccessFile(file, "rw")

        do {
            var newStart = if (mDownloadPartInfo.partEnd > 0) {
                when {
                    mDownloadPartInfo.partFinished > DOWNLOAD_BUFFER_SIZE -> {
                        ZLog.w(TAG, "分片下载开始 回退进度简要信息 第${mDownloadPartInfo.downloadPartID}分片:  可以回退")
                        mDownloadPartInfo.partStart + mDownloadPartInfo.partFinished - DOWNLOAD_BUFFER_SIZE
                    }

                    else -> {
                        mDownloadPartInfo.partStart
                    }
                }
            } else {
                0
            }

            if (newStart < 0) {
                newStart = 0
            }

            var availableSpace = FileUtils.getDirectoryAvailableSpace(file.parentFile.absolutePath)
            var needSpace = mDownloadPartInfo.partEnd - newStart
            if (needSpace > 0 && needSpace > availableSpace) {
                mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                ZLog.e(
                    TAG,
                    "分片下载开始 第${mDownloadPartInfo.downloadPartID}分片失败 下载异常 $retryTimes！！！！存储空间不足, availableSpace: $availableSpace, need: ${mDownloadPartInfo.partEnd - newStart} ",
                )
                break
            }

            ZLog.e(
                TAG,
                "分片下载开始 第${mDownloadPartInfo.downloadPartID}分片回退进度简要信息 分片下载前:  分片开头：${mDownloadPartInfo.partStart}, 本次开头：${mDownloadPartInfo.partStart + mDownloadPartInfo.partFinished} 回退后开头：$newStart, 分片结束 ${mDownloadPartInfo.partEnd}, 已完成：${mDownloadPartInfo.partFinished}",
            )
            mDownloadPartInfo.partFinished = newStart - mDownloadPartInfo.partStart
            mDownloadPartInfo.partFinishedBefore = mDownloadPartInfo.partFinished
            DownloadInfoDBManager.saveDownloadPartInfo(mDownloadPartInfo)

            if (mDownloadPartInfo.partEnd > 0) {
                ZLog.e(
                    TAG,
                    "分片下载开始 第${mDownloadPartInfo.downloadPartID}分片 开始: start: ${mDownloadPartInfo.partStart}, finalStart : $newStart end: ${mDownloadPartInfo.partEnd}",
                )
            } else {
                ZLog.d(TAG, "分片下载开始 第${mDownloadPartInfo.downloadPartID}：分片长度异常，从头下载")
            }
            try {
                randomAccessFile.seek(newStart)
            } catch (e: Exception) {
                e.printStackTrace()
                ZLog.e(TAG, "分片下载开始 第${mDownloadPartInfo.downloadPartID}分片 文件移动异常 :${e.javaClass.name}")
                mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                break
            }

            try {
                if (!startDownload(randomAccessFile, newStart)) {
                    break
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.downloadPartID}分片下载异常 $retryTimes！！！！:${e.javaClass.name}")
                DownloadInfoDBManager.updateDownloadFinished(
                    mDownloadPartInfo.downloadPartID,
                    mDownloadPartInfo.partFinished,
                )
                sleep(3)
                if (retryTimes < DOWNLOAD_RETRY_TIMES) {
                    retryTimes++
                } else {
                    ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.downloadPartID}分片下载失败 $retryTimes！！！！:${e.javaClass.name}")
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                    break
                }
            }
        } while (true)
        try {
            randomAccessFile.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    // return 是否需要重试
    private fun startDownload(
        randomAccessFile: RandomAccessFile,
        finalStart: Long,
    ): Boolean {
        ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.downloadPartID}分片: 开始start: ${mDownloadPartInfo.partStart}, finalStart : $finalStart end: ${mDownloadPartInfo.partEnd}")
        val url = URL(mDownloadPartInfo.realDownloadURL)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            upateRequestInfo()
            if (mDownloadPartInfo.partEnd > 0) {
                ZLog.e(
                    TAG,
                    "分片下载 第${mDownloadPartInfo.downloadPartID}分片: 下载params bytes=$finalStart-${mDownloadPartInfo.partEnd}",
                )
                setRequestProperty("Range", "bytes=$finalStart-${mDownloadPartInfo.partEnd}")
            }
        }
        var time = System.currentTimeMillis()
        connection.connect()
        ZLog.w(
            TAG,
            "分片下载 第${mDownloadPartInfo.downloadPartID}分片: 请求用时: ${System.currentTimeMillis() - time} ~~~~~~~~~~~~~",
        )
        connection.logHeaderFields("分片下载数据 第${mDownloadPartInfo.downloadPartID}分片")

        var serverContentLength = HTTPRequestUtils.getContentLength(connection)
        var localContentLength = mDownloadPartInfo.partEnd - finalStart
        ZLog.e(TAG, "~~~~~~~~~~~~~ 分片信息 第${mDownloadPartInfo.downloadPartID}分片 ~~~~~~~~~~~~~")
        ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.downloadPartID}分片: getContentType:${connection.contentType}")
        ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.downloadPartID}分片: responseCode:${connection.responseCode}")
        ZLog.e(
            TAG,
            "分片下载 第${mDownloadPartInfo.downloadPartID}分片: contentLength: origin start ${mDownloadPartInfo.partStart}, final start $finalStart, end ${mDownloadPartInfo.partEnd}, bytes=$finalStart-${mDownloadPartInfo.partEnd}",
        )
        ZLog.e(
            TAG,
            "分片下载 第${mDownloadPartInfo.downloadPartID}分片: contentLength: from server $serverContentLength, local $localContentLength ",
        )
        ZLog.e(
            TAG,
            "分片下载 第${mDownloadPartInfo.downloadPartID}分片: finished ${mDownloadPartInfo.partFinished}, finished before: ${mDownloadPartInfo.partFinishedBefore} \n",
        )

        if (connection.responseCode == HttpURLConnection.HTTP_OK || connection.responseCode == HttpURLConnection.HTTP_PARTIAL || connection.responseCode == 416) {
            if (mDownloadPartInfo.partEnd > 0) {
                // 分片下载
                if (abs(serverContentLength - localContentLength) > 1) {
                    ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.downloadPartID}分片长度 错误 ！！！")
                    if (mDownloadPartInfo.partFinished >= mDownloadPartInfo.partEnd - mDownloadPartInfo.partStart) {
                        mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
                    } else {
                        DownloadInfoDBManager.clearDownloadPartByID(mDownloadPartInfo.downloadID)
                        mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                    }
                    return false
                } else {
                    if (serverContentLength < 1L) {
                        ZLog.e(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
                        ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.downloadPartID}分片长度为0 ！！！ $retryTimes")
                        ZLog.e(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
                        mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                        return false
                    } else {
                        // go on
                    }
                }
            } else {
                // 单片下载
                if (mDownloadPartInfo.partID > 0) {
                    ZLog.e(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
                    ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.downloadPartID}分片长度为0 ！！！ $retryTimes")
                    ZLog.e(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                    return false
                }
            }

            val inputStream = connection.inputStream
            val data = ByteArray(DOWNLOAD_BUFFER_SIZE)
            var len = -1
            var hasDownloadLength = 0L
            var lastUpdateTime = 0L
            var lastUpdateLength = 0L
            while (inputStream.read(data).also { len = it } !== -1) {
                if (mDownloadPartInfo.partStatus > DownloadStatus.STATUS_DOWNLOADING) {
                    // 下载完成或者失败
                    DownloadInfoDBManager.updateDownloadFinished(
                        mDownloadPartInfo.downloadPartID,
                        hasDownloadLength + mDownloadPartInfo.partFinishedBefore,
                    )
                    return false
                }
                if (mDownloadPartInfo.partStatus != DownloadStatus.STATUS_DOWNLOADING) {
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOADING
                }

                // 读取成功,写入文件
                randomAccessFile.write(data, 0, len)
                hasDownloadLength += len

                if (mDownloadPartInfo.partEnd > 0 && mDownloadPartInfo.partFinished + len - 1 > mDownloadPartInfo.partEnd - mDownloadPartInfo.partStart) {
                    ZLog.e(
                        TAG,
                        "分片下载数据 第${mDownloadPartInfo.downloadPartID}分片累积下载超长！！！分片长度：${mDownloadPartInfo.partEnd - mDownloadPartInfo.partStart}, 累积下载长度：${mDownloadPartInfo.partFinished + len}",
                    )
                    ZLog.e(
                        TAG,
                        "分片下载数据 第${mDownloadPartInfo.downloadPartID}分片累积下载超长！！！$mDownloadPartInfo",
                    )
                    // 继续下载，长度不增加
                } else {
                    // 10秒（弱网）或 2M(高速网络)保存策略
                    if (System.currentTimeMillis() - lastUpdateTime > DOWNLOAD_SVAE_TIMER || hasDownloadLength - lastUpdateLength > DOWNLOAD_SVAE_SIZE) {
                        ZLog.w(
                            TAG,
                            "分片下载数据 - ${mDownloadPartInfo.downloadPartID} 分片存储： 距离上次存储时间：${System.currentTimeMillis() - lastUpdateTime}, 新增：${hasDownloadLength - lastUpdateLength} 已下载长度：$hasDownloadLength 上次存储下载长度：$lastUpdateLength",
                        )
                        DownloadInfoDBManager.updateDownloadFinished(
                            mDownloadPartInfo.downloadPartID,
                            hasDownloadLength + mDownloadPartInfo.partFinishedBefore,
                        )
                        lastUpdateTime = System.currentTimeMillis()
                        lastUpdateLength = hasDownloadLength
                    }
                    mDownloadPartInfo.partFinished = mDownloadPartInfo.partFinished + len
                }
                if (retryTimes > 0) {
                    ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.downloadPartID}分片重试次数将被重置")
                    retryTimes = 0
                }
            }
            ZLog.e(
                TAG,
                "分片下载结束 第${mDownloadPartInfo.downloadPartID}分片：分片长度：${mDownloadPartInfo.partEnd - mDownloadPartInfo.partStart}, 本次本地计算要长度:${mDownloadPartInfo.partEnd - finalStart} ;本次服务器下发长度: $serverContentLength",
            )
            ZLog.e(
                TAG,
                "分片下载结束 第${mDownloadPartInfo.downloadPartID}分片：分片长度：${mDownloadPartInfo.partEnd - mDownloadPartInfo.partStart}, 本次下载长度:$hasDownloadLength ;累积下载长度: ${mDownloadPartInfo.partFinished}",
            )
            // 下载结束
            DownloadInfoDBManager.updateDownloadFinished(
                mDownloadPartInfo.downloadPartID,
                hasDownloadLength + mDownloadPartInfo.partFinishedBefore,
            )
            if (hasDownloadLength == serverContentLength) {
                if (hasDownloadLength >= localContentLength) {
                    ZLog.e(
                        TAG,
                        "分片下载数据 第${mDownloadPartInfo.downloadPartID}分片下载数据修正: 本次实际下载：$hasDownloadLength 本次计划下载大小：$localContentLength",
                    )
                    mDownloadPartInfo.partFinished = mDownloadPartInfo.partEnd - mDownloadPartInfo.partStart
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
                } else {
                    ZLog.e(
                        TAG,
                        "分片下载数据 第${mDownloadPartInfo.downloadPartID}分片下载异常: 本次计划下载：$localContentLength 服务器下发长度: $serverContentLength 服务器实际返回：$hasDownloadLength ",
                    )
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                }
            } else if (serverContentLength < 1 && hasDownloadLength > 0) {
                // content-length = 0 ， 但是有数据
                ZLog.e(
                    TAG,
                    "分片下载数据 第${mDownloadPartInfo.downloadPartID}分片下载数据修正: 本次实际下载：$hasDownloadLength 服务器下发长度: $serverContentLength  本次计划下载大小：$localContentLength",
                )
                mDownloadPartInfo.partFinished = mDownloadPartInfo.partEnd - mDownloadPartInfo.partStart
                mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
            } else {
                ZLog.e(
                    TAG,
                    "分片下载数据 第${mDownloadPartInfo.downloadPartID}分片下载异常: 本次计划下载：$localContentLength 服务器下发长度: $serverContentLength 服务器实际返回：$hasDownloadLength ",
                )
                mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
            }
            // 数据修正后存储
            DownloadInfoDBManager.updateDownloadFinished(mDownloadPartInfo.downloadPartID, mDownloadPartInfo.partFinished)
            ZLog.e(TAG, "分片下载数据 第${mDownloadPartInfo.downloadPartID}分片下载结束: $mDownloadPartInfo")

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
        } else {
            return true
        }
        return false
    }
}
