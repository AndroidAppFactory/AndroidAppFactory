package com.bihe0832.android.lib.download.core.part

import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadItem.TAG
import com.bihe0832.android.lib.download.DownloadPartInfo
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.core.addDownloadHeaders
import com.bihe0832.android.lib.download.core.dabase.DownloadInfoDBManager
import com.bihe0832.android.lib.download.core.getContentLength
import com.bihe0832.android.lib.download.core.logRequestHeaderFields
import com.bihe0832.android.lib.download.core.logResponseHeaderFields
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.okhttp.wrapper.OkHttpClientManager
import okhttp3.Protocol
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
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
            ZLog.e(
                TAG,
                "分片下载开始 第${mDownloadPartInfo.downloadPartID}分片: 分片此前已经下载结束"
            )
            mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
            return
        }

        val file = File(mDownloadPartInfo.finalFileName)
        
        // 冷启动回退：只在进入 run() 时执行一次，防止上次下载中断时最后一个缓冲区写入不完整
        val coldStartFinished = when {
            mDownloadPartInfo.partFinished > DOWNLOAD_BUFFER_SIZE -> {
                ZLog.w(
                    TAG,
                    "分片下载开始 冷启动回退 第${mDownloadPartInfo.downloadPartID}分片: ${mDownloadPartInfo.partFinished} -> ${mDownloadPartInfo.partFinished - DOWNLOAD_BUFFER_SIZE}"
                )
                mDownloadPartInfo.partFinished - DOWNLOAD_BUFFER_SIZE
            }
            else -> {
                0
            }
        }
        
        // 冷启动时回退进度并保存
        mDownloadPartInfo.partFinished = coldStartFinished
        DownloadInfoDBManager.saveDownloadPartInfo(mDownloadPartInfo)
        
        val availableSpace = FileUtils.getDirectoryAvailableSpace(file.parentFile.absolutePath)
        val needSpace = mDownloadPartInfo.partLength - mDownloadPartInfo.partFinished
        if (needSpace > 0 && needSpace > availableSpace) {
            mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
            ZLog.e(
                TAG,
                "分片下载开始 第${mDownloadPartInfo.downloadPartID}分片失败 下载异常 $retryTimes！！！！存储空间不足, availableSpace: $availableSpace, need: $needSpace ",
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
                // 热重试：直接从当前 partFinished 继续，不再回退
                // partFinished 在写入循环中实时更新，是准确的
                val currentFinished = mDownloadPartInfo.partFinished
                mDownloadPartInfo.partFinishedBefore = currentFinished
                
                ZLog.e(
                    TAG,
                    "分片下载开始 第${mDownloadPartInfo.downloadPartID}分片: 分片长度：${mDownloadPartInfo.partLength}, 当前进度：$currentFinished, 重试次数：$retryTimes",
                )

                try {
                    val localStart = mDownloadPartInfo.partLocalStart + currentFinished
                    val rangeStart = mDownloadPartInfo.partRangeStart + currentFinished
                    ZLog.e(
                        TAG,
                        "分片下载开始 第${mDownloadPartInfo.downloadPartID}分片 开始: 本次Range开头：$rangeStart, 本次Local开头：$localStart, 本次Range结尾: ${mDownloadPartInfo.partRangeEnd}",
                    )
                    randomAccessFile.seek(localStart)
                } catch (e: Exception) {
                    e.printStackTrace()
                    ZLog.e(
                        TAG,
                        "分片下载开始 第${mDownloadPartInfo.downloadPartID}分片 文件移动异常 :${e.javaClass.name}"
                    )
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                    break
                }

                try {
                    val rangeStart = mDownloadPartInfo.partRangeStart + currentFinished
                    if (!startDownload(
                            randomAccessFile,
                            rangeStart,
                            mDownloadPartInfo.partRangeEnd
                        )
                    ) {
                        break
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    ZLog.e(
                        TAG,
                        "分片下载 第${mDownloadPartInfo.downloadPartID}分片下载异常 $retryTimes！！！！:${e.javaClass.name}"
                    )
                    // 保存当前进度（partFinished 已经是最新值）
                    DownloadInfoDBManager.updateDownloadFinished(
                        mDownloadPartInfo.downloadPartID,
                        mDownloadPartInfo.partFinished,
                    )
                    sleep(3)
                    if (retryTimes < DOWNLOAD_RETRY_TIMES) {
                        retryTimes++
                        // 继续循环，从 partFinished 当前值继续，不回退
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

        // 使用 OkHttp 替代 HttpURLConnection，支持 HTTP/2 多路复用
        val requestBuilder = Request.Builder()
            .url(mDownloadPartInfo.realDownloadURL)
            .addDownloadHeaders(mDownloadPartInfo.requestHeader)
            .get()

        // 设置 Range 请求头
        if (rangeEnd > 0 && rangeEnd > rangeStart) {
            requestBuilder.addHeader("Range", "bytes=$rangeStart-${rangeEnd}")
        }
        val request = requestBuilder.build()
        val time = System.currentTimeMillis()

        // 使用传递过来的协议信息选择客户端
        val preferHttp2 = mDownloadPartInfo.isHttp2
        val response = OkHttpClientManager.executeRequest(request, preferHttp2)

        // 如果实际协议与预期不同，更新缓存（保守策略）
        if (response.protocol != mDownloadPartInfo.protocol) {
            OkHttpClientManager.recordProtocolForUrl(
                mDownloadPartInfo.realDownloadURL,
                response.protocol
            )
            ZLog.w(
                TAG, "分片 ${mDownloadPartInfo.downloadPartID} 协议变化: " +
                        "${mDownloadPartInfo.protocol} -> ${response.protocol}"
            )
        }

        ZLog.w(
            TAG,
            "分片下载 第${mDownloadPartInfo.downloadPartID}分片: 请求用时: ${System.currentTimeMillis() - time}, 协议: ${response.protocol} ~~~~~~~~~~~~~",
        )

        // 从 header 中读取 Content-Length，使用统一的扩展方法
        val serverContentLength = response.getContentLength()
        val localContentLength = mDownloadPartInfo.partLength - mDownloadPartInfo.partFinished
        ZLog.e(
            TAG,
            "~~~~~~~~~~~~~ 分片信息 第${mDownloadPartInfo.downloadPartID} 分片 ~~~~~~~~~~~~~"
        )
        ZLog.e(
            TAG,
            "分片下载 第${mDownloadPartInfo.downloadPartID}分片: getContentType:${response.body?.contentType()}"
        )
        ZLog.e(
            TAG,
            "分片下载 第${mDownloadPartInfo.downloadPartID}分片: responseCode:${response.code}"
        )
        ZLog.e(
            TAG,
            "分片下载 第${mDownloadPartInfo.downloadPartID}分片: protocol:${response.protocol}"
        )
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

        // 打印完整的响应头信息
        response.logResponseHeaderFields("分片下载数据 第${mDownloadPartInfo.downloadPartID}分片")

        if (response.code == HttpURLConnection.HTTP_OK || response.code == HttpURLConnection.HTTP_PARTIAL || response.code == 416) {
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
                    ZLog.e(
                        TAG,
                        "分片下载 第${mDownloadPartInfo.downloadPartID}分片长度为0 ！！！ $retryTimes"
                    )
                    ZLog.e(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
                    mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                    return false
                } else {
                    // go on
                }
            }

            val inputStream = response.body?.byteStream() ?: run {
                ZLog.e(
                    TAG,
                    "分片下载 第${mDownloadPartInfo.downloadPartID}分片: Response body 为空"
                )
                mDownloadPartInfo.partStatus = DownloadStatus.STATUS_DOWNLOAD_FAILED
                response.close()
                return false
            }

            val data = ByteArray(DOWNLOAD_BUFFER_SIZE)
            var len = -1
            var hasDownloadLength = 0L
            var lastUpdateTime = 0L
            var lastUpdateLength = 0L

            try {
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

                    if (mDownloadPartInfo.partLength > 0 && mDownloadPartInfo.partFinished + len > mDownloadPartInfo.partLength) {
                        ZLog.e(
                            TAG,
                            "分片下载数据 第${mDownloadPartInfo.downloadPartID}分片累积下载超长！！！分片长度：${mDownloadPartInfo.partLength}, 累积下载长度：${mDownloadPartInfo.partFinished + len}",
                        )
                        ZLog.e(
                            TAG,
                            "分片下载数据 第${mDownloadPartInfo.downloadPartID}分片累积下载超长！！！$mDownloadPartInfo",
                        )
                        // 继续下载，长度不增加
                    } else if (mDownloadPartInfo.partLength > 0) {
                        // partLength > 0 时支持断点续传，保存进度
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
                    } else {
                        // partLength = 0 时不支持断点续传，仅更新内存进度，不保存到DB
                        mDownloadPartInfo.partFinished = mDownloadPartInfo.partFinished + len
                    }
                    if (retryTimes > 0) {
                        ZLog.e(
                            TAG,
                            "分片下载 第${mDownloadPartInfo.downloadPartID}分片重试次数将被重置"
                        )
                        retryTimes = 0
                    }
                }
            } finally {
                // 确保资源被释放
                try {
                    inputStream.close()
                } catch (e: Exception) {
                    ZLog.e(TAG, "关闭 InputStream 失败: ${e.message}")
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
            ZLog.e(
                TAG,
                "分片下载数据 第${mDownloadPartInfo.downloadPartID}分片下载结束: $mDownloadPartInfo"
            )
            ZLog.e(TAG, "\n")

            // 关闭 Response
            response.close()
        } else {
            return true
        }
        return false
    }
}
