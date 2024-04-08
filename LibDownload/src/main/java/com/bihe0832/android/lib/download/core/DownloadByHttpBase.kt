package com.bihe0832.android.lib.download.core

import android.annotation.SuppressLint
import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadErrorCode
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_DOWNLOAD_PART_START_EXCEPTION
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadItem.TAG
import com.bihe0832.android.lib.download.DownloadPartInfo
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.core.dabase.DownloadInfoDBManager
import com.bihe0832.android.lib.download.core.dabase.DownloadPartInfoTableModel
import com.bihe0832.android.lib.download.core.part.DOWNLOAD_MIN_SIZE
import com.bihe0832.android.lib.download.core.part.DOWNLOAD_PART_SIZE
import com.bihe0832.android.lib.download.core.part.DownloadThread
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.HTTPRequestUtils
import com.bihe0832.android.lib.thread.ThreadManager
import java.io.File
import java.net.HttpURLConnection
import java.net.URL


/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-10.
 * Description: 下载引擎的具体实现
 *
 */


abstract class DownloadByHttpBase(private var maxNum: Int, protected val isDebug: Boolean = false) {

    private val MAX_DOWNLOAD_THREAD = 5
    private val MAX_DOWNLOAD_TOTAL_THREAD = 30
    private val MAX_RETRY_TIMES = 2

    private var hasStart = false

    abstract fun notifyProcess(downloadItem: DownloadItem)

    abstract fun notifyDownloadAfterFinish(downloadItem: DownloadItem)

    abstract fun notifyWait(info: DownloadItem)

    abstract fun notifyStart(info: DownloadItem)

    abstract fun onFail(item: DownloadItem, errorCode: Int, msg: String)

    fun startDownload(info: DownloadItem, downloadType: Int, rangeStart: Long, rangeLength: Long, localStart: Long) {
        if (DownloadingList.isDownloading(info)) {
            ZLog.d(TAG, "download has start")
            DownloadingList.addToDownloadingList(info)
            return
        }

        if (updateDownItemByServerInfo(info, downloadType, rangeStart, rangeLength)) {
            try {
                ZLog.e(TAG, "开始下载 updateDownItemByServerInfo 后:${info}")
                //强制下载的数量也要控制
                if (DownloadingList.getDownloadingNum() < maxNum || DownloadingPartList.getDownloadingPartNum() < MAX_DOWNLOAD_TOTAL_THREAD || DownloadItem.MAX_DOWNLOAD_PRIORITY == info.downloadPriority) {
                    if (DownloadingList.getDownloadingNum() < maxNum || info.downloadPriority >= DownloadItem.FORCE_DOWNLOAD_PRIORITY) {
                        ZLog.d(TAG, "getDownloadList() is good")
                        addToDownloadList(info)
                        notifyStart(info)
                        if (downloadType == DownloadPartInfo.TYPE_FILE) {
                            goDownload(info, downloadType, 0, info.contentLength, localStart)
                        } else {
                            info.contentLength = rangeLength
                            goDownload(info, downloadType, rangeStart, rangeLength, localStart)
                        }
                    } else {
                        notifyWait(info)
                    }
                } else {
                    notifyWait(info)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                if (info.status != DownloadStatus.STATUS_DOWNLOAD_PAUSED) {
                    notifyDownloadFailed(
                        info, DownloadErrorCode.ERR_DOWNLOAD_EXCEPTION, "download with exception$e"
                    )
                }
            }

        } else {
            ZLog.d(TAG, "has notify in updateDownItemByServerInfo")
        }

    }

    @SuppressLint("Range")
    protected fun goDownload(
        info: DownloadItem, downloadType: Int, rangeStart: Long, rangeLength: Long, localStart: Long
    ) {
        ZLog.e(TAG, "\n")
        ZLog.e(TAG, "~~~~~~~~~~~~~~~~~~ goDownload 最终入参 ~~~~~~~~~~~~~~~~~~")
        ZLog.e(TAG, "goDownload downloadID:${info.downloadID}")
        ZLog.e(TAG, "goDownload downloadType:${downloadType}")
        ZLog.e(TAG, "goDownload rangeStart:${rangeStart}")
        ZLog.e(TAG, "goDownload rangeLength:${rangeLength} ${FileUtils.getFileLength(rangeLength)}")
        ZLog.e(TAG, "goDownload localStart:${localStart}")
        ZLog.e(TAG, "goDownload contentLength:${info.contentLength} ${FileUtils.getFileLength(info.contentLength)}")
        ZLog.e(TAG, "goDownload info:${info}")


        val file = File(info.filePath)
        var hasDownload = DownloadInfoDBManager.hasDownloadPartInfo(info.downloadID, isDebug)
        if (file.exists() && hasDownload && rangeLength > 0 && file.length() <= rangeLength) {
            ZLog.e(TAG, "断点续传逻辑:$info")
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
            var cursor = DownloadInfoDBManager.getDownloadPartInfo(info.downloadID)
            ZLog.e(TAG, "分片下载数据 - ${info.downloadID} - 已有分片:${cursor.count}")
            try {
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    val id = cursor.getInt(cursor.getColumnIndex(DownloadPartInfoTableModel.col_part_id))
                    val rangeStart = cursor.getLong(cursor.getColumnIndex(DownloadPartInfoTableModel.col_range_start))
                    val localStart = cursor.getLong(cursor.getColumnIndex(DownloadPartInfoTableModel.col_local_start))
                    val length = cursor.getLong(cursor.getColumnIndex(DownloadPartInfoTableModel.col_length))
                    val finished = cursor.getLong(cursor.getColumnIndex(DownloadPartInfoTableModel.col_finished))
                    ZLog.e(
                        TAG,
                        "分片下载数据 - ${info.downloadID} - 继续已有分片:${info.downloadID - id} rangeStart:$rangeStart localStart:$localStart length:$length finished:$finished"
                    )
                    startDownloadPart(info, id, rangeStart, localStart, length, finished)
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
            //有下载记录，无文件，删除后重新下载
            if (hasDownload) {
                DownloadInfoDBManager.clearDownloadPartByID(info.downloadID)
            }
            startNew(info, rangeStart, rangeLength, localStart)
        }
    }


    protected fun startNew(info: DownloadItem, rangeStart: Long, rangeLength: Long, localStart: Long) {
        ZLog.e(TAG, "\n")
        ZLog.e(TAG, "~~~~~~~~~~~~~~~~~~ startNew ~~~~~~~~~~~~~~~~~~")
        ZLog.e(TAG, "开启新下载: startNew: start：$rangeStart, rangeLength: $rangeLength,localStart:$localStart $info ")
        var threadNum = 1
        if (rangeLength > DOWNLOAD_PART_SIZE) {
            // 先分大片
            threadNum = (rangeLength / DOWNLOAD_PART_SIZE).toInt().let {
                ZLog.e(
                    TAG,
                    "开启新下载: 内容长度: ${rangeLength}，默认分片大小：${DOWNLOAD_PART_SIZE}，按默认分片可分片：${it}"
                )
                when {
                    it > MAX_DOWNLOAD_THREAD * 2 -> {
                        MAX_DOWNLOAD_THREAD
                    }

                    it in 2..MAX_DOWNLOAD_THREAD * 2 -> {
                        MAX_DOWNLOAD_THREAD * 2 / maxNum
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
            } else if (threadNum > MAX_DOWNLOAD_THREAD) {
                threadNum = MAX_DOWNLOAD_THREAD
            }
            ZLog.e(TAG, "开启新下载: 内容长度: ${rangeLength}，三次分片数量：${threadNum}，并行下载量数量：${maxNum}")
        } else {
            threadNum = 1
        }
        //太小的文件分小片
        if (rangeLength / threadNum < DOWNLOAD_MIN_SIZE) {
            threadNum = (rangeLength / DOWNLOAD_MIN_SIZE).toInt()
        }
        if (threadNum < 1) {
            threadNum = 1
        }
        if (threadNum > MAX_DOWNLOAD_THREAD){
            threadNum = MAX_DOWNLOAD_THREAD
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
        info: DownloadItem, partNo: Int, oldRangeStart: Long, oldLocalStart: Long, length: Long, finished: Long
    ) {
        ZLog.e(TAG, "\n")
        ZLog.e(TAG, "~~~~~~~~~~~~~~~~~~ 分片下载数据 ~~~~~~~~~~~~~~~~~~")
        ZLog.e(
            TAG,
            "分片下载数据 第${partNo}分片 rangeStart: $oldRangeStart, localStart:$oldLocalStart ,length :${length} ${
                FileUtils.getFileLength(length)
            },finished: $finished"
        )
        val downloadThreadForPart = DownloadThread(DownloadPartInfo().apply {
            this.downloadID = info.downloadID
            this.partID = partNo
            this.realDownloadURL = info.realURL
            this.finalFileName = info.filePath
            this.partRangeStart = oldRangeStart
            this.partLocalStart = oldLocalStart
            this.partLength = length
            this.partFinished = finished
            this.partFinishedBefore = finished
        }.also {
            ZLog.d(TAG, "分片下载数据 - ${info.downloadID}: 开始第$partNo 段开始:$it")
        })
        DownloadingPartList.addDownloadingPart(downloadThreadForPart)
        DownloadInfoDBManager.saveDownloadPartInfo(downloadThreadForPart.getDownloadPartInfo())
        notifyProcess(info)

        try {
            downloadThreadForPart.start()
            ThreadManager.getInstance().start({ checkDownloadProcess() }, 1)
        } catch (e: Throwable) {
            e.printStackTrace()
            notifyDownloadFailed(info, ERR_DOWNLOAD_PART_START_EXCEPTION, "download with exception:$e")
        }
    }

    private fun updateDownItemByServerInfo(
        info: DownloadItem, downloadType: Int, rangeStart: Long, rangeLength: Long
    ): Boolean {
        ZLog.d(TAG, "updateDownItemByServerInfo:$info")
        // 重新启动，获取文件总长度
        var times = 0
        var realURL = if (TextUtils.isEmpty(info.realURL)) {
            HTTPRequestUtils.getRedirectUrl(info.downloadURL)
        } else {
            info.realURL
        }
        do {
            times++
            try {
                ZLog.e(TAG, "获取文件长度 $times:$realURL")
                val url = URL(realURL)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    upateRequestInfo()
                }
                val time = System.currentTimeMillis()
                connection.connect()
                ZLog.e(TAG, "获取文件长度，请求用时: ${System.currentTimeMillis() - time} ~~~~~~~~~~~~~")
                if (isDebug) {
                    connection.logHeaderFields("获取文件长度")
                }
                val contentLength = HTTPRequestUtils.getContentLength(connection)
                ZLog.e(TAG, "获取文件长度 getContentType:${connection.contentType}")
                ZLog.e(TAG, "获取文件长度 getContentLength:${contentLength}")
                ZLog.e(TAG, "计划下载的信息 rangeStart:${rangeStart}, rangeLength:${rangeLength}")
                ZLog.e(TAG, "获取文件长度 responseCode:${connection.responseCode}")
                if (connection.responseCode == HttpURLConnection.HTTP_OK || connection.responseCode == HttpURLConnection.HTTP_PARTIAL) {
                    info.realURL = realURL
                    if (contentLength > 0) {
                        if (DownloadPartInfo.TYPE_FILE == downloadType) {
                            info.contentLength = contentLength
                        }
                        if (rangeLength > 0 && contentLength < rangeStart + rangeLength) {
                            //请求长度小于服务器获取的长度
                            notifyDownloadFailed(
                                info, DownloadErrorCode.ERR_RANGE_BAD_SERVER_LENGTH, "download length is less than need"
                            )
                            return false
                        } else {
                            ZLog.d(TAG, "获取文件长度 保存信息:${info}")
                            DownloadInfoDBManager.saveDownloadInfo(info)
                            return true
                        }
                    } else {
                        ZLog.e(TAG, "获取文件长度 长度为0: $times ${info.downloadID}")
                        if (times > MAX_RETRY_TIMES) {
                            if (rangeStart > 0) {
                                // 区间下载，但是源数据不支持分片，返回失败
                                notifyDownloadFailed(
                                    info,
                                    DownloadErrorCode.ERR_RANGE_NOT_SUPPORT,
                                    "download maybe not support range download"
                                )
                                return false
                            } else {
                                if (DownloadPartInfo.TYPE_FILE == downloadType) {
                                    info.contentLength = contentLength
                                }
                                return true
                            }
                        } else {
                            realURL = HTTPRequestUtils.getRedirectUrl(info.downloadURL)
                        }
                    }
                } else {
                    if (times > MAX_RETRY_TIMES) {
                        ZLog.e(
                            TAG, "download with error file length after max times:${connection.responseCode} " + info
                        )
                        //请求三次都失败在结束
                        notifyDownloadFailed(
                            info,
                            DownloadErrorCode.ERR_HTTP_LENGTH_FAILED,
                            "download with error file length after max times"
                        )
                        return false
                    } else {
                        ZLog.e(TAG, "download with error file length :${connection.responseCode} " + info)
                        realURL = HTTPRequestUtils.getRedirectUrl(info.downloadURL)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ZLog.e(TAG, "获取文件长度 异常: download with exception: $times ${e.javaClass.name}")
                if (times > MAX_RETRY_TIMES) {
                    //累积请求三次都失败在结束
                    notifyDownloadFailed(
                        info,
                        DownloadErrorCode.ERR_HTTP_EXCEPTION,
                        "download with exception after three times:${e.javaClass.name}"
                    )
                    ZLog.e(TAG, "获取文件长度 异常: download with exception after three times:${e.javaClass.name}")
                    return false
                }
            }
        } while (true)
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
                DownloadingList.getDownloadingItemList().forEach { downloadItem ->
                    var notFinished = false
                    var hasFail = false
                    var errorInfo = ""
                    if (downloadItem.status == DownloadStatus.STATUS_DOWNLOADING) {
                        var newFinished = 0L
                        var finishedBefore = 0L
                        DownloadingPartList.getPartListById(downloadItem.downloadID).forEach { downloadPartItem ->
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
                                errorInfo =
                                    "download part exception: ${downloadPartItem.getDownloadPartInfo().downloadPartID}"
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
                        notifyDownloadFailed(downloadItem, DownloadErrorCode.ERR_DOWNLOAD_PART_EXCEPTION, errorInfo)
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

    fun closeDownload(downloadID: Long, isFinished: Boolean, clearHistory: Boolean) {
        ZLog.d(TAG, "cancelDownload connectList:" + DownloadingPartList.getDownloadingPartNum())
        ZLog.d(TAG, "cancelDownload downloadList:" + DownloadingList.getDownloadingNum())
        DownloadingPartList.removeItem(downloadID, isFinished)
        if (clearHistory) {
            DownloadInfoDBManager.clearDownloadInfoByID(downloadID)
            DownloadInfoDBManager.clearDownloadPartByID(downloadID)
        }
        DownloadingList.removeFromDownloadingList(downloadID)
        ZLog.d(TAG, "cancelDownload connectList:" + DownloadingPartList.getDownloadingPartNum())
        ZLog.d(TAG, "cancelDownload downloadList:" + DownloadingList.getDownloadingNum())
    }

    fun deleteFile(downloadInfo: DownloadItem) {
        ThreadManager.getInstance().start {
            FileUtils.deleteFile(downloadInfo.filePath)
        }
    }

    fun notifyDownloadFailed(item: DownloadItem, errorCode: Int, msg: String) {
        ZLog.e(TAG, "notifyDownloadFailed errorCode $errorCode, msg: $msg, item: $item")
        closeDownload(item.downloadID, isFinished = false, clearHistory = true)
        if (item.status != DownloadStatus.STATUS_DOWNLOAD_PAUSED) {
            onFail(item, errorCode, msg)
        }
        ZLog.d(TAG, "cancelDownload connectList:" + DownloadingPartList.getDownloadingPartNum())
    }

}