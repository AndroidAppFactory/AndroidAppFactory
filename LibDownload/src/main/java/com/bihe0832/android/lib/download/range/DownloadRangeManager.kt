package com.bihe0832.android.lib.download.range

import android.annotation.SuppressLint
import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadErrorCode
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_BAD_URL
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_RANGE_BAD_DOWNLOAD_LENGTH
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_RANGE_BAD_PATH
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_URL_IS_TOO_OLD_THAN_LOACL
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadItem.TAG
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadPauseType
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.core.DownloadExceptionAnalyzer
import com.bihe0832.android.lib.download.core.DownloadByHttpBase
import com.bihe0832.android.lib.download.core.DownloadManager
import com.bihe0832.android.lib.download.core.DownloadTaskList
import com.bihe0832.android.lib.download.core.DownloadingList
import com.bihe0832.android.lib.download.core.dabase.DownloadInfoDBManager
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.toast.ToastUtil
import okhttp3.Protocol
import java.io.File
import com.bihe0832.android.lib.aaf.res.R as ResR

@SuppressLint("StaticFieldLeak")
object DownloadRangeManager : DownloadManager() {

    private val mDownloadEngine by lazy {
        DownloadByHttpForRange(innerDownloadListener, maxNum, isDebug, downloadClientConfig)
    }

    private val innerDownloadListener = object : DownloadListener {
        override fun onWait(item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_WAITING
            item.downloadListener?.onWait(item)
            DownloadInfoDBManager.saveDownloadInfo(item)
        }

        override fun onStart(item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_STARTED
            item.lastSpeed = 0
            item.startTime = System.currentTimeMillis()
            item.downloadListener?.onStart(item)
            DownloadInfoDBManager.saveDownloadInfo(item)
        }

        override fun onProgress(item: DownloadItem) {
            // 如果已经暂停全部，不再触发进度回调（双重保护，避免竞态）
            if (hasPauseAll()) {
                ZLog.d(TAG, "onProgress skip because hasPauseAll")
                return
            }
            item.status = DownloadStatus.STATUS_DOWNLOADING
            item.downloadListener?.onProgress(item)
        }

        override fun onPause(item: DownloadItem, @DownloadPauseType pauseType: Int) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_PAUSED
            item.downloadListener?.onPause(item, pauseType)
            if (!hasPauseAll()) {
                addWaitToDownload()
            }
        }

        override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_FAILED
            if (!hasPauseAll()) {
                addWaitToDownload()
            }
            item.downloadListener?.onFail(errorCode, msg, item)
        }

        override fun onComplete(filePath: String, item: DownloadItem): String {
            val file = File(filePath)
            if (file.length() < item.contentLength) {
                onFail(
                    ERR_RANGE_BAD_DOWNLOAD_LENGTH,
                    "file length（${file.length()}） is less than need downlaod（${item.contentLength}） length",
                    item
                )
            } else {
                item.status = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
                // 下载成功，重置网络异常重试计数
                item.resetNetworkErrorRetryRound()
                addDownloadItemToListAndSaveRecord(item)
                closeDownloadAndRemoveRecord(item)
                if (!hasPauseAll()) {
                    addWaitToDownload()
                }
                ThreadManager.getInstance().start {
                    ZLog.d(TAG, "onComplete start: $filePath ")
                    val newPath = item.downloadListener?.onComplete(filePath, item) ?: item.filePath
                    ZLog.d(TAG, "onComplete end: $newPath ")
                    item.filePath = newPath
                    if (item.isNeedRecord) {
                        DownloadInfoDBManager.saveDownloadInfo(item)
                    }
                }
            }


            return filePath
        }

        override fun onDelete(item: DownloadItem) {
            item.downloadListener?.onDelete(item)
        }
    }

    override fun getDownloadEngine(): DownloadByHttpBase {
        return mDownloadEngine
    }

    override fun getInnerDownloadListener(): DownloadListener {
        return innerDownloadListener
    }

    override fun updateItemByServer(
        info: DownloadItem,
        rangeStart: Long,
        rangeLength: Long,
        localStart: Long,
        realURL: String,
        protocol: Protocol,
        serverContentLength: Long,
        downloadAfterAdd: Boolean
    ): Boolean {
        ZLog.d(TAG, "startTask:$info")
        // DEBUG: 记录 updateItemByServer 的关键参数
        ZLog.w(TAG, "updateItemByServer ================")
        ZLog.w(TAG, "updateItemByServer serverContentLength: $serverContentLength")
        ZLog.w(TAG, "updateItemByServer rangeStart: $rangeStart, rangeLength: $rangeLength")
        ZLog.w(TAG, "updateItemByServer 判断条件: serverContentLength > 0 = ${serverContentLength > 0}")
        try {
            if (serverContentLength > 0) {
                ZLog.w(TAG, "updateItemByServer serverContentLength > 0, 检查 rangeLength")
                // 修复：分片下载时，serverContentLength 是本次请求返回的数据长度，应与 rangeLength 比较
                if (rangeLength > 0 && serverContentLength < rangeLength) {
                    ZLog.w(TAG, "失败: serverContentLength($serverContentLength) < rangeLength($rangeLength)")
                    //请求长度小于服务器获取的长度
                    innerDownloadListener.onFail(
                        DownloadErrorCode.ERR_RANGE_BAD_SERVER_LENGTH,
                        "download length is less than need",
                        info
                    )
                    return false
                }
                ZLog.w(TAG, "updateItemByServer serverContentLength 检查通过")
            } else {
                ZLog.w(TAG, "updateItemByServer 失败: serverContentLength <= 0, 不支持分片下载")
                // 区间下载，但是源数据不支持分片，返回失败
                innerDownloadListener.onFail(
                    DownloadErrorCode.ERR_RANGE_NOT_SUPPORT,
                    "download maybe not support range download",
                    info
                )
                return false
            }

            val file = File(info.filePath)
            if (!file.exists() || (file.exists() && !File(info.filePath).isFile)) {
                ZLog.e(TAG, "bad file path:$info")
                innerDownloadListener.onFail(
                    ERR_RANGE_BAD_PATH,
                    "bad para, file not exist or not file",
                    info
                )
                return false
            }
            info.realURL = realURL
            info.rangeStart = rangeStart
            info.contentLength = serverContentLength
            info.localStart = localStart
            info.isDownloadWhenAdd = downloadAfterAdd
            ZLog.d(TAG, "获取文件长度 保存信息:${info}")
            DownloadInfoDBManager.saveDownloadInfo(info)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            ZLog.e(TAG, "download:$e")
            val internalErrorCode = DownloadExceptionAnalyzer.analyzeException(e)
            // 对外回调时收敛为旧版本错误码
            val externalErrorCode = DownloadExceptionAnalyzer.toExternalErrorCode(internalErrorCode)
            innerDownloadListener.onFail(
                externalErrorCode,
                "update server content exception: ${e.javaClass.simpleName}: ${e.message}",
                info
            )
            return false
        }

    }


    @Synchronized
    override fun startTask(info: DownloadItem, downloadAfterAdd: Boolean) {
        ZLog.d(TAG, "startTask:$info")
        try {
            // 不合法的URl
            if (!URLUtils.isHTTPUrl(info.downloadURL)) {
                ZLog.e(TAG, "bad para downloadURL:$info")
                innerDownloadListener.onFail(ERR_BAD_URL, "bad para downloadURL", info)
                return
            }
            if (info.contentLength < 1) {
                ZLog.e(TAG, "bad para contentLength, contentLength:${info.contentLength} $info")
                innerDownloadListener.onFail(ERR_BAD_URL, "bad para contentLength", info)
                return
            }
            addDownloadItemToListAndSaveRecord(info)
            Thread {
                if (downloadAfterAdd) {
                    if (isMobileNet() && !info.isDownloadWhenUseMobile) {
                        pauseTask(info, DownloadPauseType.PAUSED_BY_MOBILE_NETWORK)
                        ZLog.e(TAG, "当前网络为移动网络，任务暂停:$info")
                    } else {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - info.pauseTime < 3000L) {
                            ZLog.e(TAG, "resume to quick:$info")
                            innerDownloadListener.onWait(info)
                            (info.pauseTime + 3000L - currentTime).let {
                                if (it > 0) {
                                    Thread.sleep(it)
                                }
                            }
                        }
                        mDownloadEngine.startDownload(
                            info,
                            info.rangeStart,
                            info.contentLength,
                            info.localStart
                        )
                    }
                } else {
                    ZLog.e(TAG, "download paused by downloadAfterAdd")
                    pauseTask(info, DownloadPauseType.PAUSED_PENDING_START)
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
            ZLog.e(TAG, "download:$e")
        }
    }


    fun addTask(info: DownloadItem, rangeStart: Long, rangeLength: Long, localStart: Long) {
        ThreadManager.getInstance().start {
            ZLog.d(TAG, "addTask:$info")
            // DEBUG: 记录 addTask 的关键参数
            ZLog.w(TAG, "addTask START ================")
            ZLog.w(TAG, "downloadURL: ${info.downloadURL}")
            ZLog.w(TAG, "downloadWhenUseMobile: ${info.isDownloadWhenUseMobile}")
            ZLog.w(TAG, "downloadWhenAdd: ${info.isDownloadWhenAdd}")
            ZLog.w(TAG, "rangeStart: $rangeStart, rangeLength: $rangeLength, localStart: $localStart")
            info.downloadType = DownloadItem.TYPE_RANGE
            if (DownloadingList.isDownloading(info)) {
                val currentDownload = DownloadTaskList.getTaskByDownloadID(info.downloadID)
                currentDownload?.downloadListener = info.downloadListener
            } else {
                innerDownloadListener.onWait(info)
                if (!updateDownItemByServerInfo(
                        info,
                        rangeStart,
                        rangeLength,
                        localStart,
                        info.isDownloadWhenAdd
                    )
                ) {
                    ZLog.d(TAG, "has notify in updateDownItemByServerInfo")
                    return@start
                }
                updateInfo(info, false)
                val file = File(info.filePath)
                if (!file.exists() || (file.exists() && !File(info.filePath).isFile)) {
                    ZLog.e(TAG, "bad file path:$info")
                    innerDownloadListener.onFail(
                        ERR_RANGE_BAD_PATH,
                        "bad para, file not exist or not file",
                        info
                    )
                    return@start
                }
                val path = checkBeforeDownloadFile(info)
                if (!TextUtils.isEmpty(path)) {
                    ZLog.e(TAG, "has download:$info")
                    info.status = DownloadStatus.STATUS_HAS_DOWNLOAD
                    innerDownloadListener.onComplete(info.filePath, info)
                    return@start
                }
                if (info.shouldForceReDownload()) {
                    // 此前下载的文件不完整，删除文件，清空任务，重头开始
                    if (TextUtils.isEmpty(checkBeforeDownloadFile(info))) {
                        deleteTask(info.downloadID, startByUser = false, deleteFile = true)
                    }
                    File(info.filePath).createNewFile()
                }
                if (DownloadTaskList.hadAddTask(info)) {
                    ZLog.d(TAG, "mDownloadList contains:$info")
                    DownloadTaskList.updateDownloadTaskListItem(info)
                    resumeTask(
                        info.downloadID,
                        info.downloadListener,
                        info.isDownloadWhenAdd,
                        info.isDownloadWhenUseMobile
                    )
                } else {
                    addNewTask(info, info.isDownloadWhenAdd)
                }
            }
        }
    }


    override fun getAllTask(): List<DownloadItem> {
        return DownloadTaskList.getDownloadTasKList(DownloadItem.TYPE_RANGE)
    }

    override fun getDownloadingTask(): List<DownloadItem> {
        return DownloadingList.getDownloadingItemList(DownloadItem.TYPE_RANGE)
    }
}
