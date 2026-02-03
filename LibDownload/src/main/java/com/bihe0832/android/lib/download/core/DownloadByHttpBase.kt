package com.bihe0832.android.lib.download.core

import android.annotation.SuppressLint
import com.bihe0832.android.lib.download.DownloadClientConfig
import com.bihe0832.android.lib.download.DownloadErrorCode
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadItem.TAG
import com.bihe0832.android.lib.download.DownloadPartInfo
import com.bihe0832.android.lib.download.DownloadPauseType
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.core.dabase.DownloadInfoDBManager
import com.bihe0832.android.lib.download.core.dabase.DownloadPartInfoTableModel
import com.bihe0832.android.lib.download.core.part.DOWNLOAD_PART_SIZE
import com.bihe0832.android.lib.download.core.part.DownloadThread
import com.bihe0832.android.lib.okhttp.wrapper.OkHttpClientManager
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import okhttp3.Protocol
import java.io.File

/**
 * 下载引擎基类
 *
 * 提供 HTTP 下载的核心功能：
 * - 分片下载管理
 * - 下载进度监控
 * - 断点续传支持
 * - 并发下载控制
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-10.
 * Description: 下载引擎的具体实现
 */
abstract class DownloadByHttpBase(
    private var maxNum: Int,
    protected val isDebug: Boolean = false,
    protected val downloadClientConfig: DownloadClientConfig = DownloadClientConfig.createDefault()
) {

    companion object {
        /** 单个下载任务的最大分片数 */
        private const val MAX_DOWNLOAD_THREAD = 5

        /** 所有下载任务的最大总线程数 */
        private const val MAX_DOWNLOAD_TOTAL_THREAD = 30
    }

    private var hasStart = false

    abstract fun notifyProcess(downloadItem: DownloadItem)

    abstract fun notifyDownloadAfterFinish(downloadItem: DownloadItem)

    abstract fun notifyWait(info: DownloadItem)

    abstract fun notifyStart(info: DownloadItem)

    abstract fun onFail(item: DownloadItem, errorCode: Int, msg: String)
    
    abstract fun onPause(item: DownloadItem, @DownloadPauseType pauseType: Int)

    fun startDownload(
        info: DownloadItem, downloadType: Int, rangeStart: Long, rangeLength: Long, localStart: Long,
    ) {
        if (DownloadingList.isDownloading(info)) {
            ZLog.d(TAG, "download has start")
            DownloadingList.addToDownloadingList(info)
            return
        }
        try {
            ZLog.e(TAG, "开始下载 updateDownItemByServerInfo 后:${info}")
            //强制下载的数量也要控制
            if (DownloadingList.getDownloadingNum() < maxNum || DownloadingPartList.getDownloadingPartNum() < MAX_DOWNLOAD_TOTAL_THREAD || DownloadItem.MAX_DOWNLOAD_PRIORITY == info.downloadPriority) {
                if (DownloadingList.getDownloadingNum() < maxNum || info.downloadPriority >= DownloadItem.FORCE_DOWNLOAD_PRIORITY) {
                    ZLog.d(TAG, "getDownloadList() is good")
                    addToDownloadList(info)
                    notifyStart(info)
                    goDownload(info, downloadType, rangeStart, rangeLength, localStart)
                } else {
                    notifyWait(info)
                }
            } else {
                notifyWait(info)
            }
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


    @SuppressLint("Range")
    protected fun goDownload(
        info: DownloadItem, downloadType: Int, rangeStart: Long, rangeLength: Long, localStart: Long,
    ) {
        ZLog.e(TAG, "\n")
        ZLog.e(TAG, "~~~~~~~~~~~~~~~~~~ goDownload 最终入参 ~~~~~~~~~~~~~~~~~~")
        ZLog.e(TAG, "goDownload downloadID:${info.downloadID}")
        ZLog.e(TAG, "goDownload downloadType:${downloadType}")
        ZLog.e(TAG, "goDownload rangeStart:${rangeStart}")
        ZLog.e(TAG, "goDownload rangeLength:${rangeLength} ${FileUtils.getFileLength(rangeLength)}")
        ZLog.e(TAG, "goDownload localStart:${localStart}")
        ZLog.e(TAG, "goDownload contentLength:${info.contentLength} ${FileUtils.getFileLength(info.contentLength)}")
        if (downloadType == DownloadItem.TYPE_RANGE && (rangeLength <= 0 || info.contentLength <= 0)) {
            ZLog.w(TAG, "goDownload ⚠️ Range下载但内容大小未知(rangeLength=$rangeLength, contentLength=${info.contentLength})，不支持断点续传，下载中断后需重新下载")
        }
        ZLog.e(TAG, "goDownload info:${info}")
        ZLog.e(TAG, "~~~~~~~~~~~~~~~~~~ goDownload 最终入参 ~~~~~~~~~~~~~~~~~~")
        ZLog.e(TAG, "\n")
        val file = File(info.filePath)
        val hasDownload = DownloadInfoDBManager.hasDownloadPartInfo(info.downloadID, isDebug)
         ZLog.e(TAG, "断点续传判断: filePath=${info.filePath}")
         ZLog.e(TAG, "断点续传判断: file.exists()=${file.exists()}, hasDownload=$hasDownload")
         ZLog.e(TAG, "断点续传判断: rangeLength=$rangeLength, file.length()=${if (file.exists()) file.length() else -1}")
         ZLog.e(TAG, "断点续传判断: 条件结果=${file.exists() && hasDownload && rangeLength > 0 && file.length() <= rangeLength}")
        if (file.exists() && hasDownload && rangeLength > 0 && file.length() <= rangeLength) {
             ZLog.e(TAG, "✅ 进入断点续传逻辑")
            
            // 断点续传：如果协议信息丢失，从缓存查询或使用保守策略
            if (info.protocol == Protocol.HTTP_1_1 && info.realURL.isNotEmpty()) {
                OkHttpClientManager.getProtocolFromCache(info.realURL)?.let {
                    info.protocol = it
                    ZLog.d(TAG, "断点续传：从缓存恢复协议信息 ${info.realURL}: $it")
                }
            }
            
            //断点续传逻辑
            ZLog.e(
                TAG,
                "分片下载数据 - ${info.downloadID} 历史下载计算前: 之前已完成${FileUtils.getFileLength(info.finishedLengthBefore)}，累积已完成: ${
                    FileUtils.getFileLength(info.finished)
                }"
            )
            info.finishedLengthBefore = DownloadInfoDBManager.getFinishedBefore(info.downloadID)
            if (info.finishedLengthBefore > info.finished) {
                info.finished = info.finishedLengthBefore
            }
            ZLog.e(
                TAG,
                "分片下载数据 - ${info.downloadID} 历史下载计算后: 之前已完成${FileUtils.getFileLength(info.finishedLengthBefore)}，累积已完成: ${
                    FileUtils.getFileLength(info.finished)
                }"
            )
            ZLog.e(
                TAG,
                "分片下载数据 - ${info.downloadID} : data length:${FileUtils.getFileLength(rangeLength)}, finished before: ${
                    FileUtils.getFileLength(info.finishedLengthBefore)
                }, need download ${FileUtils.getFileLength(rangeLength - info.finishedLengthBefore)}"
            )
            val cursor = DownloadInfoDBManager.getDownloadPartInfo(info.downloadID)
            ZLog.e(TAG, "分片下载数据 - ${info.downloadID} - 已有分片:${cursor.count}")
            try {
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    val id = cursor.getInt(cursor.getColumnIndex(DownloadPartInfoTableModel.col_part_id))
                    val rangeStartInDB =
                        cursor.getLong(cursor.getColumnIndex(DownloadPartInfoTableModel.col_range_start))
                    val localStartInDB =
                        cursor.getLong(cursor.getColumnIndex(DownloadPartInfoTableModel.col_local_start))
                    val length = cursor.getLong(cursor.getColumnIndex(DownloadPartInfoTableModel.col_length))
                    val finished = cursor.getLong(cursor.getColumnIndex(DownloadPartInfoTableModel.col_finished))
                    ZLog.e(
                        TAG,
                        "分片下载数据 - ${info.downloadID} - 继续已有分片:${info.downloadID - id}  rangeStart:$rangeStart rangeStartInDB:$rangeStartInDB localStartInDB:$localStart localStart:$localStartInDB length:$length finished:$finished"
                    )
                    startDownloadPart(info, id, rangeStartInDB, localStartInDB, length, finished)
                    cursor.moveToNext()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    cursor.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
             ZLog.e(TAG, "❌ 不走断点续传，原因分析:")
             ZLog.e(TAG, "  - file.exists()=${file.exists()}")
             ZLog.e(TAG, "  - hasDownload=$hasDownload")
             ZLog.e(TAG, "  - rangeLength=$rangeLength (需>0)")
            if (file.exists()) {
                 ZLog.e(TAG, "  - file.length()=${file.length()}, rangeLength=$rangeLength (需file.length()<=rangeLength)")
            }
            //有下载记录，无文件，删除后重新下载
            if (hasDownload) {
                 ZLog.e(TAG, "清除旧的下载记录，重新下载")
                DownloadInfoDBManager.clearDownloadPartByID(info.downloadID)
            }
            startNew(info, rangeStart, rangeLength, localStart)
        }
    }


    protected fun startNew(info: DownloadItem, rangeStart: Long, rangeLength: Long, localStart: Long) {
        ZLog.e(TAG, "\n")
        ZLog.e(TAG, "~~~~~~~~~~~~~~~~~~ startNew ~~~~~~~~~~~~~~~~~~")
        ZLog.e(TAG, "开启新下载: startNew: start：$rangeStart, rangeLength: $rangeLength,localStart:$localStart $info ")
        
        // 使用 DownloadItem 中已记录的协议版本，而不是重新检测
        // 这确保了分片策略与实际下载使用相同的协议判断
        val isHttp2 = downloadClientConfig.enableHttp2 && info.isHttp2
        
        val maxThreadNum = downloadClientConfig.getMaxChunks(isHttp2)
        val minChunkSize = downloadClientConfig.getMinChunkSize(isHttp2)
        
        if (downloadClientConfig.logProtocolInfo) {
            ZLog.e(TAG, "开启新下载: 协议类型: ${info.protocol}, 最大分片数: $maxThreadNum, 最小分片大小: ${FileUtils.getFileLength(minChunkSize)}")
        }
        
        var threadNum = 1
        if (rangeLength > DOWNLOAD_PART_SIZE) {
            // 先分大片
            threadNum = (rangeLength / DOWNLOAD_PART_SIZE).toInt().let {
                ZLog.e(
                    TAG,
                    "开启新下载: 内容长度: ${rangeLength}，默认分片大小：${DOWNLOAD_PART_SIZE}，按默认分片可分片：${it}"
                )
                when {
                    it > maxThreadNum * 2 -> {
                        maxThreadNum
                    }

                    it in 2..maxThreadNum * 2 -> {
                        maxThreadNum * 2 / maxNum
                    }

                    it < 1 -> {
                        1
                    }

                    else -> {
                        it / maxNum
                    }
                }
            }
            ZLog.e(TAG, "开启新下载: 内容长度: ${rangeLength}，二次分片数量：${threadNum}，并行下载量数量：${maxNum}")
            if (threadNum < 1) {
                threadNum = 1
            } else if (threadNum > maxThreadNum) {
                threadNum = maxThreadNum
            }
            ZLog.e(TAG, "开启新下载: 内容长度: ${rangeLength}，三次分片数量：${threadNum}，并行下载量数量：${maxNum}")
        } else {
            threadNum = 1
        }
        // 根据协议类型动态调整最小分片大小
        if (rangeLength / threadNum < minChunkSize) {
            threadNum = (rangeLength / minChunkSize).toInt()
        }
        if (threadNum < 1) {
            threadNum = 1
        }
        if (threadNum > maxThreadNum) {
            threadNum = maxThreadNum
        }
        val partSize = rangeLength / threadNum
        if ((rangeLength - partSize * (threadNum - 1)) < partSize / 5) {
            if (threadNum > 1) {
                threadNum -= 1
            }
        }
        ZLog.e(TAG, "开启新下载: 内容长度: ${rangeLength}，最终分片数量：${threadNum}")
        ZLog.e(TAG, "开启新下载: 最后一片长度: ${rangeLength - partSize * (threadNum - 1)}")
        if (threadNum > 1) {
            for (i in 0 until threadNum) {
                ZLog.d("开启新下载：开始第$i 片，共$threadNum 片")
                when (i) {
                    threadNum - 1 -> {
                        startDownloadPart(
                            info, i, rangeStart + i * partSize, localStart + i * partSize, rangeLength - i * partSize, 0
                        )
                    }

                    else -> {
                        startDownloadPart(info, i, rangeStart + i * partSize, localStart + i * partSize, partSize, 0)
                    }
                }
            }
        } else {
            ZLog.d("开启新下载：不分片")
            startDownloadPart(info, 0, rangeStart, localStart, rangeLength, 0)
        }
    }

    protected fun startDownloadPart(
        info: DownloadItem, partNo: Int, oldRangeStart: Long, oldLocalStart: Long, length: Long, finished: Long,
    ) {
        ZLog.e(TAG, "\n")
        ZLog.e(TAG, "~~~~~~~~~~~~~~~~~~ 分片下载数据 ${info.downloadID} - $partNo ~~~~~~~~~~~~~~~~~~")
        ZLog.e(
            TAG,
            "分片下载数据 ${info.downloadID} - $partNo rangeStart: $oldRangeStart, localStart:$oldLocalStart ,length :${length} ${
                FileUtils.getFileLength(length)
            },finished: $finished"
        )
        val downloadThreadForPart = DownloadThread(DownloadPartInfo().apply {
            this.downloadID = info.downloadID
            this.partID = partNo
            this.requestHeader = info.requestHeader
            this.realDownloadURL = info.realURL
            this.finalFileName = info.filePath
            this.partRangeStart = oldRangeStart
            this.partLocalStart = oldLocalStart
            this.partLength = length
            this.partFinished = finished
            this.partFinishedBefore = finished
            this.protocol = info.protocol  // 传递协议信息到分片
        }.also {
            ZLog.d(TAG, "分片下载数据 ${info.downloadID} - $partNo: 开始:$it")
        })
        DownloadingPartList.addDownloadingPart(downloadThreadForPart)
        DownloadInfoDBManager.saveDownloadPartInfo(downloadThreadForPart.getDownloadPartInfo())
        notifyProcess(info)

        try {
            downloadThreadForPart.start()
            ThreadManager.getInstance().start({ checkDownloadProcess() }, 1)
        } catch (e: Throwable) {
            e.printStackTrace()
            val errorCode = DownloadExceptionAnalyzer.analyzeException(e)
            notifyDownloadFailed(info, errorCode, "download part start with exception: ${e.javaClass.simpleName}: ${e.message}")
        }
    }


    private fun addToDownloadList(info: DownloadItem) {
        DownloadingList.addToDownloadingList(info)
    }


    private fun checkDownloadProcess() {
        if (hasStart) {
            ZLog.d(TAG, "checkDownloadProcess has start")
            return
        }
        hasStart = true
        Thread {
            while (DownloadingList.getDownloadingNum() > 0) {
                ZLog.d("checkDownloadProcess work")
                // 创建快照，避免并发修改异常
                val downloadingSnapshot = DownloadingList.getAllDownloadingItemList().toList()

                downloadingSnapshot.forEach { downloadItem ->
                    var notFinished = false
                    var hasFail = false
                    var hasRealUnrecoverableError = false  // 是否存在不可恢复的错误
                    var failedPartErrorCode = 0  // 记录失败分片的错误码（优先保留不可恢复的，默认不可恢复）
                    var errorInfo = ""
                    if (downloadItem.status == DownloadStatus.STATUS_DOWNLOADING) {
                        var newFinished = 0L
                        var finishedBefore = 0L
                        // 同样创建分片列表的快照
                        val partListSnapshot = DownloadingPartList.getPartListById(downloadItem.downloadID).toList()

                        partListSnapshot.forEach { downloadPartItem ->
                            ZLog.d(
                                TAG,
                                "第${downloadPartItem.getDownloadPartInfo().partID} " + "分片信息:${downloadPartItem.getDownloadPartInfo()}"
                            )
                            newFinished += downloadPartItem.getDownloadPartInfo().partFinished
                            finishedBefore += downloadPartItem.getDownloadPartInfo().partFinishedBefore

                            if (downloadPartItem.getDownloadPartInfo().partStatus != DownloadStatus.STATUS_DOWNLOAD_SUCCEED) {
                                notFinished = true
                            }

                            if (downloadPartItem.getDownloadPartInfo().partStatus == DownloadStatus.STATUS_DOWNLOAD_FAILED) {
                                hasFail = true
                                // 获取分片的具体错误码，用于判断是否可重试
                                val partErrorCode = downloadPartItem.getDownloadPartInfo().partErrorCode
                                val isUnrecoverable = !DownloadExceptionAnalyzer.isRecoverableError(partErrorCode)
                                
                                // 策略：优先保留不可恢复的错误码
                                // - 如果已经有不可恢复错误了，就不再更新（保留第一个不可恢复的）
                                // - 如果还没有不可恢复错误，就更新（无论当前是可恢复还是不可恢复）
                                if (!hasRealUnrecoverableError) {
                                    failedPartErrorCode = partErrorCode
                                    errorInfo = "download part exception: ${downloadPartItem.getDownloadPartInfo().downloadPartID}, errorCode: $partErrorCode"
                                    if (isUnrecoverable) {
                                        hasRealUnrecoverableError = true
                                    }
                                }
                            }
                        }
                        if (newFinished - finishedBefore < 1) {
                            downloadItem.lastSpeed = 0
                        } else {
                            downloadItem.lastSpeed = newFinished - downloadItem.finished
                        }
                        downloadItem.finished = newFinished
                        downloadItem.finishedLengthBefore = finishedBefore
                        if (isDebug) ZLog.d(
                            TAG,
                            "分片下载汇总 - ${downloadItem.downloadID}: 完成长度:${FileUtils.getFileLength(newFinished)} 之前下载长度:${
                                FileUtils.getFileLength(finishedBefore)
                            }"
                        )
                        if (isDebug) ZLog.d(
                            TAG, "分片下载汇总 - ${downloadItem.downloadID}: " + "文件长度 :${
                                FileUtils.getFileLength(downloadItem.contentLength)
                            }" + ";完成长度 :${FileUtils.getFileLength(downloadItem.finished)}" + ";之前下载长度 :${
                                FileUtils.getFileLength(
                                    downloadItem.finishedLengthBefore
                                )
                            }" + ";本次下载累计长度 :${FileUtils.getFileLength(newFinished - downloadItem.finishedLengthBefore)} ，新增长度: ${
                                FileUtils.getFileLength(
                                    downloadItem.lastSpeed
                                )
                            }"
                        )
                        if (downloadItem.contentLength > 0 && downloadItem.finished >= downloadItem.contentLength) {
                            downloadItem.finished = downloadItem.contentLength
                        }
                        notifyProcess(downloadItem)
                    }
                    if (!notFinished) {
                        notifyDownloadAfterFinish(downloadItem)
                    } else if (downloadItem.contentLength > 0 && downloadItem.finished == downloadItem.contentLength) {
                        notifyDownloadAfterFinish(downloadItem)
                    } else if (hasFail) {
                        // 根据分片错误码判断是否可重试
                        // 如果分片没有设置错误码（0），使用通用的分片异常码
                        val errorCode = if (failedPartErrorCode != 0) failedPartErrorCode else DownloadErrorCode.ERR_DOWNLOAD_PART_EXCEPTION
                        notifyDownloadFailed(downloadItem, errorCode, errorInfo)
                    }
                }

                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            hasStart = false
        }.start()
    }

    fun closeDownload(downloadID: Long, finishDownload: Boolean, clearDownloadHistory: Boolean) {
        ZLog.d(TAG, "closeDownload connectList:" + DownloadingPartList.getDownloadingPartNum())
        ZLog.d(TAG, "closeDownload downloadList:" + DownloadingList.getDownloadingNum())
        DownloadingPartList.removeItem(downloadID, finishDownload)
        if (clearDownloadHistory) {
            DownloadInfoDBManager.clearDownloadInfoByID(downloadID)
            DownloadInfoDBManager.clearDownloadPartByID(downloadID)
        }
        DownloadingList.removeFromDownloadingList(downloadID)
        ZLog.d(TAG, "closeDownload connectList:" + DownloadingPartList.getDownloadingPartNum())
        ZLog.d(TAG, "closeDownload downloadList:" + DownloadingList.getDownloadingNum())
    }

    fun deleteFile(downloadInfo: DownloadItem) {
        ThreadManager.getInstance().start {
            FileUtils.deleteFile(downloadInfo.filePath)
        }
    }

    fun notifyDownloadFailed(item: DownloadItem, errorCode: Int, msg: String) {
        ZLog.e(TAG, "notifyDownloadFailed errorCode $errorCode (${DownloadExceptionAnalyzer.getErrorDescription(errorCode)}), msg: $msg, item: $item")
        
        // 判断是否是可恢复的错误（使用内部细化错误码判断）
        if (isRecoverableError(errorCode)) {
            // 可恢复错误：检查重试轮数
            if (item.networkErrorRetryRound >= DownloadItem.MAX_NETWORK_ERROR_RETRY_ROUND) {
                // 超过最大重试轮数，标记为失败
                ZLog.e(TAG, "Max retry round exceeded (${item.networkErrorRetryRound}/${DownloadItem.MAX_NETWORK_ERROR_RETRY_ROUND}), mark as failed")
                closeDownload(item.downloadID, finishDownload = true, clearDownloadHistory = true)
                if (item.status != DownloadStatus.STATUS_DOWNLOAD_PAUSED) {
                    onFail(item, DownloadErrorCode.ERR_MAX_RETRY_EXCEEDED, "超过最大重试轮数: $msg")
                }
            } else {
                // 增加重试轮数，暂停任务等待网络恢复
                item.incrementNetworkErrorRetryRound()
                ZLog.e(TAG, "Recoverable error, pause for retry (round ${item.networkErrorRetryRound}/${DownloadItem.MAX_NETWORK_ERROR_RETRY_ROUND}), pauseType=PAUSED_BY_NETWORK_ERROR(5)")
                
                // 保存当前进度（不清除历史）
                closeDownload(item.downloadID, finishDownload = false, clearDownloadHistory = false)
                
                // 设置暂停状态并回调
                item.setPause(DownloadPauseType.PAUSED_BY_NETWORK_ERROR)
                ZLog.e(TAG, "任务已暂停: status=${item.status}, pauseType=${item.pauseType}, downloadID=${item.downloadID}")
                onPause(item, DownloadPauseType.PAUSED_BY_NETWORK_ERROR)
            }
        } else {
            // 不可恢复错误：直接失败
            closeDownload(item.downloadID, finishDownload = true, clearDownloadHistory = true)
            if (item.status != DownloadStatus.STATUS_DOWNLOAD_PAUSED) {
                // 对外回调时，将内部细化错误码收敛为旧版本错误码
                val externalErrorCode = DownloadExceptionAnalyzer.toExternalErrorCode(errorCode)
                onFail(item, externalErrorCode, msg)
            }
        }
        ZLog.d(TAG, "cancelDownload connectList:" + DownloadingPartList.getDownloadingPartNum())
    }
    
    /**
     * 判断是否是可恢复的错误（网络相关错误）
     * 可恢复错误会暂停任务，等待网络恢复后自动重试
     * 
     * 委托给 DownloadExceptionAnalyzer 进行精确判断
     */
    private fun isRecoverableError(errorCode: Int): Boolean {
        return DownloadExceptionAnalyzer.isRecoverableError(errorCode)
    }

}