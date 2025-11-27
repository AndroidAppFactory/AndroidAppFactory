package com.bihe0832.android.lib.download.range

import android.annotation.SuppressLint
import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadErrorCode
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_BAD_URL
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_CONTENT_LENGTH_EXCEPTION
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_RANGE_BAD_DOWNLOAD_LENGTH
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_RANGE_BAD_PATH
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_URL_IS_TOO_OLD_THAN_LOACL
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadItem.TAG
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadPauseType
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.R
import com.bihe0832.android.lib.download.core.DownloadByHttpBase
import com.bihe0832.android.lib.download.core.DownloadManager
import com.bihe0832.android.lib.download.core.DownloadTaskList
import com.bihe0832.android.lib.download.core.DownloadingList
import com.bihe0832.android.lib.download.core.dabase.DownloadInfoDBManager
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.toast.ToastUtil
import java.io.File

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
            item.status = DownloadStatus.STATUS_DOWNLOADING
            item.downloadListener?.onProgress(item)
        }

        override fun onPause(item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_PAUSED
            item.downloadListener?.onPause(item)
            if (!hasPauseAll()) {
                addWaitToDownload()
            }
        }

        override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_FAILED
            if (!hasPauseAll()) {
                addWaitToDownload()
            }
            if (ERR_URL_IS_TOO_OLD_THAN_LOACL == errorCode) {
                getContext()?.getString(R.string.download_failed_local_is_new)?.let {
                    ToastUtil.showLong(context, String.format(it, item.downloadTitle))
                }
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
        serverContentLength: Long,
        downloadAfterAdd: Boolean
    ): Boolean {
        ZLog.d(TAG, "startTask:$info")
        try {
            if (serverContentLength > 0) {
                if (rangeLength > 0 && serverContentLength < rangeStart + rangeLength) {
                    //请求长度小于服务器获取的长度
                    innerDownloadListener.onFail(
                        DownloadErrorCode.ERR_RANGE_BAD_SERVER_LENGTH,
                        "download length is less than need",
                        info
                    )
                    return false
                }
            } else {
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
            info.contentLength = rangeLength
            info.localStart = localStart
            ZLog.d(TAG, "获取文件长度 保存信息:${info}")
            DownloadInfoDBManager.saveDownloadInfo(info)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            ZLog.e(TAG, "download:$e")
            innerDownloadListener.onFail(
                ERR_CONTENT_LENGTH_EXCEPTION,
                "update server content exception",
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
                ZLog.e(TAG, "bad para contentLength:$info")
                innerDownloadListener.onFail(ERR_BAD_URL, "bad para contentLength", info)
                return
            }
            addDownloadItemToListAndSaveRecord(info)
            Thread {
                if (downloadAfterAdd) {
                    if (isMobileNet() && !info.isDownloadWhenUseMobile) {
                        pauseTask(
                            info,
                            DownloadPauseType.PAUSED_BY_NETWORK,
                            clearHistory = false
                        )
                        ZLog.e(TAG, "当前网络为移动网络，任务暂停:$info")
                    } else {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - info.pauseTime < 3000L) {
                            ZLog.e(TAG, "resume to quick:$info")
                            innerDownloadListener.onWait(info)
                            info.isDownloadWhenAdd = true
                            (info.pauseTime + 3000L - currentTime).let {
                                if (it > 0) {
                                    Thread.sleep(it)
                                }
                            }
                        }
                        if (!hasPauseAll()) {
                            mDownloadEngine.startDownload(
                                info,
                                info.rangeStart,
                                info.contentLength,
                                info.localStart
                            )
                        } else {
                            ZLog.e(TAG, "download paused by pause all")
                        }
                    }
                } else {
                    ZLog.e(TAG, "download paused by downloadAfterAdd")
                    pauseTask(info, DownloadPauseType.PAUSED_BY_ADD, clearHistory = false)
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
                    info.setDownloadStatus(DownloadStatus.STATUS_HAS_DOWNLOAD)
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
}
