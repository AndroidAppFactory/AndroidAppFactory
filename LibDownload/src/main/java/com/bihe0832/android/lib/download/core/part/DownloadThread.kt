package com.bihe0832.android.lib.download.core.part

import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadItem.TAG
import com.bihe0832.android.lib.download.DownloadPartInfo
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.core.dabase.DownloadInfoDBManager
import com.bihe0832.android.lib.download.core.logRequestHeaderFields
import com.bihe0832.android.lib.download.core.logResponseHeaderFields
import com.bihe0832.android.lib.download.core.upateRequestInfo
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

    companion object {
        /** 保存进度的时间间隔（毫秒），适用于弱网环境 */
        private const val DOWNLOAD_SAVE_TIMER = 10 * 1000

        /** 保存进度的大小间隔（字节），适用于高速网络，约 10MB */
        private const val DOWNLOAD_SAVE_SIZE = DOWNLOAD_BUFFER_SIZE * 125 * 5
    }

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

        if (mDownloadPartInfo.partFinished > 0 && mDownloadPartInfo.partFinished == mDownloadPartInfo.partLength) {
            ZLog.e(TAG, "分片下载开始 第${mDownloadPartInfo.downloadPartID}分片: 分片此前已经下载结束")
            mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
            return
        }

        val file = File(mDownloadPartInfo.finalFileName)
        val finishedBeforeLength = when {
            mDownloadPartInfo.partFinished > DOWNLOAD_BUFFER_SIZE -> {
                ZLog.w(TAG, "分片下载开始 回退进度简要信息 第${mDownloadPartInfo.downloadPartID}分片:  可以回退")
                mDownloadPartInfo.partFinished - DOWNLOAD_BUFFER_SIZE
            }

            else -> {
                0
            }
        }
        val availableSpace = FileUtils.getDirectoryAvailableSpace(file.parentFile.absolutePath)
        val needSpace = mDownloadPartInfo.partLength - finishedBeforeLength
        if (needSpace > 0 && needSpace > availableSpace) {
            mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
            ZLog.e(
                TAG,
                "分片下载开始 第${mDownloadPartInfo.downloadPartID}分片失败 下载异常 $retryTimes！！！！存储空间不足, availableSpace: $availableSpace, need: $finishedBeforeLength ",
            )
            return
        }

        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        val randomAccessFile = try {
            RandomAccessFile(file, "rw")
        } catch (e: Exception) {
            e.printStackTrace()
            ZLog.e(TAG, "RandomAccessFile 创建失败: ${e.message}")
            mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
            return
        }

        try {
            do {
            ZLog.e(
                TAG,
                "分片下载开始 回退进度前简要信息 第${mDownloadPartInfo.downloadPartID}分片下载前: 分片长度：${mDownloadPartInfo.partLength}, 分片Range开头：${mDownloadPartInfo.partRangeStart}, 分片本地开头：${mDownloadPartInfo.partLocalStart}, 已完成：${mDownloadPartInfo.partFinished} ",
            )
            mDownloadPartInfo.partFinished = finishedBeforeLength
            mDownloadPartInfo.partFinishedBefore = mDownloadPartInfo.partFinished
            DownloadInfoDBManager.saveDownloadPartInfo(mDownloadPartInfo)
            ZLog.e(
                TAG,
                "分片下载开始 回退进度后简要信息 第${mDownloadPartInfo.downloadPartID}分片下载前: 分片长度：${mDownloadPartInfo.partLength}, 分片Range开头：${mDownloadPartInfo.partRangeStart}, 分片本地开头：${mDownloadPartInfo.partLocalStart}, 已完成：${mDownloadPartInfo.partFinished} ",
            )

            try {
                ZLog.e(
                    TAG,
                    "分片下载开始 第${mDownloadPartInfo.downloadPartID}分片 开始: 本次Range开头：${mDownloadPartInfo.partRangeStart + finishedBeforeLength}, 本次Local开头：${mDownloadPartInfo.partLocalStart + finishedBeforeLength},  本次Range结尾: ${mDownloadPartInfo.partRangeEnd}",
                )
                randomAccessFile.seek(mDownloadPartInfo.partLocalStart + finishedBeforeLength)
            } catch (e: Exception) {
                e.printStackTrace()
                ZLog.e(TAG, "分片下载开始 第${mDownloadPartInfo.downloadPartID}分片 文件移动异常 :${e.javaClass.name}")
                mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                break
            }

            try {
                val rangeStart = mDownloadPartInfo.partRangeStart + finishedBeforeLength
                if (!startDownload(randomAccessFile, rangeStart, mDownloadPartInfo.partRangeEnd)) {
                    break
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ZLog.e(
                    TAG,
                    "分片下载 第${mDownloadPartInfo.downloadPartID}分片下载异常 $retryTimes！！！！:${e.javaClass.name}"
                )
                DownloadInfoDBManager.updateDownloadFinished(
                    mDownloadPartInfo.downloadPartID,
                    mDownloadPartInfo.partFinished,
                )
                sleep(3)
                if (retryTimes < DOWNLOAD_RETRY_TIMES) {
                    retryTimes++
                } else {
                    ZLog.e(
                        TAG,
                        "分片下载 第${mDownloadPartInfo.downloadPartID}分片下载失败 $retryTimes！！！！:${e.javaClass.name}"
                    )
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                    break
                }
            }
        } while (true)
        } finally {
            // 确保资源被释放
            try {
                randomAccessFile.close()
                ZLog.d(TAG, "RandomAccessFile 已关闭")
            } catch (e: Exception) {
                e.printStackTrace()
                ZLog.e(TAG, "关闭 RandomAccessFile 失败: ${e.message}")
            }
        }
    }

    // return 是否需要重试
    private fun startDownload(
        randomAccessFile: RandomAccessFile,
        rangeStart: Long,
        rangeEnd: Long,
    ): Boolean {
        ZLog.e(
            TAG,
            "分片下载 第${mDownloadPartInfo.downloadPartID}分片: Range开头: ${mDownloadPartInfo.partRangeStart}, 本次Range开头 : $rangeStart end: ${rangeEnd}"
        )
        val url = URL(mDownloadPartInfo.realDownloadURL)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            upateRequestInfo(mDownloadPartInfo.requestHeader)

            if (rangeEnd > 0 && rangeEnd > rangeStart) {
                setRequestProperty("Range", "bytes=$rangeStart-${rangeEnd}")
            }
            logRequestHeaderFields("分片下载数据 第${mDownloadPartInfo.downloadPartID}分片")
        }
        var time = System.currentTimeMillis()
        connection.connect()
        ZLog.w(
            TAG,
            "分片下载 第${mDownloadPartInfo.downloadPartID}分片: 请求用时: ${System.currentTimeMillis() - time} ~~~~~~~~~~~~~",
        )

        connection.logResponseHeaderFields("分片下载数据 第${mDownloadPartInfo.downloadPartID}分片")

        var serverContentLength = HTTPRequestUtils.getContentLength(connection)
        var localContentLength = mDownloadPartInfo.partLength - mDownloadPartInfo.partFinished
        ZLog.e(TAG, "~~~~~~~~~~~~~ 分片信息 第${mDownloadPartInfo.downloadPartID} 分片 ~~~~~~~~~~~~~")
        ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.downloadPartID}分片: getContentType:${connection.contentType}")
        ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.downloadPartID}分片: responseCode:${connection.responseCode}")
        ZLog.e(
            TAG,
            "分片下载 第${mDownloadPartInfo.downloadPartID}分片: contentLength: origin start ${mDownloadPartInfo.partRangeStart}, final start $rangeStart, end ${rangeEnd}, bytes=$rangeStart-$rangeEnd",
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
            // 分片下载
            if (abs(serverContentLength - localContentLength) > 1 && localContentLength > 0) {
                ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.downloadPartID}分片长度 错误 ！！！")
                if (mDownloadPartInfo.partFinished >= mDownloadPartInfo.partLength) {
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
                } else {
                    DownloadInfoDBManager.clearDownloadPartByID(mDownloadPartInfo.downloadID)
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                }
                return false
            } else {
                if (serverContentLength < 1L && rangeEnd > rangeStart) {
                    ZLog.e(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
                    ZLog.e(TAG, "分片下载 第${mDownloadPartInfo.downloadPartID}分片长度为0 ！！！ $retryTimes")
                    ZLog.e(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                    return false
                } else {
                    // go on
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

                if (mDownloadPartInfo.partFinished + len > mDownloadPartInfo.partLength) {
                    ZLog.e(
                        TAG,
                        "分片下载数据 第${mDownloadPartInfo.downloadPartID}分片累积下载超长！！！分片长度：${mDownloadPartInfo.partLength}, 累积下载长度：${mDownloadPartInfo.partFinished + len}",
                    )
                    ZLog.e(
                        TAG,
                        "分片下载数据 第${mDownloadPartInfo.downloadPartID}分片累积下载超长！！！$mDownloadPartInfo",
                    )
                    // 继续下载，长度不增加
                } else {
                    // 10秒（弱网）或 10M(高速网络)保存策略
                    if (System.currentTimeMillis() - lastUpdateTime > DOWNLOAD_SAVE_TIMER || hasDownloadLength - lastUpdateLength > DOWNLOAD_SAVE_SIZE) {
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
            ZLog.e(TAG, "\n")
            ZLog.e(TAG, "~~~~~~~~~~~~~~~~~~ 分片下载结束 ~~~~~~~~~~~~~~~~~~")
            ZLog.e(
                TAG,
                "分片下载结束 第${mDownloadPartInfo.downloadPartID}分片：分片长度：${mDownloadPartInfo.partLength}, 本次本地计算要长度:${rangeEnd - rangeStart + 1} ;本次服务器下发长度: $serverContentLength",
            )
            ZLog.e(
                TAG,
                "分片下载结束 第${mDownloadPartInfo.downloadPartID}分片：分片长度：${mDownloadPartInfo.partLength}, 本次下载长度:$hasDownloadLength ;累积下载长度: ${mDownloadPartInfo.partFinished}",
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
                        "分片下载数据 第${mDownloadPartInfo.downloadPartID}分片下载数据修正: 本次服务器实际返回并下载：$hasDownloadLength 本次计划下载大小：$localContentLength",
                    )
                    mDownloadPartInfo.partFinished = mDownloadPartInfo.partLength
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
                } else {
                    ZLog.e(
                        TAG,
                        "分片下载数据 第${mDownloadPartInfo.downloadPartID}分片下载异常: 本次计划下载：$localContentLength 服务器下发长度: $serverContentLength 服务器实际返回：$hasDownloadLength ",
                    )
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                }
            } else if (serverContentLength < 1L && hasDownloadLength > 0) {
                // content-length = 0 ， 但是有数据
                ZLog.e(
                    TAG,
                    "分片下载数据 第${mDownloadPartInfo.downloadPartID}分片下载数据修正: 本次实际下载：$hasDownloadLength 服务器下发长度: $serverContentLength  本次计划下载大小：$localContentLength",
                )
                mDownloadPartInfo.partFinished = mDownloadPartInfo.partLength
                mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
            } else {
                ZLog.e(
                    TAG,
                    "分片下载数据 第${mDownloadPartInfo.downloadPartID}分片下载异常: 本次计划下载：$localContentLength 服务器下发长度: $serverContentLength 服务器实际返回：$hasDownloadLength ",
                )
                mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
            }
            // 数据修正后存储
            DownloadInfoDBManager.updateDownloadFinished(
                mDownloadPartInfo.downloadPartID,
                mDownloadPartInfo.partFinished
            )
            ZLog.e(TAG, "分片下载数据 第${mDownloadPartInfo.downloadPartID}分片下载结束: $mDownloadPartInfo")
            ZLog.e(TAG, "\n")
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
