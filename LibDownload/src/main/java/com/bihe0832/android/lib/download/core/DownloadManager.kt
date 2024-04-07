package com.bihe0832.android.lib.download.core

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadItem.TAG
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.core.dabase.DownloadInfoDBManager
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.network.NetworkUtil
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.thread.ThreadManager

@SuppressLint("StaticFieldLeak")
abstract class DownloadManager {

    abstract fun getAllTask(): List<DownloadItem>
    abstract fun getTaskByDownloadID(downloadID: Long): DownloadItem?

    abstract fun addToDownloadTaskList(info: DownloadItem)
    abstract fun removeFromDownloadTaskList(downloadId: Long)

    abstract fun addWaitToDownload()

    internal var mContext: Context? = null
    internal val DEFAULT_MAX_NUM = 1
    internal val MAX_MAX_NUM = 5

    internal var mMaxNum = DEFAULT_MAX_NUM
    internal var mHasInit = false
    internal var mIsDebug = false

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
        init(context, DEFAULT_MAX_NUM, isDebug)
    }

    open fun init(context: Context, maxNum: Int, isDebug: Boolean = false) {
        initContext(context)
        mMaxNum = maxNum
        if (mMaxNum > MAX_MAX_NUM) {
            ZLog.e(
                TAG,
                "  \n !!!========================================  \n \n \n !!! zixie download: The max download mum is recommended less than 5 \n \n \n !!!========================================",
            )
        }
        mIsDebug = isDebug
        if (!mHasInit) {
            mHasInit = true
            DownloadInfoDBManager.init(context)
        }
    }

    private fun initContext(context: Context?) {
        context?.let {
            mContext = it
        }
    }

    fun checkBeforeDownloadFile(info: DownloadItem): String {
        ZLog.d(TAG, "本地文件是否完整检查开始: $info ")
        if (FileUtils.checkFileExist(info.filePath, info.contentLength, info.fileMD5, info.fileSHA256, false)) {
            return info.filePath
        }
        ZLog.d(TAG, "本地文件是否完整检查结束：$info ")
        return ""
    }

    fun isWifi(): Boolean {
        return NetworkUtil.isWifiNet(mContext)
    }

    fun updateInfo(info: DownloadItem, addFilePath: Boolean) {
        var savedInfo = DownloadInfoDBManager.getDownloadInfo(info.downloadURL, info.downloadActionKey)
        if (savedInfo != null) {
            info.filePath = savedInfo.filePath
            if (!TextUtils.isEmpty(savedInfo.realURL)) {
                info.realURL = savedInfo.realURL
            }
            if (savedInfo.contentLength > 0) {
                info.contentLength = savedInfo.contentLength
                info.finishedLengthBefore = DownloadInfoDBManager.getFinishedBefore(info.downloadID)
                info.finished = info.finishedLengthBefore
            }
        }
        val tempFilePathIsNull = TextUtils.isEmpty(info.filePath)
        if (tempFilePathIsNull && addFilePath) {
            val backFileName = if (!TextUtils.isEmpty(info.fileMD5)) {
                info.fileMD5
            } else if (!TextUtils.isEmpty(info.fileSHA256)) {
                info.fileSHA256
            } else {
                System.currentTimeMillis().toString()
            }
            info.filePath = getDownladTempFilePath(info.downloadURL, backFileName, info.fileFolder)
        }
    }

    internal fun addDownloadItemToList(info: DownloadItem) {
        ThreadManager.getInstance().start {
            DownloadInfoDBManager.saveDownloadInfo(info)
            addToDownloadTaskList(info)
        }
    }


    fun getDownladTempFilePath(downloadURL: String, backFileName: String, fileName: String): String {
        return getFilePath(downloadURL, backFileName, fileName, "Temp_")
    }

    fun getFinalFileName(downloadURL: String, backFileName: String, filePath: String): String {
        return getFilePath(downloadURL, backFileName, filePath, "")
    }

    fun getFilePath(downloadURL: String, backFileName: String, filePath: String, prefix: String): String {
        var folder = if (TextUtils.isEmpty(filePath)) {
            ZixieFileProvider.getZixieCacheFolder(mContext!!)
        } else {
            filePath
        }
        FileUtils.checkAndCreateFolder(folder)
        return FileUtils.getFolderPathWithSeparator(folder) + prefix + URLUtils.getFileName(downloadURL).let {
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

    fun getFinishedTask(): List<DownloadItem> {
        return getAllTask().filter { it.status == DownloadStatus.STATUS_DOWNLOAD_SUCCEED || it.status == DownloadStatus.STATUS_HAS_DOWNLOAD }
                .toList<DownloadItem>()
    }

    fun getDownloadingTask(): List<DownloadItem> {
        return DownloadingList.getDownloadingItemList().toList<DownloadItem>()
    }

    fun getWaitingTask(): List<DownloadItem> {
        return getAllTask().filter { it.status == DownloadStatus.STATUS_DOWNLOAD_WAITING }.toList<DownloadItem>()
    }
}
