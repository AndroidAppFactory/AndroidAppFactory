package com.bihe0832.android.lib.download.core

import android.content.Context
import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadErrorCode.*
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadItem.TAG
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadPartInfo
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.core.list.DownloadingList
import com.bihe0832.android.lib.download.core.list.DownloadingPartList
import com.bihe0832.android.lib.download.dabase.DownloadInfoDBManager
import com.bihe0832.android.lib.download.dabase.DownloadPartInfoTableModel
import com.bihe0832.android.lib.download.part.DOWNLOAD_MIN_SIZE
import com.bihe0832.android.lib.download.part.DOWNLOAD_PART_SIZE
import com.bihe0832.android.lib.download.part.DownloadThread
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.HTTPRequestUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.encrypt.MD5
import com.bihe0832.android.lib.utils.encrypt.SHA256
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


class DownloadByHttp(private var applicationContext: Context, private var maxNum: Int, private val innerDownloadListener: DownloadListener) {

    private var hasStart = false
    private val MAX_RETRY_TIMES = 2
    private val MAX_DOWNLOAD_THREAD = 5
    private val MAX_DOWNLOAD_TOTAL_THREAD = 30

    fun startDownload(context: Context, info: DownloadItem) {
        ZLog.e(TAG, "开始下载:${info}")
        if (applicationContext == null) {
            applicationContext = context.applicationContext
        }

        if (DownloadingList.isDownloading(info)) {
            ZLog.d(TAG, "download has start")
            DownloadingList.addToDownloadingList(info)
            return
        }

        if (updateDownItemByServerInfo(info)) {
            //强制下载的数量也要控制
            if (DownloadingList.getDownloadingNum() < maxNum || DownloadingPartList.getDownloadingPartNum() < MAX_DOWNLOAD_TOTAL_THREAD || DownloadItem.MAX_DOWNLOAD_PRIORITY == info.downloadPriority) {
                if (DownloadingList.getDownloadingNum() < maxNum || info.downloadPriority >= DownloadItem.FORCE_DOWNLOAD_PRIORITY) {
                    ZLog.d(TAG, "getDownloadList() is good")
                    addToDownloadList(info)
                    innerDownloadListener.onStart(info)
                    goDownload(info)
                    checkDownloadProcess()
                } else {
                    innerDownloadListener.onWait(info)
                }
            } else {
                innerDownloadListener.onWait(info)
            }
        } else {
            ZLog.d(TAG, "has notify in updateDownItemByServerInfo")
        }
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
                            newFinished += downloadPartItem.getDownloadPartInfo().partFinished
                            finishedBefore += downloadPartItem.getDownloadPartInfo().partFinishedBefore
                            ZLog.d(TAG, "第${downloadPartItem.getDownloadPartInfo().partID}分片信息:${downloadPartItem.getDownloadPartInfo()}")
                            if (downloadPartItem.getDownloadPartInfo().partStatus != DownloadStatus.STATUS_DOWNLOAD_SUCCEED) {
                                notFinished = true
                            }

                            if (downloadPartItem.getDownloadPartInfo().partStatus == DownloadStatus.STATUS_DOWNLOAD_FAILED) {
                                hasFail = true
                                errorInfo = "download part exception: ${downloadPartItem.getDownloadPartInfo().downloadPartID}"
                            }
                        }
                        if (newFinished - finishedBefore < 1) {
                            downloadItem.lastSpeed = 0
                        } else {
                            downloadItem.lastSpeed = newFinished - downloadItem.finished
                        }
                        downloadItem.finished = newFinished
                        downloadItem.finishedLengthBefore = finishedBefore
                        if (DownloadManager.isDebug()) ZLog.d(TAG, "分片下载汇总 - ${downloadItem.downloadID}: 完成长度:${FileUtils.getFileLength(newFinished)} 之前下载长度:${FileUtils.getFileLength(finishedBefore)}")
                        if (DownloadManager.isDebug()) ZLog.d(TAG, "分片下载汇总 - ${downloadItem.downloadID}: " +
                                "文件长度 :${FileUtils.getFileLength(downloadItem.fileLength)}" +
                                ";完成长度 :${FileUtils.getFileLength(downloadItem.finished)}" +
                                ";之前下载长度 :${FileUtils.getFileLength(downloadItem.finishedLengthBefore)}" +
                                ";本次下载累计长度 :${FileUtils.getFileLength(newFinished - downloadItem.finishedLengthBefore)} ，新增长度: ${FileUtils.getFileLength(downloadItem.lastSpeed)}")
                        if (downloadItem.fileLength > 0 && downloadItem.finished >= downloadItem.fileLength) {
                            downloadItem.finished = downloadItem.fileLength
                        }
                        innerDownloadListener.onProgress(downloadItem)
                    }

                    if (!notFinished) {
                        notifyDownloadAfterFinish(downloadItem)
                    } else if (downloadItem.fileLength > 0 && downloadItem.finished == downloadItem.fileLength) {
                        notifyDownloadAfterFinish(downloadItem)
                    } else if (hasFail) {
                        notifyDownloadFailed(ERR_DOWNLOAD_PART_EXCEPTION, errorInfo, downloadItem)
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

    //返回资源可以下载
    private fun updateDownItemByServerInfo(info: DownloadItem): Boolean {
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
                var time = System.currentTimeMillis()
                connection.connect()
                ZLog.e(TAG, "获取文件长度，请求用时: ${System.currentTimeMillis() - time} ~~~~~~~~~~~~~")
                if (DownloadManager.isDebug()) {
                    connection.logHeaderFields("获取文件长度")
                }
                var contentLength = HTTPRequestUtils.getContentLength(connection)
                ZLog.e(TAG, "获取文件长度 getContentType:${connection.contentType}")
                ZLog.e(TAG, "获取文件长度 getContentLength:${contentLength}")
                ZLog.e(TAG, "获取文件长度 responseCode:${connection.responseCode}")
                if (connection.responseCode == HttpURLConnection.HTTP_OK || connection.responseCode == HttpURLConnection.HTTP_PARTIAL) {
                    info.realURL = realURL
                    if (contentLength > 0) {
                        info.fileLength = contentLength
                        ZLog.d(TAG, "获取文件长度 保存信息:${info}")
                        DownloadInfoDBManager.saveDownloadInfo(info)
                        return true
                    } else {
                        ZLog.e(TAG, "获取文件长度 长度为0: $times ${info.downloadID}")
                        if (times > MAX_RETRY_TIMES) {
                            info.fileLength = 0
                            return true
                        } else {
                            realURL = HTTPRequestUtils.getRedirectUrl(info.downloadURL)
                        }
                    }
                } else {
                    if (times > MAX_RETRY_TIMES) {
                        ZLog.e(TAG, "download with error file length after max times:${connection.responseCode} " + info)
                        //请求三次都失败在结束
                        notifyDownloadFailed(ERR_HTTP_LENGTH_FAILED, "download with error file length after max times", info)
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
                    notifyDownloadFailed(ERR_HTTP_EXCEPTION, "download with exception after three times:${e.javaClass.name}", info)
                    ZLog.e(TAG, "获取文件长度 异常: download with exception after three times:${e.javaClass.name}")
                    return false
                }
            }
        } while (true)
    }

    private fun goDownload(info: DownloadItem) {
        ZLog.d(TAG, "goDownload:$info")
        ZLog.d("DebugDownloadFragment", "goDownload:${info.downloadURL}")

        try {
            val file = File(info.tempFilePath)
            var hasDownload = DownloadInfoDBManager.hasDownloadPartInfo(info.downloadID, DownloadManager.isDebug())
            if (file.exists() && hasDownload && info.fileLength > 0 && file.length() <= info.fileLength) {
                ZLog.e(TAG, "断点续传逻辑:$info")
                //断点续传逻辑
                ZLog.e(TAG, "分片下载数据 - ${info.downloadID} 历史下载计算前: 之前已完成${FileUtils.getFileLength(info.finishedLengthBefore)}，累积已完成: ${FileUtils.getFileLength(info.finished)}")
                info.finishedLengthBefore = DownloadInfoDBManager.getFinishedBefore(info.downloadID)
                if (info.finishedLengthBefore > info.finished) {
                    info.finished = info.finishedLengthBefore
                }
                ZLog.e(TAG, "分片下载数据 - ${info.downloadID} 历史下载计算后: 之前已完成${FileUtils.getFileLength(info.finishedLengthBefore)}，累积已完成: ${FileUtils.getFileLength(info.finished)}")
                ZLog.e(TAG, "分片下载数据 - ${info.downloadID} : file length:${FileUtils.getFileLength(info.fileLength)}, finished before: ${FileUtils.getFileLength(info.finishedLengthBefore)}, need download ${FileUtils.getFileLength(info.fileLength - info.finishedLengthBefore)}")
                var cursor = DownloadInfoDBManager.getDownloadPartInfo(info.downloadID)
                ZLog.e(TAG, "分片下载数据 - ${info.downloadID} - 已有分片:${cursor.count}")
                try {
                    cursor.moveToFirst()
                    while (!cursor.isAfterLast) {
                        var id = cursor.getInt(cursor.getColumnIndex(DownloadPartInfoTableModel.col_part_id))
                        var start = cursor.getLong(cursor.getColumnIndex(DownloadPartInfoTableModel.col_start))
                        var end = cursor.getLong(cursor.getColumnIndex(DownloadPartInfoTableModel.col_end))
                        var finished = cursor.getLong(cursor.getColumnIndex(DownloadPartInfoTableModel.col_finished))
                        ZLog.e(TAG, "分片下载数据 - ${info.downloadID} - 继续已有分片:${info.downloadID - id} start:$start end:$end finished:$finished")
                        startDownloadPart(id, info, start, end, finished)
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
                startNew(info)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            if (info.status != DownloadStatus.STATUS_DOWNLOAD_PAUSED) {
                notifyDownloadFailed(ERR_DOWNLOAD_EXCEPTION, "download with exception$e", info)
            }
        }
    }

    private fun startNew(info: DownloadItem) {
        ZLog.d(TAG, "startNew:$info")
        var threadNum = 1
        if (info.fileLength > DOWNLOAD_PART_SIZE) {
            // 先分大片
            threadNum = (info.fileLength / DOWNLOAD_PART_SIZE).toInt().let {
                ZLog.e(TAG, "分片下载: 文件长度: ${info.fileLength}，默认分片大小：${DOWNLOAD_PART_SIZE}，按默认分片可分片：${it}")
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
            ZLog.e(TAG, "分片下载: 文件长度: ${info.fileLength}，二次分片数量：${threadNum}，并行下载量数量：${maxNum}")
            if (threadNum < 1) {
                threadNum = 1
            } else if (threadNum > MAX_DOWNLOAD_THREAD) {
                threadNum = MAX_DOWNLOAD_THREAD
            }
            ZLog.e(TAG, "分片下载: 文件长度: ${info.fileLength}，三次分片数量：${threadNum}，并行下载量数量：${maxNum}")
            //太小的文件分小片
            if (info.fileLength / threadNum < DOWNLOAD_MIN_SIZE) {
                threadNum = (info.fileLength / DOWNLOAD_MIN_SIZE).toInt()
            }
            if (threadNum < 1) {
                threadNum = 1
            }
        } else {
            threadNum = 1
        }
        var newpart = info.fileLength / threadNum
        if ((info.fileLength - newpart * (threadNum - 1)) < newpart / 5) {
            if (threadNum > 1) {
                threadNum -= 1
            }
        }
        ZLog.e(TAG, "分片下载: 文件长度: ${info.fileLength}，最终分片数量：${threadNum}")
        ZLog.e(TAG, "分片下载: 最后一片长度: ${info.fileLength - newpart * (threadNum - 1)}")
        if (threadNum > 1) {
            for (i in 0 until threadNum) {
                var start = i * newpart
                ZLog.d("分片下载：开始第$i 段")
                when (i) {
                    0 -> {
                        startDownloadPart(i, info, 0, newpart, 0)
                    }
                    threadNum - 1 -> {
                        startDownloadPart(i, info, start, info.fileLength, 0)
                    }
                    else -> {
                        startDownloadPart(i, info, start, start + newpart, 0)
                    }
                }
            }
        } else {
            startDownloadPart(0, info, 0, newpart, 0)
        }
    }

    private fun startDownloadPart(partNo: Int, info: DownloadItem, oldstart: Long, end: Long, finished: Long) {
        ZLog.e(TAG, "分片下载数据 第${partNo}分片 start: $oldstart, end:$end,finished: $finished ,length :${end - oldstart}, 文件长度:${info.fileLength} ")
        val downloadThreadForPart = DownloadThread(DownloadPartInfo().apply {
            this.partID = partNo
            this.downloadID = info.downloadID
            this.realDownloadURL = info.realURL
            this.finalFileName = info.tempFilePath
            this.partStart = oldstart
            this.partEnd = end
            this.partFinished = finished
            this.partFinishedBefore = finished
        }.also {
            ZLog.d(TAG, "分片下载数据 - ${info.downloadID}: 开始第$partNo 段开始:$it")
        })
        DownloadingPartList.addDownloadingPart(downloadThreadForPart)
        DownloadInfoDBManager.saveDownloadPartInfo(downloadThreadForPart.getDownloadPartInfo())
        innerDownloadListener.onProgress(info)

        try {
            downloadThreadForPart.start()
        } catch (e: Throwable) {
            e.printStackTrace()
            notifyDownloadFailed(ERR_DOWNLOAD_PART_START_EXCEPTION, "download with exception:$e", info)
        }
    }

    private fun addToDownloadList(info: DownloadItem) {
        DownloadingList.addToDownloadingList(info)
    }

    fun closeDownload(downloadID: Long, isFinished: Boolean, clearHistory: Boolean) {
        ZLog.d(TAG, "cancelDownload connectList:" + DownloadingPartList.getDownloadingPartNum())
        ZLog.d(TAG, "cancelDownload downloadList:" + DownloadingList.getDownloadingNum())

        DownloadingPartList.removeItem(downloadID, isFinished)
        if (clearHistory) {
            DownloadInfoDBManager.clearDownloadPartByID(downloadID)
        }
        DownloadingList.removeFromDownloadingList(downloadID)
        ZLog.d(TAG, "cancelDownload connectList:" + DownloadingPartList.getDownloadingPartNum())
        ZLog.d(TAG, "cancelDownload downloadList:" + DownloadingList.getDownloadingNum())
    }


    fun deleteFile(downloadInfo: DownloadItem) {
        ThreadManager.getInstance().start {
            FileUtils.deleteFile(downloadInfo.finalFilePath)
            FileUtils.deleteFile(downloadInfo.tempFilePath)
        }
    }


    private fun notifyDownloadFailed(errorCode: Int, msg: String, item: DownloadItem) {
        ZLog.e(TAG, "notifyDownloadFailed errorCode $errorCode, msg: $msg, item: $item")
        closeDownload(item.downloadID, false, false)
        if (item.status != DownloadStatus.STATUS_DOWNLOAD_PAUSED) {
            innerDownloadListener.onFail(errorCode, msg, item)
        }
    }

    private fun notifyDownloadAfterFinish(downloadInfo: DownloadItem) {
        closeDownload(downloadInfo.downloadID, true, false)
        var downloadFile = downloadInfo.tempFilePath
        ThreadManager.getInstance().start {
            try {
                val oldfile = File(downloadFile)
                ZLog.e(TAG, " oldfile:$oldfile")
                ZLog.e(TAG, " oldfile length:" + oldfile.length())
                if (TextUtils.isEmpty(downloadInfo.fileMD5)) {
                    if (TextUtils.isEmpty(downloadInfo.fileSHA256)) {
                        notifyDownloadSucc(downloadInfo)
                    } else {
                        val sha256 = SHA256.getFileSHA256(downloadFile)
                        ZLog.e(TAG, " oldfile SHA256:$sha256")
                        ZLog.e(TAG, " downloadInfo md5:" + downloadInfo.fileSHA256)
                        if (sha256.equals(downloadInfo.fileSHA256, ignoreCase = true)) {
                            notifyDownloadSucc(downloadInfo)
                        } else {
                            notifyDownloadFailed(ERR_MD5_BAD, "Sorry! the file SHA256 is bad", downloadInfo)
                            if (downloadInfo.isForceDeleteBad) {
                                deleteFile(downloadInfo)
                            }
                        }
                    }
                } else {
                    val md5 = MD5.getFileMD5(downloadFile)
                    ZLog.e(TAG, " oldfile md5:$md5")
                    ZLog.e(TAG, " downloadInfo md5:" + downloadInfo.fileMD5)
                    if (md5.equals(downloadInfo.fileMD5, ignoreCase = true)) {
                        notifyDownloadSucc(downloadInfo)
                    } else {
                        notifyDownloadFailed(ERR_MD5_BAD, "Sorry! the file md5 is bad", downloadInfo)
                        if (downloadInfo.isForceDeleteBad) {
                            deleteFile(downloadInfo)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                notifyDownloadFailed(ERR_FILE_RENAME_FAILED, "Sorry! the file can't be renamed", downloadInfo)
            }
        }
    }

    private fun notifyDownloadSucc(downloadInfo: DownloadItem) {
        var downloadFile = downloadInfo.tempFilePath
        var finalFileName = downloadInfo.finalFilePath
        val oldfile = File(downloadFile)
        val newfile = File(finalFileName)
        when {
            downloadFile.equals(finalFileName) -> {
                innerDownloadListener.onComplete(finalFileName, downloadInfo)
            }
            oldfile.renameTo(newfile) -> {
                ZLog.e(TAG, " File renamed")
                ZLog.e(TAG, " finalFile:$finalFileName")
                ZLog.e(TAG, " finalFile length:" + newfile.length())
                innerDownloadListener.onComplete(finalFileName, downloadInfo)
            }
            else -> {
                ZLog.e("Sorry! the file can't be renamed")
                innerDownloadListener.onComplete(downloadFile, downloadInfo)
            }
        }
    }
}