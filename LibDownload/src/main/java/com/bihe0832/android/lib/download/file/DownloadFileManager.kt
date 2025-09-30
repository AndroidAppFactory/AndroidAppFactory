package com.bihe0832.android.lib.download.file

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadErrorCode
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_BAD_URL
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_MD5_BAD
import com.bihe0832.android.lib.download.DownloadErrorCode.ERR_URL_IS_TOO_OLD_THAN_DOWNLOADING
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
import com.bihe0832.android.lib.download.file.notify.DownloadFileNotify
import com.bihe0832.android.lib.file.FileUtils
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

    override fun onDestroy() {
        super.onDestroy()
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
                    ToastUtil.showLong(mContext, String.format(it, item.downloadTitle))
                }
            }
            item.downloadListener?.onFail(errorCode, msg, item)
            if (item.notificationVisibility()) {
                DownloadFileNotify.notifyFailed(item)
            }
        }

        override fun onComplete(filePath: String, item: DownloadItem): String {
            item.status = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
            val file = File(filePath)
            if (item.contentLength < 1) {
                item.contentLength = file.length()
            }
            val time = file.lastModified()
            FileUtils.updateModifiedTime(filePath).let {
                ZLog.e(TAG, "更新文件的最新更新时间: 更新前 $time, 更新后：${file.lastModified()}")
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
                if (item.isNeedRecord || !newPath.equals(filePath)) {
                    DownloadInfoDBManager.saveDownloadInfo(item)
                }
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
        val alreadyDownloadItem =
            DownloadInfoDBManager.getDownloadInfoFromPackageName(info.packageName)
        return if (alreadyDownloadItem == null) {
            ZLog.d(TAG, "checkIsNeedDownload alreadyDownloadItem null")
            false
        } else {
            if (alreadyDownloadItem.versionCode > 0) {
                if (info.versionCode != alreadyDownloadItem.versionCode) {
                    deleteTask(
                        alreadyDownloadItem.downloadID,
                        startByUser = false,
                        deleteFile = true
                    )
                }
                false
            } else {
                false
            }
        }
    }

    @Synchronized
    override fun startTask(info: DownloadItem, downloadAfterAdd: Boolean) {
        ZLog.d(TAG, "startTask:${info}")
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
                innerDownloadListener.onFail(
                    ERR_URL_IS_TOO_OLD_THAN_DOWNLOADING,
                    "install is new",
                    info
                )
                return
            }
            addDownloadItemToListAndSaveRecord(info)
            Thread {
                // 本地已下载
                val filePath = checkBeforeDownloadFile(info)
                if (!TextUtils.isEmpty(filePath)) {
                    ZLog.e(TAG, "has download:$info")
                    info.setDownloadStatus(DownloadStatus.STATUS_HAS_DOWNLOAD)
                    Thread.sleep(1000L)
                    innerDownloadListener.onComplete(info.filePath, info)
                } else {
                    if (downloadAfterAdd) {
                        if (isMobileNet() && !info.isDownloadWhenUseMobile) {
                            ZLog.e(TAG, "当前网络为移动网络，任务暂停:$info")
                            pauseTask(
                                info,
                                DownloadPauseType.PAUSED_BY_NETWORK,
                                clearHistory = false
                            )
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
                            if (!hasPauseAll()) {
                                mDownloadEngine.startDownload(mContext!!, info)
                            } else {
                                ZLog.e(TAG, "download paused by pause all")
                            }
                        }
                    } else {
                        ZLog.e(TAG, "download paused by downloadAfterAdd")
                        pauseTask(info, DownloadPauseType.PAUSED_BY_ADD, clearHistory = false)
                    }
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
            ZLog.e(TAG, "download:$e")
        }
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
            ZLog.d(TAG, "获取文件长度 保存信息:${info}")

            val file = File(info.filePath)
            if (file.exists() && !File(info.filePath).isFile) {
                ZLog.e(TAG, "bad file path:$info")
                innerDownloadListener.onFail(
                    DownloadErrorCode.ERR_RANGE_BAD_PATH,
                    "bad para, file not exist or not file",
                    info
                )
                return false
            }
            info.realURL = realURL
            info.rangeStart = rangeStart
            info.contentLength = serverContentLength
            info.localStart = localStart
            DownloadInfoDBManager.saveDownloadInfo(info)

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            ZLog.e(TAG, "download:$e")
            innerDownloadListener.onFail(
                DownloadErrorCode.ERR_CONTENT_LENGTH_EXCEPTION,
                "update server content exception",
                info
            )
            return false
        }

    }

    fun addTask(info: DownloadItem) {
        ThreadManager.getInstance().start {
            ZLog.d(TAG, "addTask:$info")
            info.downloadType = DownloadItem.TYPE_FILE
            if (DownloadingList.isDownloading(info)) {
                val currentDownload = DownloadTaskList.getTaskByDownloadID(info.downloadID)
                if (!TextUtils.isEmpty(currentDownload?.contentMD5) && !info.contentMD5.equals(
                        currentDownload?.contentMD5
                    )
                ) {
                    info.downloadListener?.onFail(
                        ERR_MD5_BAD,
                        "new md5 is diff with current download",
                        info
                    )
                } else if (!TextUtils.isEmpty(currentDownload?.contentSHA256) && !info.contentSHA256.equals(
                        currentDownload?.contentSHA256
                    )
                ) {
                    info.downloadListener?.onFail(
                        ERR_MD5_BAD,
                        "new sha256 is diff with current download",
                        info
                    )
                } else {
                    currentDownload?.downloadListener = info.downloadListener
                }
            } else {
                innerDownloadListener.onWait(info)
                updateInfo(info, true)
                val filePath = checkBeforeDownloadFile(info)
                if (!TextUtils.isEmpty(filePath)) {
                    ZLog.e(TAG, "has download:$info")
                    info.setDownloadStatus(DownloadStatus.STATUS_HAS_DOWNLOAD)
                    innerDownloadListener.onComplete(info.filePath, info)
                    return@start
                }
                if (info.shouldForceReDownload()) {
                    // 此前下载的文件不完整
                    if (TextUtils.isEmpty(filePath)) {
                        deleteTask(info.downloadID, startByUser = false, deleteFile = true)
                    }
                }
                if (!updateDownItemByServerInfo(info, 0, -1, 0, info.isDownloadWhenAdd)) {
                    ZLog.d(TAG, "has notify in updateDownItemByServerInfo")
                    return@start
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

    override fun deleteTask(downloadId: Long, startByUser: Boolean, deleteFile: Boolean) {
        DownloadTaskList.getTaskByDownloadID(downloadId)?.let { info ->
            DownloadFileNotify.notifyDelete(info)
            super.deleteTask(downloadId, startByUser, deleteFile)
        }
    }

    override fun getAllTask(): List<DownloadItem> {
        return DownloadTaskList.getDownloadTasKList(DownloadItem.TYPE_FILE)
    }

    override fun getDownloadEngine(): DownloadByHttpBase {
        return mDownloadEngine
    }

    override fun getInnerDownloadListener(): DownloadListener {
        return innerDownloadListener
    }


}
