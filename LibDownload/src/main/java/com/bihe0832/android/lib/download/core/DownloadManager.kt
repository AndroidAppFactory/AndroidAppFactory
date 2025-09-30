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
import com.bihe0832.android.lib.download.DownloadPauseType
import com.bihe0832.android.lib.download.DownloadStatus
import com.bihe0832.android.lib.download.core.dabase.DownloadInfoDBManager
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.provider.ZixieFileProvider
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.network.NetworkUtil
import com.bihe0832.android.lib.request.HTTPRequestUtils
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.encrypt.messagedigest.MD5
import com.bihe0832.android.lib.utils.encrypt.messagedigest.SHA256
import java.net.HttpURLConnection
import java.net.URL

@SuppressLint("StaticFieldLeak")
abstract class DownloadManager {

    private val MAX_RETRY_TIMES = 2

    // 是否一键暂停所有任务，暂停以后，不再新增下载，当前下载全部暂停
    private var hasPauseAll = false
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

    private val msgHandler =
        object : Handler(ThreadManager.getInstance().getLooper(ThreadManager.LOOPER_TYPE_NORMAL)) {
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

    fun hasInit(): Boolean {
        return mHasInit
    }

    fun addNewTask(info: DownloadItem, downloadAfterAdd: Boolean) {
        hasPauseAll = false
        startTask(info, downloadAfterAdd)
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

    fun hasPauseAll(): Boolean {
        return hasPauseAll
    }

    open fun onDestroy() {
        mContext?.unregisterReceiver(mNetReceiver)
        pauseAllTask(DownloadPauseType.PAUSED_BY_ALL, true)
    }

    fun checkDownloadWhenNetChanged() {
        if (isMobileNet()) {
            getDownloadingTask().forEach {
                if (!it.isDownloadWhenUseMobile) {
                    ZLog.e(TAG, "当前网络切换为移动网络，任务暂停:$it")
                    pauseTask(
                        it.downloadID,
                        DownloadPauseType.PAUSED_BY_NETWORK,
                        clearHistory = false
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
        if (info.downloadType == DownloadItem.TYPE_FILE) {
            if (FileUtils.checkFileExist(
                    info.filePath, info.contentLength, info.contentMD5, info.contentSHA256, false
                )
            ) {
                return info.filePath
            }
        } else if (info.downloadType == DownloadItem.TYPE_RANGE) {
            if (!TextUtils.isEmpty(info.contentMD5)) {
                val result = MD5.getFilePartMD5(info.filePath, info.localStart, info.contentLength)
                if (result.equals(info.contentMD5, ignoreCase = true)) {
                    return info.filePath
                }
            } else if (!TextUtils.isEmpty(info.contentSHA256)) {
                val result =
                    SHA256.getFilePartSHA256(info.filePath, info.localStart, info.contentLength)
                if (result.equals(info.contentSHA256, ignoreCase = true)) {
                    return info.filePath
                }
            }
        }

        ZLog.d(TAG, "本地文件是否完整检查结束：$info ")
        return ""
    }

    fun isMobileNet(): Boolean {
        return NetworkUtil.isMobileNet(mContext)
    }

    fun updateInfo(info: DownloadItem, addFilePath: Boolean) {
        val savedInfo =
            DownloadInfoDBManager.getDownloadInfo(info.downloadURL, info.downloadActionKey)
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
        info: DownloadItem,
        rangeStart: Long,
        rangeLength: Long,
        localStart: Long,
        downloadAfterAdd: Boolean
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
                    upateRequestInfo(info.requestHeader)
                    logRequestHeaderFields("获取文件长度")
                }
                val time = System.currentTimeMillis()
                connection.connect()
                ZLog.e(
                    TAG, "获取文件长度，请求用时: ${System.currentTimeMillis() - time} ~~~~~~~~~~~~~"
                )
                if (mIsDebug) {
                    connection.logResponseHeaderFields("获取文件长度")
                }
                val contentLength = HTTPRequestUtils.getContentLength(connection)
                ZLog.e(TAG, "获取文件长度 getContentType:${connection.contentType}")
                ZLog.e(TAG, "获取文件长度 getContentLength:${contentLength}")
                ZLog.e(TAG, "计划下载的信息 rangeStart:${rangeStart}, rangeLength:${rangeLength}")
                ZLog.e(TAG, "获取文件长度 responseCode:${connection.responseCode}")
                if (connection.responseCode == HttpURLConnection.HTTP_OK || connection.responseCode == HttpURLConnection.HTTP_PARTIAL) {
                    return updateItemByServer(
                        info,
                        rangeStart,
                        rangeLength,
                        localStart,
                        realURL,
                        contentLength,
                        downloadAfterAdd
                    )
                } else {
                    if (times > MAX_RETRY_TIMES) {
                        ZLog.e(
                            TAG,
                            "download with error file length after max times:${connection.responseCode} " + info
                        )
                        //请求三次都失败在结束
                        getInnerDownloadListener().onFail(
                            DownloadErrorCode.ERR_HTTP_LENGTH_FAILED,
                            "download with error file length after max times",
                            info
                        )
                        return false
                    } else {
                        ZLog.e(
                            TAG,
                            "download with error file length :${connection.responseCode} " + info
                        )
                        realURL = HTTPRequestUtils.getRedirectUrl(info.downloadURL)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ZLog.e(
                    TAG, "获取文件长度 异常: download with exception: $times ${e.javaClass.name}"
                )
                if (times > MAX_RETRY_TIMES) {
                    //累积请求三次都失败在结束
                    getInnerDownloadListener().onFail(
                        DownloadErrorCode.ERR_HTTP_EXCEPTION,
                        "download with exception after three times:${e.javaClass.name}",
                        info
                    )
                    ZLog.e(
                        TAG,
                        "获取文件长度 异常: download with exception after three times:${e.javaClass.name}"
                    )
                    return false
                }
            }
        } while (true)
    }


    internal fun addDownloadItemToListAndSaveRecord(info: DownloadItem) {
        ThreadManager.getInstance().start {
            if (info.isNeedRecord) {
                DownloadInfoDBManager.saveDownloadInfo(info)
            }
            addToDownloadTaskList(info)
        }
    }


    fun getDownladTempFilePath(
        downloadURL: String, backFileName: String, fileFolder: String
    ): String {
        return getFilePath(downloadURL, backFileName, fileFolder, "Temp_")
    }

    fun getFilePath(
        downloadURL: String, backFileName: String, fileFolder: String, prefix: String
    ): String {
        var folder = if (TextUtils.isEmpty(fileFolder)) {
            ZixieFileProvider.getZixieCacheFolder(mContext!!)
        } else {
            fileFolder
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
        return getAllTask().filter { it.status == DownloadStatus.STATUS_DOWNLOAD_PAUSED && it.pauseType == DownloadPauseType.PAUSED_BY_NETWORK }
            .toList()
    }

    fun addToDownloadTaskList(info: DownloadItem) {
        DownloadTaskList.addToDownloadTaskList(info)
    }

    fun closeDownloadAndRemoveRecord(item: DownloadItem) {
        getDownloadEngine().closeDownload(item.downloadID, true, !item.isNeedRecord)
    }

    fun checkAndAddTaskFromList(taskList: List<DownloadItem>) {
        taskList.let { list ->
            if (list.isNotEmpty()) {
                list.maxByOrNull { it.downloadPriority }?.let {
                    ThreadManager.getInstance().start {
                        if (!hasPauseAll()) {
                            startTask(it, it.isDownloadWhenAdd)
                        }
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
        hasPauseAll = false
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

    fun pauseTask(downloadId: Long, type: Int, clearHistory: Boolean) {
        DownloadTaskList.getTaskByDownloadID(downloadId)?.let { info ->
            pauseTask(info, type, clearHistory)
        }
    }

    fun pauseTask(info: DownloadItem, type: Int, clearHistory: Boolean) {
        ZLog.d(TAG, "pause:$type $clearHistory ${info.downloadURL}")
        ZLog.d(TAG, "pause:$info")
        getDownloadEngine().closeDownload(info.downloadID, false, clearHistory)
        info.setPause(type)
        when (type) {
            DownloadPauseType.PAUSED_BY_NETWORK -> {

            }

            else -> {
                getInnerDownloadListener().onPause(info)
            }
        }
    }

    open fun deleteTask(downloadId: Long, startByUser: Boolean, deleteFile: Boolean) {
        getDownloadEngine().closeDownload(
            downloadId,
            finishDownload = true,
            clearDownloadHistory = true
        )
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

    fun pauseAllTask(type: Int, pauseMaxDownload: Boolean) {
        hasPauseAll = true
        ZLog.d(TAG, "pauseAllTask:$type")
        pauseWaitingTask(type, pauseMaxDownload)
        pauseDownloadingTask(type, pauseMaxDownload)
    }

    fun pauseDownloadingTask(type: Int, pauseMaxDownload: Boolean) {
        ZLog.d(TAG, "pauseDownloadingTask")
        getDownloadingTask().forEach {
            if (it.downloadPriority == DownloadItem.MAX_DOWNLOAD_PRIORITY) {
                if (pauseMaxDownload) {
                    pauseTask(it.downloadID, type, clearHistory = false)
                } else {
                    ZLog.e(TAG, "skip pause maxPriority download:$it")
                    ZLog.e(TAG, "skip pause maxPriority download:$it")
                }
            } else {
                pauseTask(it.downloadID, type, clearHistory = false)
            }
        }
    }

    fun pauseWaitingTask(type: Int, pauseMaxDownload: Boolean) {
        ZLog.d(TAG, "pauseWaitingTask")
        getWaitingTask().forEach {
            if (it.downloadPriority == DownloadItem.MAX_DOWNLOAD_PRIORITY) {
                if (pauseMaxDownload) {
                    pauseTask(it.downloadID, type, clearHistory = false)
                } else {
                    ZLog.e(TAG, "skip pause maxPriority download:$it")
                    ZLog.e(TAG, "skip pause maxPriority download:$it")
                }
            } else {
                pauseTask(it.downloadID, type, clearHistory = false)
            }
        }
    }

    fun resumeAllTask(pauseOnMobile: Boolean) {
        ZLog.d(TAG, "resumeAllTask")
        hasPauseAll = false
        resumePauseTask(pauseOnMobile)
        resumeFailedTask(pauseOnMobile)
    }

    fun resumeFailedTask(pauseOnMobile: Boolean) {
        ZLog.d(TAG, "resumeFailedTask")
        hasPauseAll = false
        getAllTask().filter { it.status == DownloadStatus.STATUS_DOWNLOAD_FAILED }.forEach {
            resumeTask(it.downloadID, it.downloadListener, true, pauseOnMobile)
        }
    }

    fun resumePauseTask(pauseOnMobile: Boolean) {
        ZLog.d(TAG, "resumePauseTask")
        hasPauseAll = false
        getAllTask().filter { it.status == DownloadStatus.STATUS_DOWNLOAD_PAUSED }.forEach {
            resumeTask(it.downloadID, it.downloadListener, true, pauseOnMobile)
        }
    }
}
