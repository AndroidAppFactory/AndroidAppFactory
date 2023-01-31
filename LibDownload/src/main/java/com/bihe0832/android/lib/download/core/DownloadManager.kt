package com.bihe0832.android.lib.download.core

import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadErrorCode.*
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadItem.TAG
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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList


object DownloadManager {

    private val mDownloadEngine by lazy {
        DownloadByHttp(mContext!!, mMaxNum, innerDownloadListener)
    }

    private var mContext: Context? = null
    private var mGlobalDownloadListenerList = CopyOnWriteArrayList<DownloadListener>()
    private var mTempDownloadListenerList = ConcurrentHashMap<Long, ArrayList<DownloadListener>>()
    private const val DEFAULT_MAX_NUM = 3
    private const val MAX_MAX_NUM = 5

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
        init(context, false)
    }

    fun init(context: Context, isDebug: Boolean = false) {
        init(context, DEFAULT_MAX_NUM, null, isDebug)
    }


    fun init(context: Context, maxNum: Int, listener: DownloadListener?, isDebug: Boolean = false) {
        initContext(context)
        mMaxNum = maxNum
        if (mMaxNum > MAX_MAX_NUM) {
            ZLog.e(TAG, "  \n !!!========================================  \n \n \n !!! zixie download: The max download mum is recommended less than 5 \n \n \n !!!========================================")
        }
        mIsDebug = isDebug
        if (!mHasInit) {
            mHasInit = true
            DownloadNotify.init(context)
            DownloadInfoDBManager.init(context)
        }
        listener?.let {
            if (!mGlobalDownloadListenerList.contains(it)) {
                mGlobalDownloadListenerList.add(it)
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
        mGlobalDownloadListenerList.clear()
    }

    private val innerDownloadListener = object : DownloadListener {
        override fun onWait(item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_WAITING
            DownloadNotify.notifyProcess(item)
            item.downloadListener?.let {
                it.onWait(item)
            }

            mTempDownloadListenerList[item.downloadID]?.forEach {
                it.onWait(item)
            }

            mGlobalDownloadListenerList.forEach {
                it.onWait(item)
            }
            DownloadInfoDBManager.saveDownloadInfo(item)
        }

        override fun onStart(item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_STARTED
            item.lastSpeed = 0
            DownloadNotify.notifyProcess(item)
            item.startTime = System.currentTimeMillis()

            item.downloadListener?.let {
                it.onStart(item)
            }

            mTempDownloadListenerList[item.downloadID]?.forEach {
                it.onStart(item)
            }

            mGlobalDownloadListenerList.forEach {
                it.onStart(item)
            }
            DownloadInfoDBManager.saveDownloadInfo(item)
        }

        override fun onProgress(item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOADING

            item.downloadListener?.let {
                it.onProgress(item)
            }

            mTempDownloadListenerList[item.downloadID]?.forEach {
                it.onProgress(item)
            }

            mGlobalDownloadListenerList.forEach {
                it.onProgress(item)
            }

            if (item.notificationVisibility()) {
                DownloadNotify.notifyProcess(item)
            }
        }

        override fun onPause(item: DownloadItem) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_PAUSED

            item.downloadListener?.let {
                it.onPause(item)
            }

            mTempDownloadListenerList[item.downloadID]?.forEach {
                it.onPause(item)
            }

            mGlobalDownloadListenerList.forEach {
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

            item.downloadListener?.let {
                it.onFail(errorCode, msg, item)
            }

            mTempDownloadListenerList[item.downloadID]?.forEach {
                it.onFail(errorCode, msg, item)
            }
            mTempDownloadListenerList.remove(item.downloadID)

            mGlobalDownloadListenerList.forEach {
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
                mContext?.packageManager?.getPackageArchiveInfo(
                        filePath,
                        PackageManager.GET_ACTIVITIES
                )?.packageName?.let {
                    item.packageName = it
                }
            }
            DownloadInfoDBManager.saveDownloadInfo(item)
            addDownloadItemToList(item)
            item.finalFilePath = filePath
            addWaitToDownload()

            item.downloadListener?.let {
                it.onComplete(filePath, item)
            }
            mTempDownloadListenerList[item.downloadID]?.forEach {
                it.onComplete(filePath, item)
            }
            mTempDownloadListenerList.remove(item.downloadID)

            mGlobalDownloadListenerList.forEach {
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

            item.downloadListener?.let {
                it.onDelete(item)
            }

            if (item.notificationVisibility()) {
                DownloadNotify.notifyDelete(item)
            }

            mGlobalDownloadListenerList.forEach {
                it.onDelete(item)
            }
        }
    }

    private fun addWaitToDownload() {
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

    private fun updateInfo(info: DownloadItem) {
        var savedInfo = DownloadInfoDBManager.getDownloadInfo(info.downloadURL)
        if (savedInfo != null) {
            info.finalFilePath = savedInfo.finalFilePath
            info.tempFilePath = savedInfo.tempFilePath
            if (!TextUtils.isEmpty(savedInfo.realURL)) {
                info.realURL = savedInfo.realURL
            }
            if (savedInfo.fileLength > 0) {
                info.fileLength = savedInfo.fileLength
                info.finishedLengthBefore = DownloadInfoDBManager.getFinishedBefore(info.downloadID)
                info.finished = info.finishedLengthBefore
            }
        }
        val finalFilePathIsNull = TextUtils.isEmpty(info.finalFilePath)
        val tempFilePathIsNull = TextUtils.isEmpty(info.tempFilePath)
        if (finalFilePathIsNull || tempFilePathIsNull) {
            val backFileName = if (!TextUtils.isEmpty(info.fileMD5)) {
                info.fileMD5
            } else if (!TextUtils.isEmpty(info.fileSHA256)) {
                info.fileSHA256
            } else {
                System.currentTimeMillis().toString()
            }

            if (finalFilePathIsNull) {
                info.finalFilePath =
                        getFinalFileName(info.downloadURL, backFileName, info.fileFolder)
            }

            if (tempFilePathIsNull) {
                info.tempFilePath =
                        getDownladTempFilePath(info.downloadURL, backFileName, info.fileFolder)
            }
        }
    }


    private fun checkBeforeDownloadFile(info: DownloadItem): String {

        if (FileUtils.checkFileExist(
                        info.finalFilePath,
                        info.fileLength,
                        info.fileMD5,
                        info.fileSHA256,
                        false
                )
        ) {
            return info.finalFilePath
        }

        if (FileUtils.checkFileExist(
                        info.tempFilePath,
                        info.fileLength,
                        info.fileMD5,
                        info.fileSHA256,
                        false
                )
        ) {
            info.finalFilePath = info.tempFilePath
            return info.tempFilePath
        }
        return ""
    }

    @Synchronized
    private fun startTask(
            info: DownloadItem,
            downloadAfterAdd: Boolean,
            downloadWhenUseMobile: Boolean
    ) {
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

    private fun isWifi(): Boolean {
        return NetworkUtil.isWifiNet(mContext)
    }

    private fun realStartTask(
            info: DownloadItem,
            downloadAfterAdd: Boolean
    ) {
        ZLog.d(TAG, "startTask:$info")
        try {
            // 不合法的URl
            if (!URLUtils.isHTTPUrl(info.downloadURL)) {
                ZLog.e(TAG, "bad para:$info")
                innerDownloadListener.onFail(ERR_BAD_URL, "bad para", info)
                return
            }

            //本地已有更高版本
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

            addDownloadItemToList(info)
            Thread {
                //本地已下载
                var filePath = checkBeforeDownloadFile(info)
                if (!TextUtils.isEmpty(filePath)) {
                    ZLog.e(TAG, "has download:$info")
                    info.setDownloadStatus(DownloadStatus.STATUS_HAS_DOWNLOAD)
                    innerDownloadListener.onComplete(info.finalFilePath, info)
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

    private fun addDownloadItemToList(info: DownloadItem) {
        ThreadManager.getInstance().start {
            DownloadInfoDBManager.saveDownloadInfo(info)
            DownloadTaskList.addToDownloadTaskList(info)
        }
    }

    fun getDownladTempFilePath(
            downloadURL: String,
            backFileName: String,
            fileName: String
    ): String {
        return getFilePath(downloadURL, backFileName, fileName, "Temp_")
    }

    fun getFinalFileName(downloadURL: String, backFileName: String, filePath: String): String {
        return getFilePath(downloadURL, backFileName, filePath, "")
    }

    private fun getFilePath(
            downloadURL: String,
            backFileName: String,
            filePath: String,
            prefix: String
    ): String {
        var folder = if (TextUtils.isEmpty(filePath)) {
            ZixieFileProvider.getZixieFilePath(mContext!!)
        } else {
            filePath
        }
        FileUtils.checkAndCreateFolder(folder)
        return FileUtils.getFolderPathWithSeparator(folder) + prefix + URLUtils.getFileName(
                downloadURL
        ).let {
            if (TextUtils.isEmpty(it)) {
                if (TextUtils.isEmpty(backFileName)) {
                    System.currentTimeMillis()
                } else {
                    backFileName
                }
            } else {
                it
            }
        }
    }


    fun addTask(info: DownloadItem) {
        ZLog.d(TAG, "addTask:$info")
        if (DownloadingList.isDownloading(info)) {
            val currentDownload = DownloadTaskList.getTaskByDownloadID(info.downloadID)
            if (!TextUtils.isEmpty(currentDownload?.fileMD5) && !info.fileMD5.equals(currentDownload?.fileMD5)) {
                info.downloadListener?.onFail(
                        ERR_MD5_BAD,
                        "new md5 is diff with current download",
                        info
                )
            } else if (!TextUtils.isEmpty(currentDownload?.fileSHA256) && !info.fileSHA256.equals(
                            currentDownload?.fileSHA256
                    )
            ) {
                info.downloadListener?.onFail(
                        ERR_MD5_BAD,
                        "new sha256 is diff with current download",
                        info
                )
            } else {
                info.downloadListener?.let {
                    if (mTempDownloadListenerList.containsKey(info.downloadID) && null != mTempDownloadListenerList[info.downloadID]) {
                        mTempDownloadListenerList[info.downloadID]!!.add(it)
                    } else {
                        mTempDownloadListenerList.put(
                                info.downloadID,
                                ArrayList<DownloadListener>().apply {
                                    add(it)
                                })
                    }
                }
            }
        } else {
            updateInfo(info)
            innerDownloadListener.onWait(info)
            if (info.isForceDownloadNew) {
                // 此前下载的文件不完整
                if (TextUtils.isEmpty(checkBeforeDownloadFile(info))) {
                    deleteTask(info.downloadID, startByUser = false, deleteFile = true)
                }
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
                startTask(info, info.isDownloadWhenAdd, info.isDownloadWhenUseMobile)
            }
        }
    }

    fun resumeTask(
            downloadId: Long,
            downloadListener: DownloadListener?,
            startByUser: Boolean,
            downloadWhenUseMobile: Boolean
    ) {
        DownloadTaskList.getTaskByDownloadID(downloadId)?.let { info ->
            ZLog.d(TAG, "resumeTask:$info")
            if (startByUser) {
                info.isDownloadWhenAdd = true
            }
            downloadListener?.let {
                info.downloadListener = it
            }
            innerDownloadListener.onWait(info)
            startTask(info, startByUser, downloadWhenUseMobile)
        }
    }

    fun pauseTask(downloadId: Long, startByUser: Boolean, clearHistory: Boolean) {
        DownloadTaskList.getTaskByDownloadID(downloadId)?.let { info ->
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
        DownloadTaskList.getTaskByDownloadID(downloadId)?.let { info ->
            var downloadListener = mTempDownloadListenerList.get(downloadId)?.first()
            if (null != downloadListener) {
                info.downloadListener.onDelete(info)
                info.downloadListener = downloadListener
            } else {
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
    }

    fun addGlobalDownloadListener(listener: DownloadListener) {
        if (!mGlobalDownloadListenerList.contains(listener)) {
            mGlobalDownloadListenerList.add(listener)
        }
    }

    fun removeGlobalDownloadListener(listener: DownloadListener) {
        mGlobalDownloadListenerList.remove(listener)
    }

    fun pauseAllTask(startByUser: Boolean) {
        ZLog.d(TAG, "pauseAllTask")
        pauseWaitingTask(startByUser)
        pauseDownloadingTask(startByUser)
    }

    fun pauseDownloadingTask(startByUser: Boolean) {
        ZLog.d(TAG, "pauseDownloadingTask")
        getDownloadingTask().forEach { pauseTask(it.downloadID, startByUser, false) }
    }

    fun pauseWaitingTask(startByUser: Boolean) {
        ZLog.d(TAG, "pauseWaitingTask")
        getWaitingTask().forEach { pauseTask(it.downloadID, startByUser, false) }
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

    fun getAllTask(): List<DownloadItem> {
        return DownloadTaskList.getDownloadTasKList()
    }


    fun getFinishedTask(): List<DownloadItem> {
        return DownloadTaskList.getDownloadTasKList()
                .filter { it.status == DownloadStatus.STATUS_DOWNLOAD_SUCCEED || it.status == DownloadStatus.STATUS_HAS_DOWNLOAD }
                .toList()
    }

    fun getDownloadingTask(): List<DownloadItem> {
        return DownloadTaskList.getDownloadTasKList().filter { DownloadingList.isDownloading(it) }
                .toList()
    }

    fun getWaitingTask(): List<DownloadItem> {
        return DownloadTaskList.getDownloadTasKList()
                .filter { it.status == DownloadStatus.STATUS_DOWNLOAD_WAITING }.toList()
    }
}
