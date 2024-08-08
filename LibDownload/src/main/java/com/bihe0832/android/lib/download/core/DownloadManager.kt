package com.bihe0832.android.lib.download.core

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadErrorCode
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadItem.TAG
import com.bihe0832.android.lib.download.DownloadListener
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.core.dabase.DownloadInfoDBManager
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.network.NetworkUtil
import com.bihe0832.android.lib.request.HTTPRequestUtils
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.thread.ThreadManager
import java.net.HttpURLConnection
import java.net.URL

@SuppressLint("StaticFieldLeak")
abstract class DownloadManager {
    private val MAX_RETRY_TIMES = 2

    abstract fun getAllTask(): List<DownloadItem>

    abstract fun getDownloadEngine(): DownloadByHttpBase

    abstract fun getInnerDownloadListener(): DownloadListener

    abstract fun startTask(info: DownloadItem, downloadAfterAdd: Boolean);
    abstract fun updateItemByServer(
        info: DownloadItem,
        rangeStart: Long,
        rangeLength: Long,
        localStart: Long,
        realURL: String,
        serverContentLength: Long,
        downloadAfterAdd: Boolean
    ): Boolean


    internal var mContext: Context? = null
    internal val DEFAULT_MAX_NUM = 1
    internal val MAX_MAX_NUM = 5

    internal var mMaxNum = DEFAULT_MAX_NUM
    internal var mHasInit = false
    internal var mIsDebug = false
    internal var mNetReceiver: BroadcastReceiver? = null

    private val MSG_TYPE_START_CHECK = 1
    private val MSG_DELAY_START_CHECK = 3 * 1000L

    private val msgHandler = object : Handler(ThreadManager.getInstance().getLooper(ThreadManager.LOOPER_TYPE_NORMAL)) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_TYPE_START_CHECK -> {
                    removeMessages(MSG_TYPE_START_CHECK)
                    checkDownloadWhenNetChanged()
                }
            }
        }
    }

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
            DownloadInfoDBManager.init(context, isDebug)
        }

        mNetReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                ZLog.d(TAG, "当前发生网络切换")
                msgHandler.sendEmptyMessageDelayed(MSG_TYPE_START_CHECK, MSG_DELAY_START_CHECK)
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(mNetReceiver, intentFilter)
    }

    private fun initContext(context: Context?) {
        context?.let {
            mContext = it
        }
    }

    open fun onDestroy() {
        mContext?.unregisterReceiver(mNetReceiver)
        pauseAllTask(startByUser = false, pauseMaxDownload = true)
    }

    fun checkDownloadWhenNetChanged() {
        if (isMobileNet()) {
            getDownloadingTask().forEach {
                if (!it.isDownloadWhenUseMobile) {
                    ZLog.e(TAG, "当前网络切换为移动网络，任务暂停:$it")
                    pauseTask(
                        it.downloadID, startByUser = false, clearHistory = false, pauseByNetwork = true
                    )
                }
            }
        } else {
            ZLog.d(TAG, "当前网络切换为非移动网络，任务尝试重启")
            addPauseByNetworkToDownload()
        }
    }

    fun checkBeforeDownloadFile(info: DownloadItem): String {
        ZLog.d(TAG, "本地文件是否完整检查开始: $info ")
        if (FileUtils.checkFileExist(info.filePath, info.contentLength, info.contentMD5, info.contentSHA256, false)) {
            return info.filePath
        }
        ZLog.d(TAG, "本地文件是否完整检查结束：$info ")
        return ""
    }

    fun isMobileNet(): Boolean {
        return NetworkUtil.isMobileNet(mContext)
    }

    fun updateInfo(info: DownloadItem, addFilePath: Boolean) {
        var savedInfo = DownloadInfoDBManager.getDownloadInfo(info.downloadURL, info.downloadActionKey)
        if (savedInfo != null) {
            info.filePath = savedInfo.filePath
            info.finishedLengthBefore = DownloadInfoDBManager.getFinishedBefore(info.downloadID)
            info.finished = info.finishedLengthBefore
        }
        val tempFilePathIsNull = TextUtils.isEmpty(info.filePath)
        if (tempFilePathIsNull && addFilePath) {
            val backFileName = if (!TextUtils.isEmpty(info.contentMD5)) {
                info.contentMD5
            } else if (!TextUtils.isEmpty(info.contentSHA256)) {
                info.contentSHA256
            } else {
                System.currentTimeMillis().toString()
            }
            info.filePath = getDownladTempFilePath(info.downloadURL, backFileName, info.fileFolder)
        }
    }

    fun updateDownItemByServerInfo(
        info: DownloadItem, rangeStart: Long, rangeLength: Long, localStart: Long, downloadAfterAdd: Boolean
    ): Boolean {
        ZLog.w(TAG, "updateDownItemByServerInfo:$info")
        // 重新启动，获取文件总长度
        var times = 0
        var realURL = HTTPRequestUtils.getRedirectUrl(info.downloadURL)
        do {
            times++
            try {
                ZLog.w(TAG, "获取文件长度 $times:$realURL")
                val url = URL(realURL)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    upateRequestInfo()
                }
                val time = System.currentTimeMillis()
                connection.connect()
                ZLog.e(TAG, "获取文件长度，请求用时: ${System.currentTimeMillis() - time} ~~~~~~~~~~~~~")
                if (mIsDebug) {
                    connection.logHeaderFields("获取文件长度")
                }
                val contentLength = HTTPRequestUtils.getContentLength(connection)
                ZLog.e(TAG, "获取文件长度 getContentType:${connection.contentType}")
                ZLog.e(TAG, "获取文件长度 getContentLength:${contentLength}")
                ZLog.e(TAG, "计划下载的信息 rangeStart:${rangeStart}, rangeLength:${rangeLength}")
                ZLog.e(TAG, "获取文件长度 responseCode:${connection.responseCode}")
                if (connection.responseCode == HttpURLConnection.HTTP_OK || connection.responseCode == HttpURLConnection.HTTP_PARTIAL) {
                    return updateItemByServer(
                        info, rangeStart, rangeLength, localStart, realURL, contentLength, downloadAfterAdd
                    )
                } else {
                    if (times > MAX_RETRY_TIMES) {
                        ZLog.e(
                            TAG, "download with error file length after max times:${connection.responseCode} " + info
                        )
                        //请求三次都失败在结束
                        getInnerDownloadListener().onFail(
                            DownloadErrorCode.ERR_HTTP_LENGTH_FAILED,
                            "download with error file length after max times",
                            info
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
                    getInnerDownloadListener().onFail(
                        DownloadErrorCode.ERR_HTTP_EXCEPTION,
                        "download with exception after three times:${e.javaClass.name}",
                        info
                    )
                    ZLog.e(TAG, "获取文件长度 异常: download with exception after three times:${e.javaClass.name}")
                    return false
                }
            }
        } while (true)
    }


    internal fun addDownloadItemToListAndSaveLocal(info: DownloadItem) {
        ThreadManager.getInstance().start {
            DownloadInfoDBManager.saveDownloadInfo(info)
            addToDownloadTaskList(info)
        }
    }


    fun getDownladTempFilePath(downloadURL: String, backFileName: String, fileFolder: String): String {
        return getFilePath(downloadURL, backFileName, fileFolder, "Temp_")
    }

    fun getFilePath(downloadURL: String, backFileName: String, fileFolder: String, prefix: String): String {
        var folder = if (TextUtils.isEmpty(fileFolder)) {
            ZixieFileProvider.getZixieCacheFolder(mContext!!)
        } else {
            fileFolder
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
                .toList()
    }

    fun getDownloadingTask(): List<DownloadItem> {
        return DownloadingList.getDownloadingItemList().toList<DownloadItem>()
    }

    fun getWaitingTask(): List<DownloadItem> {
        return getAllTask().filter { it.status == DownloadStatus.STATUS_DOWNLOAD_WAITING }.toList()
    }

    fun getPauseByNetworkTask(): List<DownloadItem> {
        return getAllTask().filter { it.status == DownloadStatus.STATUS_DOWNLOAD_PAUSED_BY_NETWORK }.toList()
    }

    fun addToDownloadTaskList(info: DownloadItem) {
        DownloadTaskList.addToDownloadTaskList(info)
    }

    fun closeDownloadAndRemoveRecord(item: DownloadItem) {
        getDownloadEngine()?.closeDownload(item.downloadID, finishDownload = true, true)
    }

    fun checkAndAddTaskFromList(taskList: List<DownloadItem>) {
        taskList.let { list ->
            if (list.isNotEmpty()) {
                list.maxByOrNull { it.downloadPriority }?.let {
                    ThreadManager.getInstance().start {
                        startTask(it, it.isDownloadWhenAdd)
                    }
                }
            }
        }
    }

    fun addWaitToDownload() {
        checkAndAddTaskFromList(getWaitingTask())
    }

    fun addPauseByNetworkToDownload() {
        checkAndAddTaskFromList(getPauseByNetworkTask())
    }


    fun resumeTask(
        downloadId: Long,
        downloadListener: DownloadListener?,
        startByUser: Boolean,
        downloadWhenUseMobile: Boolean,
    ) {
        DownloadTaskList.getTaskByDownloadID(downloadId)?.let { info ->
            ZLog.d(TAG, "resumeTask:$info")
            if (startByUser) {
                info.isDownloadWhenAdd = true
            }
            info.isDownloadWhenUseMobile = downloadWhenUseMobile
            downloadListener?.let {
                info.downloadListener = it
            }
            info.finished = 0
            info.finishedLengthBefore = 0
            getInnerDownloadListener().onWait(info)
            startTask(info, startByUser)
        }
    }

    fun pauseTask(downloadId: Long, startByUser: Boolean, clearHistory: Boolean, pauseByNetwork: Boolean) {
        DownloadTaskList.getTaskByDownloadID(downloadId)?.let { info ->
            pauseTask(info, startByUser, clearHistory, pauseByNetwork)
        }
    }

    fun pauseTask(info: DownloadItem, startByUser: Boolean, clearHistory: Boolean, pauseByNetwork: Boolean) {
        ZLog.d(TAG, "pause:$info")
        ZLog.d(TAG, "pause:$info")
        getDownloadEngine().closeDownload(info.downloadID, false, clearHistory)
        if (pauseByNetwork) {
            info.setPause(DownloadStatus.STATUS_DOWNLOAD_PAUSED_BY_NETWORK)
        } else {
            info.setPause(DownloadStatus.STATUS_DOWNLOAD_PAUSED)
        }
        if (startByUser) {
            getInnerDownloadListener().onPause(info)
        }
    }

    open fun deleteTask(downloadId: Long, startByUser: Boolean, deleteFile: Boolean) {
        getDownloadEngine().closeDownload(downloadId, finishDownload = true, clearHistory = true)
        DownloadTaskList.getTaskByDownloadID(downloadId)?.let { info ->
            if (info.status == DownloadStatus.STATUS_DOWNLOADING) {
                addWaitToDownload()
            }
            DownloadTaskList.removeFromDownloadTaskList(downloadId)
            info.status = DownloadStatus.STATUS_DOWNLOAD_DELETE

            if (deleteFile) {
                getDownloadEngine().deleteFile(info)
            }
            getInnerDownloadListener().onDelete(info)
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
                    pauseTask(it.downloadID, startByUser, clearHistory = false, pauseByNetwork = false)
                } else {
                    ZLog.e(TAG, "skip pause maxPriority download:$it")
                    ZLog.e(TAG, "skip pause maxPriority download:$it")
                }
            } else {
                pauseTask(it.downloadID, startByUser, clearHistory = false, pauseByNetwork = false)
            }
        }
    }

    fun pauseWaitingTask(startByUser: Boolean, pauseMaxDownload: Boolean) {
        ZLog.d(TAG, "pauseWaitingTask")
        getWaitingTask().forEach {
            if (it.downloadPriority == DownloadItem.MAX_DOWNLOAD_PRIORITY) {
                if (pauseMaxDownload) {
                    pauseTask(it.downloadID, startByUser, clearHistory = false, pauseByNetwork = false)
                } else {
                    ZLog.e(TAG, "skip pause maxPriority download:$it")
                    ZLog.e(TAG, "skip pause maxPriority download:$it")
                }
            } else {
                pauseTask(it.downloadID, startByUser, clearHistory = false, pauseByNetwork = false)
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
}
