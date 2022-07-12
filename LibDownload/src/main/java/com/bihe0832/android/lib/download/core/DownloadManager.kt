package com.bihe0832.android.lib.download.core

import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadErrorCode.*
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.core.list.DownloadTaskList
import com.bihe0832.android.lib.download.core.list.DownloadingList
import com.bihe0832.android.lib.download.dabase.DownloadInfoDBManager
import com.bihe0832.android.lib.download.notify.DownloadNotify
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.mimetype.FileMimeTypes
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.network.NetworkUtil
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.toast.ToastUtil
import com.bihe0832.android.lib.utils.apk.APKUtils
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList


object DownloadManager {

    private val mDownloadEngine by lazy {
        DownloadByHttp(mContext!!, mMaxNum, innerDownloadListener)
    }

    private var mContext: Context? = null
    private var mDownloadListenerList = CopyOnWriteArrayList<DownloadListener>()
    private const val DEFAULT_MAX_NUM = 5
    private var mMaxNum = DEFAULT_MAX_NUM
    private var mHasInit = false
    private var mIsDebug = false

    fun getContext(): Context? {
        return mContext
    }

    fun isDebug(): Boolean {
        return mIsDebug
    }

    fun init(context: Context) {
        init(context, mMaxNum, null, mIsDebug)
    }

    fun init(context: Context, maxNum: Int, listener: DownloadListener?, isDebug: Boolean = false) {
        initContext(context)
        mMaxNum = maxNum
        mIsDebug = isDebug
        if (!mHasInit) {
            mHasInit = true
            DownloadNotify.init(context)
            DownloadInfoDBManager.init(context)
        }
        listener?.let {
            if (!mDownloadListenerList.contains(it)) {
                mDownloadListenerList.add(it)
            }
        }
    }

    private fun initContext(context: Context?) {
        context?.let {
            mContext = it
        }
    }

    fun onDestroy() {
        pauseAllTask(false)
        DownloadNotify.destroy()
        mDownloadListenerList.clear()
    }

    private val innerDownloadListener = object : DownloadListener {
        override fun onWait(item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_WAITING
            DownloadNotify.notifyProcess(item)
            mDownloadListenerList.forEach {
                it.onWait(item)
            }
            item.downloadListener?.let {
                it.onWait(item)
            }
            if (item.canDownloadByPart()) {
                DownloadInfoDBManager.saveDownloadInfo(item)
            }
        }

        override fun onStart(item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_STARTED
            item.lastSpeed = 0
            DownloadNotify.notifyProcess(item)
            item.startTime = System.currentTimeMillis()
            mDownloadListenerList.forEach {
                it.onStart(item)
            }
            item.downloadListener?.let {
                it.onStart(item)
            }
            if (item.canDownloadByPart()) {
                DownloadInfoDBManager.saveDownloadInfo(item)
            }
        }

        override fun onProgress(item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOADING

            mDownloadListenerList.forEach {
                it.onProgress(item)
            }

            item.downloadListener?.let {
                it.onProgress(item)
            }

            if (item.notificationVisibility()) {
                DownloadNotify.notifyProcess(item)
            }
        }

        override fun onPause(item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_PAUSED
            mDownloadListenerList.forEach {
                it.onPause(item)
            }

            item.downloadListener?.let {
                it.onPause(item)
            }

            if (item.notificationVisibility()) {
                DownloadNotify.notifyPause(item)
            }

            addWaitToDownload()
        }

        override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_FAILED
            addWaitToDownload()
            if (ERR_URL_IS_TOO_OLD_THAN_LOACL == errorCode) {
                ToastUtil.showLong(mContext, "本机已有更高版本的${item.downloadTitle}，下载已取消")
            }
            mDownloadListenerList.forEach {
                it.onFail(errorCode, msg, item)
            }
            item.downloadListener?.let {
                it.onFail(errorCode, msg, item)
            }

            if (item.notificationVisibility()) {
                DownloadNotify.notifyFailed(item)
            }
        }

        override fun onComplete(filePath: String, item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
            if (item.fileLength < 1) {
                item.fileLength = File(filePath).length()
            }
            item.finished = item.fileLength
            if (FileMimeTypes.isApkFile(filePath)) {
                mContext?.packageManager?.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES)?.packageName?.let {
                    item.packageName = it
                }
            }
            if (item.canDownloadByPart()) {
                DownloadInfoDBManager.saveDownloadInfo(item)
            }
            addDownloadItemToList(item)
            item.finalFilePath = filePath
            addWaitToDownload()

            mDownloadListenerList.forEach {
                it.onComplete(filePath, item)
            }
            item.downloadListener?.let {
                it.onComplete(filePath, item)
            }

            if (item.notificationVisibility()) {
                DownloadNotify.notifyFinished(item)
            }

            if (item.isAutoInstall) {
                InstallUtils.installAPP(mContext, filePath)
            }
        }

        override fun onDelete(item: DownloadItem) {
            mDownloadListenerList.forEach {
                it.onDelete(item)
            }

            item.downloadListener?.let {
                it.onDelete(item)
            }

            if (item.notificationVisibility()) {
                DownloadNotify.notifyDelete(item)
            }
        }
    }

    private fun addWaitToDownload() {
        getWaitingTask().let { list ->
            if (list.isNotEmpty()) {
                list.first().let {
                    ThreadManager.getInstance().start {
                        startTask(it, it.isDownloadWhenAdd, it.isDownloadWhenUseMobile, false)
                    }
                }
            }
        }
    }

    private fun checkIsInstalledAndLocalVersionIsNew(info: DownloadItem): Boolean {
        ZLog.d("checkIsNeedDownload DownloadItem:$info")
        if (TextUtils.isEmpty(info.packageName)) {
            ZLog.d("packageName is bad ")
            return false
        }
        if (info.versionCode < 1) {
            ZLog.d("versionCode is bad ")
            return false
        }
        ZLog.d("checkIsNeedDownload versionCode:${info.versionCode}")
        return try {
            val packageInfo = APKUtils.getInstalledPackage(mContext, info.packageName)
            if (packageInfo != null) {
                ZLog.d("checkIsNeedDownload installVersionCode:${packageInfo.versionCode}")
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
        ZLog.d("checkIsDownloading DownloadItem:$info")
        if (TextUtils.isEmpty(info.packageName)) {
            ZLog.d("packageName is bad ")
            return false
        }
        if (info.versionCode < 1) {
            ZLog.d("versionCode is bad ")
            return false
        }
        ZLog.d("checkIsNeedDownload versionCode:${info.versionCode}")
        val alreadyDownloadItem = DownloadInfoDBManager.getDownloadInfoFromPackageName(info.packageName)
        return if (alreadyDownloadItem == null) {
            ZLog.d("checkIsNeedDownload alreadyDownloadItem null")
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

    private fun updateInfo(info: DownloadItem) {
        var savedInfo = if (info.canDownloadByPart()) {
            DownloadInfoDBManager.getDownloadInfo(info.downloadURL)
        } else {
            null
        }
        if (savedInfo != null) {
            info.finalFilePath = savedInfo.finalFilePath
            info.tempFilePath = savedInfo.tempFilePath
            if (savedInfo.fileLength > 0) {
                info.fileLength = savedInfo.fileLength
                info.finishedLengthBefore = DownloadInfoDBManager.getFinishedBefore(info.downloadID)
                info.finished = info.finishedLengthBefore
            }
        } else {
            info.finalFilePath = getFinalFileName(info.downloadURL, info.fileMD5, info.fileNameWithPath)
            info.tempFilePath = getDownladTempFilePath(info.downloadURL, info.fileMD5, info.fileNameWithPath)
        }
    }


    private fun checkBeforeDownloadFile(info: DownloadItem): String {

        if (FileUtils.checkFileExist(info.finalFilePath, info.fileLength, info.fileMD5, false)) {
            return info.finalFilePath
        }

        if (FileUtils.checkFileExist(info.tempFilePath, info.fileLength, info.fileMD5, false)) {
            info.finalFilePath = info.tempFilePath
            return info.tempFilePath
        }
        return ""
    }

    @Synchronized
    private fun startTask(info: DownloadItem, downloadAfterAdd: Boolean, downloadWhenUseMobile: Boolean, forceDownload: Boolean) {
        innerDownloadListener.onWait(info)
        if (downloadAfterAdd) {
            if (!isWifi()) {
                if (downloadWhenUseMobile) {
                    realStartTask(info, true, forceDownload)
                } else {
                    realStartTask(info, false, forceDownload)
                }
            } else {
                realStartTask(info, true, forceDownload)
            }
        } else {
            ZLog.d("startTask do nothing: $ $info ")
            realStartTask(info, false, forceDownload)
        }
    }

    private fun isWifi(): Boolean {
        return NetworkUtil.isWifiNet(mContext)
    }

    private fun realStartTask(info: DownloadItem, downloadAfterAdd: Boolean, forceDownload: Boolean) {
        ZLog.d("startTask:$info")
        try {
            // 不合法的URl
            if (!URLUtils.isHTTPUrl(info.downloadURL)) {
                ZLog.e("bad para:$info")
                innerDownloadListener.onFail(ERR_BAD_URL, "bad para", info)
                return
            }

            //本地已有更高版本
            if (checkIsInstalledAndLocalVersionIsNew(info)) {
                ZLog.e("no need download:$info")
                innerDownloadListener.onFail(ERR_URL_IS_TOO_OLD_THAN_LOACL, "install is new", info)
                return
            }

            // 正在下载更高版本
            if (checkIsDownloadingAndVersionIsNew(info)) {
                ZLog.e("noneed download:$info")
                innerDownloadListener.onFail(ERR_URL_IS_TOO_OLD_THAN_DOWNLOADING, "install is new", info)
                return
            }

            addDownloadItemToList(info)
            Thread {
                //本地已下载
                var filePath = checkBeforeDownloadFile(info)
                if (!TextUtils.isEmpty(filePath)) {
                    ZLog.e("has download:$info")
                    info.setDownloadStatus(DownloadStatus.STATUS_HAS_DOWNLOAD)
                    innerDownloadListener.onComplete(info.finalFilePath, info)
                } else {
                    if (downloadAfterAdd) {
                        var currentTime = System.currentTimeMillis()
                        if (currentTime - info.pauseTime < 3000L) {
                            ZLog.e("resume to quick:$info")
                            innerDownloadListener.onWait(info)
                            info.isDownloadWhenAdd = true
                            (info.pauseTime + 3000L - currentTime).let {
                                if (it > 0) {
                                    Thread.sleep(it)
                                }
                            }
                        }
                        mDownloadEngine.startDownload(mContext!!, info, forceDownload)
                    } else {
                        info.setPause()
                    }
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
            ZLog.e("download:$e")
        }
    }

    private fun addDownloadItemToList(info: DownloadItem) {
        ThreadManager.getInstance().start {
            if (info.canDownloadByPart()) {
                DownloadInfoDBManager.saveDownloadInfo(info)
            }
            DownloadTaskList.addToDownloadTaskList(info)
        }
    }

    fun getDownladTempFilePath(downloadURL: String, fileMD5: String, fileName: String): String {
        return getFilePath(downloadURL, fileMD5, fileName, "Temp_")
    }

    fun getFinalFileName(downloadURL: String, fileMD5: String, fileName: String): String {
        return getFilePath(downloadURL, fileMD5, fileName, "")
    }

    private fun getFilePath(downloadURL: String, fileMD5: String, fileName: String, prefix: String): String {
        return if (TextUtils.isEmpty(fileName)) {
            ZixieFileProvider.getZixieFilePath(mContext!!) + prefix + URLUtils.getFileName(downloadURL).let {
                if (TextUtils.isEmpty(it)) {
                    if (TextUtils.isEmpty(fileMD5)) {
                        System.currentTimeMillis()
                    } else {
                        fileMD5
                    }
                } else {
                    it
                }
            }
        } else {
            fileName
        }
    }


    fun addTask(info: DownloadItem, forceDownload: Boolean) {
        ZLog.d("addTask:$info")
        updateInfo(info)
        innerDownloadListener.onWait(info)
        if (info.isForceDownloadNew) {
            // 此前下载的文件不完整
            if (TextUtils.isEmpty(checkBeforeDownloadFile(info))) {
                deleteTask(info.downloadID, startByUser = false, deleteFile = true)
            }
        }
        if (DownloadTaskList.hadAddTask(info)) {
            ZLog.d("mDownloadList contains:$info")
            DownloadTaskList.updateDownloadTaskListItem(info)
            resumeTask(info.downloadID, info.downloadListener, info.isDownloadWhenAdd, info.isDownloadWhenUseMobile, forceDownload)
        } else {
            startTask(info, info.isDownloadWhenAdd, info.isDownloadWhenUseMobile, forceDownload)
        }
    }

    fun resumeTask(downloadId: Long, downloadListener: DownloadListener?, startByUser: Boolean, downloadWhenUseMobile: Boolean, forceDownload: Boolean) {
        DownloadTaskList.getTaskByDownloadID(downloadId)?.let { info ->
            ZLog.d("resumeTask:$info")
            if (startByUser) {
                info.isDownloadWhenAdd = true
            }
            downloadListener?.let {
                info.downloadListener = it
            }
            innerDownloadListener.onWait(info)
            startTask(info, startByUser, downloadWhenUseMobile, forceDownload)
        }
    }

    fun pauseTask(downloadId: Long, startByUser: Boolean, clearHistory: Boolean) {
        DownloadTaskList.getTaskByDownloadID(downloadId)?.let { info ->
            ZLog.d("pause:$info")
            ZLog.d("pause:$info")
            mDownloadEngine.closeDownload(info.downloadID, false, clearHistory)
            info.status = DownloadStatus.STATUS_DOWNLOAD_PAUSED
            info.setPause()

            if (startByUser) {
                innerDownloadListener.onPause(info)
            }
        }
    }

    fun deleteTask(downloadId: Long, startByUser: Boolean, deleteFile: Boolean) {
        DownloadTaskList.getTaskByDownloadID(downloadId)?.let { info ->
            if (info.status == DownloadStatus.STATUS_DOWNLOADING) {
                addWaitToDownload()
            }
            DownloadTaskList.removeFromDownloadTaskList(downloadId)
            info.status = DownloadStatus.STATUS_DOWNLOAD_DELETE
            mDownloadEngine.closeDownload(downloadId, false, deleteFile)
            DownloadInfoDBManager.clearDownloadInfoByID(info.downloadID)
            if (deleteFile) {
                mDownloadEngine.deleteFile(info)
            }
            DownloadNotify.notifyDelete(info)
            innerDownloadListener.onDelete(info)
        }
    }

    fun addDownloadListener(listener: DownloadListener) {
        if (!mDownloadListenerList.contains(listener)) {
            mDownloadListenerList.add(listener)
        }
    }

    fun removeDownloadListener(listener: DownloadListener) {
        mDownloadListenerList.remove(listener)
    }

    fun pauseAllTask(startByUser: Boolean) {
        ZLog.d("pauseAllTask")
        pauseWaitingTask(startByUser)
        pauseDownloadingTask(startByUser)
    }

    fun pauseDownloadingTask(startByUser: Boolean) {
        ZLog.d("pauseDownloadingTask")
        getDownloadingTask().forEach { pauseTask(it.downloadID, startByUser, false) }
    }

    fun pauseWaitingTask(startByUser: Boolean) {
        ZLog.d("pauseWaitingTask")
        getWaitingTask().forEach { pauseTask(it.downloadID, startByUser, false) }
    }

    fun resumeAllTask(pauseOnMobile: Boolean) {
        ZLog.d("resumeAllTask")
        resumePauseTask(pauseOnMobile)
        resumeFailedTask(pauseOnMobile)
    }

    fun resumeFailedTask(pauseOnMobile: Boolean) {
        ZLog.d("resumeFailedTask")
        getAllTask().filter { it.status == DownloadStatus.STATUS_DOWNLOAD_FAILED }.forEach {
            resumeTask(it.downloadID, it.downloadListener, true, pauseOnMobile, false)
        }
    }

    fun resumePauseTask(pauseOnMobile: Boolean) {
        ZLog.d("resumePauseTask")
        getAllTask().filter { it.status == DownloadStatus.STATUS_DOWNLOAD_PAUSED }.forEach {
            resumeTask(it.downloadID, it.downloadListener, true, pauseOnMobile, false)
        }
    }

    fun getAllTask(): List<DownloadItem> {
        return DownloadTaskList.getDownloadTasKList()
    }


    fun getFinishedTask(): List<DownloadItem> {
        return DownloadTaskList.getDownloadTasKList().filter { it.status == DownloadStatus.STATUS_DOWNLOAD_SUCCEED || it.status == DownloadStatus.STATUS_HAS_DOWNLOAD }.toList()
    }

    fun getDownloadingTask(): List<DownloadItem> {
        return DownloadTaskList.getDownloadTasKList().filter { DownloadingList.isDownloading(it) }.toList()
    }

    fun getWaitingTask(): List<DownloadItem> {
        return DownloadTaskList.getDownloadTasKList().filter { it.status == DownloadStatus.STATUS_DOWNLOAD_WAITING }.toList()
    }
}
