package com.bihe0832.android.lib.download.file

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_BAD_URL
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_MD5_BAD
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_URL_IS_TOO_OLD_THAN_DOWNLOADING
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
import com.bihe0832.android.lib.utils.apk.APKUtils
import java.io.File

@SuppressLint("StaticFieldLeak")
object DownloadFileManager : DownloadManager() {

    private val mDownloadEngine by lazy {
        DownloadByHttpForFile(mContext!!, innerDownloadListener, mMaxNum, mIsDebug)
    }


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
            if (item.contentLength < 1) {
                item.contentLength = File(filePath).length()
            }
            item.finished = item.contentLength
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
                        startTask(it, it.isDownloadWhenAdd, it.isDownloadWhenUseMobile)
                    }
                }
            }
        }
    }

    private fun checkIsInstalledAndLocalVersionIsNew(info: DownloadItem): Boolean {
        ZLog.d(TAG, "checkIsNeedDownload DownloadItem:$info")
        if (TextUtils.isEmpty(info.packageName)) {
            ZLog.d(TAG, "packageName is bad ")
            return false
        }
        if (info.versionCode < 1) {
            ZLog.d(TAG, "versionCode is bad ")
            return false
        }
        ZLog.d(TAG, "checkIsNeedDownload versionCode:${info.versionCode}")
        return try {
            val packageInfo = APKUtils.getInstalledPackage(mContext, info.packageName)
            if (packageInfo != null) {
                ZLog.d(TAG, "checkIsNeedDownload installVersionCode:${packageInfo.versionCode}")
                packageInfo.versionCode > info.versionCode
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun checkIsDownloadingAndVersionIsNew(info: DownloadItem): Boolean {
        ZLog.d(TAG, "checkIsDownloading DownloadItem:$info")
        if (TextUtils.isEmpty(info.packageName)) {
            ZLog.d(TAG, "packageName is bad ")
            return false
        }
        if (info.versionCode < 1) {
            ZLog.d(TAG, "versionCode is bad ")
            return false
        }
        ZLog.d(TAG, "checkIsNeedDownload versionCode:${info.versionCode}")
        val alreadyDownloadItem = DownloadInfoDBManager.getDownloadInfoFromPackageName(info.packageName)
        return if (alreadyDownloadItem == null) {
            ZLog.d(TAG, "checkIsNeedDownload alreadyDownloadItem null")
            false
        } else {
            if (alreadyDownloadItem.versionCode > 0) {
                if (info.versionCode != alreadyDownloadItem.versionCode) {
                    deleteTask(alreadyDownloadItem.downloadID, startByUser = false, deleteFile = true)
                }
                false
            } else {
                false
            }
        }
    }



    @Synchronized
    private fun startTask(info: DownloadItem, downloadAfterAdd: Boolean, downloadWhenUseMobile: Boolean) {
        innerDownloadListener.onWait(info)
        if (downloadAfterAdd) {
            if (!isWifi()) {
                if (downloadWhenUseMobile) {
                    realStartTask(info, true)
                } else {
                    realStartTask(info, false)
                }
            } else {
                realStartTask(info, true)
            }
        } else {
            ZLog.d(TAG, "startTask do nothing: $ $info ")
            realStartTask(info, false)
        }
    }


    private fun realStartTask(info: DownloadItem, downloadAfterAdd: Boolean) {
        ZLog.d(TAG, "startTask:$info")
        try {
            // 不合法的URl
            if (!URLUtils.isHTTPUrl(info.downloadURL)) {
                ZLog.e(TAG, "bad para:$info")
                innerDownloadListener.onFail(ERR_BAD_URL, "bad para", info)
                return
            }

            // 本地已有更高版本
            if (checkIsInstalledAndLocalVersionIsNew(info)) {
                ZLog.e(TAG, "no need download:$info")
                innerDownloadListener.onFail(ERR_URL_IS_TOO_OLD_THAN_LOACL, "install is new", info)
                return
            }

            // 正在下载更高版本
            if (checkIsDownloadingAndVersionIsNew(info)) {
                ZLog.e(TAG, "noneed download:$info")
                innerDownloadListener.onFail(ERR_URL_IS_TOO_OLD_THAN_DOWNLOADING, "install is new", info)
                return
            }

            addDownloadItemToList(info)
            Thread {
                // 本地已下载
                var filePath = checkBeforeDownloadFile(info)
                if (!TextUtils.isEmpty(filePath)) {
                    ZLog.e(TAG, "has download:$info")
                    info.setDownloadStatus(DownloadStatus.STATUS_HAS_DOWNLOAD)
                    innerDownloadListener.onComplete(info.filePath, info)
                } else {
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
                        mDownloadEngine.startDownload(mContext!!, info)
                    } else {
                        info.setPause()
                    }
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
            ZLog.e(TAG, "download:$e")
        }
    }

    fun addTask(info: DownloadItem) {
        ZLog.d(TAG, "addTask:$info")
        if (DownloadingList.isDownloading(info)) {
            val currentDownload = DownloadFileTaskList.getTaskByDownloadID(info.downloadID)
            if (!TextUtils.isEmpty(currentDownload?.fileMD5) && !info.fileMD5.equals(currentDownload?.fileMD5)) {
                info.downloadListener?.onFail(ERR_MD5_BAD, "new md5 is diff with current download", info)
            } else if (!TextUtils.isEmpty(currentDownload?.fileSHA256) && !info.fileSHA256.equals(currentDownload?.fileSHA256)) {
                info.downloadListener?.onFail(ERR_MD5_BAD, "new sha256 is diff with current download", info)
            } else {
                currentDownload?.downloadListener = info.downloadListener
            }
        } else {
            updateInfo(info,true)
            innerDownloadListener.onWait(info)
            if (info.isForceDownloadNew) {
                // 此前下载的文件不完整
                if (TextUtils.isEmpty(checkBeforeDownloadFile(info))) {
                    deleteTask(info.downloadID, startByUser = false, deleteFile = true)
                }
            }
            if (DownloadFileTaskList.hadAddTask(info)) {
                ZLog.d(TAG, "mDownloadList contains:$info")
                DownloadFileTaskList.updateDownloadTaskListItem(info)
                resumeTask(info.downloadID, info.downloadListener, info.isDownloadWhenAdd, info.isDownloadWhenUseMobile)
            } else {
                startTask(info, info.isDownloadWhenAdd, info.isDownloadWhenUseMobile)
            }
        }
    }

    fun resumeTask(
        downloadId: Long,
        downloadListener: DownloadListener?,
        startByUser: Boolean,
        downloadWhenUseMobile: Boolean,
    ) {
        DownloadFileTaskList.getTaskByDownloadID(downloadId)?.let { info ->
            ZLog.d(TAG, "resumeTask:$info")
            if (startByUser) {
                info.isDownloadWhenAdd = true
            }
            downloadListener?.let {
                info.downloadListener = it
            }
            info.finished = 0
            info.finishedLengthBefore = 0
            innerDownloadListener.onWait(info)
            startTask(info, startByUser, downloadWhenUseMobile)
        }
    }

    fun pauseTask(downloadId: Long, startByUser: Boolean, clearHistory: Boolean) {
        DownloadFileTaskList.getTaskByDownloadID(downloadId)?.let { info ->
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
        DownloadFileTaskList.getTaskByDownloadID(downloadId)?.let { info ->
            if (info.status == DownloadStatus.STATUS_DOWNLOADING) {
                addWaitToDownload()
            }
            DownloadFileTaskList.removeFromDownloadTaskList(downloadId)
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
        return DownloadFileTaskList.getDownloadTasKList()
    }

    override fun getTaskByDownloadID(downloadID: Long): DownloadItem? {
        return DownloadFileTaskList.getTaskByDownloadID(downloadID)
    }

    override fun addToDownloadTaskList(info: DownloadItem) {
        DownloadFileTaskList.addToDownloadTaskList(info)
    }

    override fun removeFromDownloadTaskList(downloadId: Long) {
        DownloadFileTaskList.removeFromDownloadTaskList(downloadId)
    }
}
