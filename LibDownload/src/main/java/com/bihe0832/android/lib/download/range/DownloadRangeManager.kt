package com.bihe0832.android.lib.download.range

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_BAD_URL
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_RANGE_BAD_PATH
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_URL_IS_TOO_OLD_THAN_LOACL
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadItem.TAG
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.core.DownloadManager
import com.bihe0832.android.lib.download.core.DownloadingList
import com.bihe0832.android.lib.download.core.dabase.DownloadInfoDBManager
import com.bihe0832.android.lib.download.file.notify.DownloadFileNotify
import com.bihe0832.android.lib.file.mimetype.FileMimeTypes
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.toast.ToastUtil
import java.io.File

@SuppressLint("StaticFieldLeak")
object DownloadRangeManager : DownloadManager() {

    private val mDownloadEngine by lazy {
        DownloadByHttpForRange(mContext!!, innerDownloadListener, mMaxNum, mIsDebug)
    }


    private val mDownloadStart = HashMap<Long, Long>()
    private val mDownloadLength = HashMap<Long, Long>()
    override fun init(context: Context, maxNum: Int, isDebug: Boolean) {
        super.init(context, maxNum, isDebug)
        if (!mHasInit) {
            mHasInit = true
            DownloadFileNotify.init(context)
        }
    }

    fun onDestroy() {
        pauseAllTask(startByUser = false, pauseMaxDownload = true)
        DownloadFileNotify.destroy()
    }

    private val innerDownloadListener = object : DownloadListener {
        override fun onWait(item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_WAITING
            item.downloadListener?.onWait(item)
            DownloadInfoDBManager.saveDownloadInfo(item)
            if (item.notificationVisibility()) {
                DownloadFileNotify.notifyProcess(item)
            }
        }

        override fun onStart(item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_STARTED
            item.lastSpeed = 0
            item.startTime = System.currentTimeMillis()
            item.downloadListener?.onStart(item)
            DownloadInfoDBManager.saveDownloadInfo(item)
            if (item.notificationVisibility()) {
                DownloadFileNotify.notifyProcess(item)
            }
        }

        override fun onProgress(item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOADING
            item.downloadListener?.onProgress(item)
            if (item.notificationVisibility()) {
                DownloadFileNotify.notifyProcess(item)
            }
        }

        override fun onPause(item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_PAUSED
            item.downloadListener?.onPause(item)
            if (item.notificationVisibility()) {
                DownloadFileNotify.notifyPause(item)
            }
            addWaitToDownload()
        }

        override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_FAILED
            addWaitToDownload()
            if (ERR_URL_IS_TOO_OLD_THAN_LOACL == errorCode) {
                ToastUtil.showLong(mContext, "本机已有更高版本的${item.downloadTitle}，下载已取消")
            }
            item.downloadListener?.onFail(errorCode, msg, item)
            if (item.notificationVisibility()) {
                DownloadFileNotify.notifyFailed(item)
            }
        }

        override fun onComplete(filePath: String, item: DownloadItem): String {
            item.status = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
            if (item.fileLength < 1) {
                item.fileLength = File(filePath).length()
            }
            item.finished = item.fileLength
            if (FileMimeTypes.isApkFile(filePath)) {
                mContext?.packageManager?.getPackageArchiveInfo(
                    filePath,
                    PackageManager.GET_ACTIVITIES,
                )?.packageName?.let {
                    item.packageName = it
                }
            }
            DownloadInfoDBManager.saveDownloadInfo(item)
            addDownloadItemToList(item)
            addWaitToDownload()

            ThreadManager.getInstance().start {
                ZLog.d(TAG, "onComplete start: $filePath ")
                var newPath = item.downloadListener?.onComplete(filePath, item) ?: item.filePath
                ZLog.d(TAG, "onComplete end: $newPath ")
                item.filePath = newPath
                DownloadInfoDBManager.saveDownloadInfo(item)
                if (item.notificationVisibility()) {
                    DownloadFileNotify.notifyFinished(item)
                }
                if (item.isAutoInstall) {
                    InstallUtils.installAPP(mContext, newPath)
                }
            }

            return filePath
        }

        override fun onDelete(item: DownloadItem) {
            item.downloadListener?.onDelete(item)
            if (item.notificationVisibility()) {
                DownloadFileNotify.notifyDelete(item)
            }
        }
    }

    override fun addWaitToDownload() {
        getWaitingTask().let { list ->
            if (list.isNotEmpty()) {
                list.maxByOrNull { it.downloadPriority }?.let {
                    ThreadManager.getInstance().start {
                        startTask(
                            it,
                            mDownloadStart.get(it.downloadID) ?: 0,
                            mDownloadLength.get(it.downloadID) ?: 0,
                            it.isDownloadWhenAdd,
                            it.isDownloadWhenUseMobile
                        )
                    }
                }
            }
        }
    }

    @Synchronized
    private fun startTask(
        info: DownloadItem,
        rangeStart: Long,
        rangeLength: Long,
        downloadAfterAdd: Boolean,
        downloadWhenUseMobile: Boolean
    ) {
        if (TextUtils.isEmpty(info.downloadActionKey)) {
            info.setDownloadActionKey(rangeStart, rangeLength)
        }
        innerDownloadListener.onWait(info)
        if (downloadAfterAdd) {
            if (!isWifi()) {
                if (downloadWhenUseMobile) {
                    realStartTask(info, rangeStart, rangeLength, true)
                } else {
                    realStartTask(info, rangeStart, rangeLength, false)
                }
            } else {
                realStartTask(info, rangeStart, rangeLength, true)
            }
        } else {
            ZLog.d(TAG, "startTask do nothing: $ $info ")
            realStartTask(info, rangeStart, rangeLength, false)
        }
    }


    private fun realStartTask(info: DownloadItem, rangeStart: Long, rangeLength: Long, downloadAfterAdd: Boolean) {
        ZLog.d(TAG, "startTask:$info")
        try {
            if (TextUtils.isEmpty(info.downloadActionKey)) {
                info.setDownloadActionKey(rangeStart, rangeLength)
            }
            // 不合法的URl
            if (!URLUtils.isHTTPUrl(info.downloadURL)) {
                ZLog.e(TAG, "bad para:$info")
                innerDownloadListener.onFail(ERR_BAD_URL, "bad para", info)
                return
            }
            val file = File(info.filePath)
            if (!file.exists() || (file.exists() && !File(info.filePath).isFile)) {
                ZLog.e(TAG, "bad file path:$info")
                innerDownloadListener.onFail(ERR_RANGE_BAD_PATH, "bad para, file not exist or not file", info)
                return
            }
            addDownloadItemToList(info, rangeStart, rangeLength)
            Thread {
                // 本地已下载
                if (downloadAfterAdd) {
                    var currentTime = System.currentTimeMillis()
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
                    mDownloadEngine.startDownload(mContext!!, info, rangeStart, rangeLength)
                } else {
                    info.setPause()
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
            ZLog.e(TAG, "download:$e")
        }
    }

    fun addDownloadItemToList(info: DownloadItem, rangeStart: Long, rangeLength: Long) {
        if (TextUtils.isEmpty(info.downloadActionKey)) {
            info.setDownloadActionKey(rangeStart, rangeLength)
        }
        mDownloadStart.put(info.downloadID, rangeStart)
        mDownloadLength.put(info.downloadID, rangeLength)
        addDownloadItemToList(info)
    }

    fun addTask(info: DownloadItem, rangeStart: Long, rangeLength: Long) {
        ZLog.d(TAG, "addTask:$info")
        if (TextUtils.isEmpty(info.downloadActionKey)) {
            info.setDownloadActionKey(rangeStart, rangeLength)
        }
        if (DownloadingList.isDownloading(info)) {
            val currentDownload = DownloadRangeTaskList.getTaskByDownloadID(info.downloadID)
            currentDownload?.downloadListener = info.downloadListener
        } else {
            updateInfo(info, false)
            val file = File(info.filePath)
            if (!file.exists() || (file.exists() && !File(info.filePath).isFile)) {
                ZLog.e(TAG, "bad file path:$info")
                innerDownloadListener.onFail(ERR_RANGE_BAD_PATH, "bad para, file not exist or not file", info)
                return
            }
            innerDownloadListener.onWait(info)
            if (info.isForceDownloadNew) {
                // 此前下载的文件不完整
                if (TextUtils.isEmpty(checkBeforeDownloadFile(info))) {
                    deleteTask(info.downloadID, startByUser = false, deleteFile = true)
                }
                File(info.filePath).createNewFile()
            }
            if (DownloadRangeTaskList.hadAddTask(info)) {
                ZLog.d(TAG, "mDownloadList contains:$info")
                DownloadRangeTaskList.updateDownloadTaskListItem(info)
                resumeTask(info.downloadID, info.downloadListener, info.isDownloadWhenAdd, info.isDownloadWhenUseMobile)
            } else {
                startTask(info, rangeStart, rangeLength, info.isDownloadWhenAdd, info.isDownloadWhenUseMobile)
            }
        }
    }

    fun resumeTask(
        downloadId: Long,
        downloadListener: DownloadListener?,
        startByUser: Boolean,
        downloadWhenUseMobile: Boolean,
    ) {
        DownloadRangeTaskList.getTaskByDownloadID(downloadId)?.let { info ->
            ZLog.d(TAG, "resumeTask:$info")
            if (startByUser) {
                info.isDownloadWhenAdd = true
            }
            downloadListener?.let {
                info.downloadListener = it
            }
            innerDownloadListener.onWait(info)
            startTask(
                info,
                mDownloadStart.get(info.downloadID) ?: 0,
                mDownloadLength.get(info.downloadID) ?: 0,
                startByUser,
                downloadWhenUseMobile
            )
        }
    }

    fun pauseTask(downloadId: Long, startByUser: Boolean, clearHistory: Boolean) {
        DownloadRangeTaskList.getTaskByDownloadID(downloadId)?.let { info ->
            ZLog.d(TAG, "pause:$info")
            ZLog.d(TAG, "pause:$info")
            mDownloadEngine.closeDownload(info.downloadID, false, clearHistory)
            info.status = DownloadStatus.STATUS_DOWNLOAD_PAUSED
            info.setPause()

            if (startByUser) {
                innerDownloadListener.onPause(info)
            }
        }
    }

    fun deleteTask(downloadId: Long, startByUser: Boolean, deleteFile: Boolean) {
        DownloadRangeTaskList.getTaskByDownloadID(downloadId)?.let { info ->
            if (info.status == DownloadStatus.STATUS_DOWNLOADING) {
                addWaitToDownload()
            }
            DownloadRangeTaskList.removeFromDownloadTaskList(downloadId)
            info.status = DownloadStatus.STATUS_DOWNLOAD_DELETE
            mDownloadEngine.closeDownload(downloadId, false, deleteFile)
            DownloadInfoDBManager.clearDownloadInfoByID(info.downloadID)
            if (deleteFile) {
                mDownloadEngine.deleteFile(info)
            }
            DownloadFileNotify.notifyDelete(info)
            innerDownloadListener.onDelete(info)
        }
    }

    fun pauseAllTask(startByUser: Boolean, pauseMaxDownload: Boolean) {
        ZLog.d(TAG, "pauseAllTask")
        pauseWaitingTask(startByUser, pauseMaxDownload)
        pauseDownloadingTask(startByUser, pauseMaxDownload)
    }

    fun pauseDownloadingTask(startByUser: Boolean, pauseMaxDownload: Boolean) {
        ZLog.d(TAG, "pauseDownloadingTask")
        getDownloadingTask().forEach {
            if (it.downloadPriority == DownloadItem.MAX_DOWNLOAD_PRIORITY) {
                if (pauseMaxDownload) {
                    pauseTask(it.downloadID, startByUser, false)
                } else {
                    ZLog.e(TAG, "skip pause maxPriority download:$it")
                    ZLog.e(TAG, "skip pause maxPriority download:$it")
                }
            } else {
                pauseTask(it.downloadID, startByUser, false)
            }
        }
    }

    fun pauseWaitingTask(startByUser: Boolean, pauseMaxDownload: Boolean) {
        ZLog.d(TAG, "pauseWaitingTask")
        getWaitingTask().forEach {
            if (it.downloadPriority == DownloadItem.MAX_DOWNLOAD_PRIORITY) {
                if (pauseMaxDownload) {
                    pauseTask(it.downloadID, startByUser, false)
                } else {
                    ZLog.e(TAG, "skip pause maxPriority download:$it")
                    ZLog.e(TAG, "skip pause maxPriority download:$it")
                }
            } else {
                pauseTask(it.downloadID, startByUser, false)
            }
        }
    }

    fun resumeAllTask(pauseOnMobile: Boolean) {
        ZLog.d(TAG, "resumeAllTask")
        resumePauseTask(pauseOnMobile)
        resumeFailedTask(pauseOnMobile)
    }

    fun resumeFailedTask(pauseOnMobile: Boolean) {
        ZLog.d(TAG, "resumeFailedTask")
        getAllTask().filter { it.status == DownloadStatus.STATUS_DOWNLOAD_FAILED }.forEach {
            resumeTask(it.downloadID, it.downloadListener, true, pauseOnMobile)
        }
    }

    fun resumePauseTask(pauseOnMobile: Boolean) {
        ZLog.d(TAG, "resumePauseTask")
        getAllTask().filter { it.status == DownloadStatus.STATUS_DOWNLOAD_PAUSED }.forEach {
            resumeTask(it.downloadID, it.downloadListener, true, pauseOnMobile)
        }
    }

    override fun getAllTask(): List<DownloadItem> {
        return DownloadRangeTaskList.getDownloadTasKList()
    }

    override fun getTaskByDownloadID(downloadID: Long): DownloadItem? {
        return DownloadRangeTaskList.getTaskByDownloadID(downloadID)
    }

    override fun addToDownloadTaskList(info: DownloadItem) {
        DownloadRangeTaskList.addToDownloadTaskList(info)

    }

    override fun removeFromDownloadTaskList(downloadId: Long) {
        DownloadRangeTaskList.removeFromDownloadTaskList(downloadId)
    }
}
