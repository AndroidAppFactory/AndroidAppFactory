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
import com.bihe0832.android.lib.download.DownloadClientConfig
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
import com.bihe0832.android.lib.okhttp.wrapper.OkHttpClientManager
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.encrypt.messagedigest.MD5
import com.bihe0832.android.lib.utils.encrypt.messagedigest.SHA256
import okhttp3.Protocol
import okhttp3.Request
import java.net.HttpURLConnection

/**
 * 下载管理器基类
 *
 * 提供下载任务的统一管理功能，包括：
 * - 任务队列管理（等待、下载中、已完成）
 * - 网络状态监听与自动切换
 * - 下载状态控制（开始、暂停、恢复、删除）
 * - 数据库持久化管理
 * - 断点续传支持
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-10.
 * Description: 下载管理器核心抽象类，定义下载流程和公共逻辑
 */
@SuppressLint("StaticFieldLeak")
abstract class DownloadManager {

    companion object {
        /** 最大重试次数 */
        private const val MAX_RETRY_TIMES = 2

        /** 默认最大并发下载数 */
        private const val DEFAULT_MAX_NUM = 1

        /** 最大并发下载数上限（建议不超过5，避免资源竞争） */
        private const val MAX_MAX_NUM = 5

        /** 网络切换后延迟检查时间（毫秒），避免频繁触发 */
        private const val MSG_DELAY_START_CHECK = 3 * 1000L

        /** Handler 消息类型：开始检查下载任务 */
        private const val MSG_TYPE_START_CHECK = 1
    }

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
        protocol: Protocol,
        serverContentLength: Long,
        downloadAfterAdd: Boolean
    ): Boolean


    internal var context: Context? = null
    internal var maxNum = DEFAULT_MAX_NUM
    internal var hasInit = false
    internal var isDebug = false

    /** 下载配置实例，每个 DownloadManager 拥有独立配置 */
    internal var downloadClientConfig: DownloadClientConfig = DownloadClientConfig.createDefault()

    private var netReceiver: BroadcastReceiver? = null

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
        return context
    }

    fun isDebug(): Boolean {
        return isDebug
    }

    fun init(context: Context) {
        init(context, ZLog.isLogEnabled())
    }

    fun init(context: Context, isDebug: Boolean = false) {
        init(context, DEFAULT_MAX_NUM, DownloadClientConfig.createDefault(), isDebug)
    }

    fun hasInit(): Boolean {
        return hasInit
    }

    fun addNewTask(info: DownloadItem, downloadAfterAdd: Boolean) {
        startTask(info, downloadAfterAdd)
    }

    /**
     * 初始化下载管理器
     *
     * @param context 应用上下文
     * @param maxNum 最大并发下载数
     * @param downloadClientConfig 下载配置，用于设置 HTTP/2、分片策略等
     * @param isDebug 是否开启调试模式
     */
    open fun init(
        context: Context,
        maxNum: Int,
        downloadClientConfig: DownloadClientConfig = DownloadClientConfig.createDefault(),
        isDebug: Boolean = false
    ) {
        this.downloadClientConfig = downloadClientConfig
        // 使用 ApplicationContext 避免内存泄漏
        initContext(context.applicationContext)
        this.maxNum = maxNum
        if (this.maxNum > MAX_MAX_NUM) {
            ZLog.e(
                TAG,
                "  \n !!!========================================  \n \n \n !!! zixie download: The max download mum is recommended less than 5 \n \n \n !!!========================================"
            )
        }
        this.isDebug = isDebug
        if (!hasInit) {
            hasInit = true
            DownloadInfoDBManager.init(context, isDebug)
        }

        // 日志输出当前配置
        if (isDebug || downloadClientConfig.logProtocolInfo) {
            ZLog.d(TAG, "下载管理器初始化配置:")
            ZLog.d(TAG, "  - HTTP/2 支持: ${downloadClientConfig.enableHttp2}")
            ZLog.d(TAG, "  - HTTP/2 最大分片: ${downloadClientConfig.http2MaxChunks}")
            ZLog.d(TAG, "  - HTTP/1.1 最大分片: ${downloadClientConfig.http1MaxChunks}")
            ZLog.d(
                TAG,
                "  - HTTP/2 最小分片大小: ${FileUtils.getFileLength(downloadClientConfig.http2MinChunkSize.toLong())}"
            )
            ZLog.d(
                TAG,
                "  - HTTP/1.1 最小分片大小: ${FileUtils.getFileLength(downloadClientConfig.http1MinChunkSize.toLong())}"
            )
        }

        netReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                ZLog.d(TAG, "当前发生网络切换")
                msgHandler.sendEmptyMessageDelayed(MSG_TYPE_START_CHECK, MSG_DELAY_START_CHECK)
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(netReceiver, intentFilter)
    }

    private fun initContext(context: Context?) {
        context?.let {
            // 确保使用 Application Context
            this.context = if (it.applicationContext != null) {
                it.applicationContext
            } else {
                it
            }
        }
    }

    fun hasPauseAll(): Boolean {
        return hasPauseAll
    }

    open fun onDestroy() {
        // 安全注销广播接收器
        try {
            netReceiver?.let {
                context?.unregisterReceiver(it)
                netReceiver = null
            }
        } catch (e: IllegalArgumentException) {
            // 广播接收器可能已经被注销
            ZLog.w(TAG, "广播接收器注销失败，可能已被注销: ${e.message}")
        } catch (e: Exception) {
            ZLog.e(TAG, "注销广播接收器异常: ${e.message}")
        }

        // 暂停所有任务
        pauseAllTask(DownloadPauseType.PAUSED_BY_ALL, true)

        // 清空 Context 引用
        context = null
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

        ZLog.d(TAG, "本地文件是否完整检查结束，本地不存在：$info ")
        return ""
    }

    fun isMobileNet(): Boolean {
        return NetworkUtil.isMobileNet(context)
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
        // 直接使用原始 URL，让 OkHttp 自动处理重定向
        var requestURL = info.downloadURL
        do {
            times++
            try {
                ZLog.w(TAG, "获取文件长度 $times:$requestURL")


                val requestBuilder = Request.Builder()
                    .url(requestURL)
                    .addDownloadHeaders(info.requestHeader)
                    .apply {
                        // 分片下载时，添加 Range 头以获取正确的 contentLength
                        if (rangeLength > 0 && rangeStart >= 0) {
                            val rangeEnd = rangeStart + rangeLength - 1
                            addHeader("Range", "bytes=$rangeStart-$rangeEnd")
                        }
                    }
                    .get()  // 使用 GET 请求（与签名校验的 method 保持一致）

                val request = requestBuilder.build()
                val time = System.currentTimeMillis()

                // 调试模式下打印请求头
                if (isDebug) {
                    request.logRequestHeaderFields("获取文件长度")
                }

                val response =
                    OkHttpClientManager.executeRequest(request, downloadClientConfig.enableHttp2)

                // 从 response 获取最终 URL（重定向后的实际 URL）
                val realURL = response.request.url.toString()

                // 同时更新 DownloadItem 的 realURL 和协议信息
                info.realURL = realURL
                info.protocol = response.protocol

                // 记录原始 URL 和最终 URL 的协议版本
                OkHttpClientManager.recordProtocolForUrl(info.downloadURL, response.protocol)
                if (realURL != info.downloadURL) {
                    OkHttpClientManager.recordProtocolForUrl(realURL, response.protocol)
                }

                ZLog.e(
                    TAG,
                    "获取文件长度，请求用时: ${System.currentTimeMillis() - time}, 协议: ${response.protocol}, 原始URL: ${info.downloadURL}, 最终URL: $realURL ~~~~~~~~~~~~~"
                )

                // 调试模式下打印服务端返回的所有 header
                if (isDebug) {
                    response.logResponseHeaderFields("获取文件长度")
                }

                // 从 header 中读取 Content-Length，使用统一的扩展方法
                val contentLength = response.getContentLength()
                ZLog.e(TAG, "获取文件长度 getContentType:${response.body?.contentType()}")
                ZLog.e(TAG, "获取文件长度 getContentLength:${contentLength}")
                ZLog.e(TAG, "获取文件长度 protocol:${response.protocol}")
                ZLog.e(TAG, "计划下载的信息 rangeStart:${rangeStart}, rangeLength:${rangeLength}")
                ZLog.e(TAG, "获取文件长度 responseCode:${response.code}")

                if (response.code == HttpURLConnection.HTTP_OK || response.code == HttpURLConnection.HTTP_PARTIAL) {
                    val result = updateItemByServer(
                        info,
                        rangeStart,
                        rangeLength,
                        localStart,
                        realURL,
                        response.protocol,
                        contentLength,
                        downloadAfterAdd
                    )
                    response.close()
                    return result
                } else {
                    if (times > MAX_RETRY_TIMES) {
                        ZLog.e(
                            TAG,
                            "download with error file length after max times:${response.code} " + info
                        )
                        response.close()
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
                            "download with error file length :${response.code} " + info
                        )
                        response.close()
                        // 重试时仍使用原始 URL
                        requestURL = info.downloadURL
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
        // 安全获取 Context，添加空值检查
        val ctx = context ?: run {
            ZLog.e(TAG, "Context 为空，无法获取文件路径")
            return ""
        }

        val folder = if (TextUtils.isEmpty(fileFolder)) {
            ZixieFileProvider.getZixieCacheFolder(ctx)
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
        DownloadTaskList.getTaskByDownloadID(downloadId)?.let { info ->
            ZLog.d(TAG, "resumeTask:$info")
            if (startByUser) {
                info.isDownloadWhenAdd = true
            }
            info.isDownloadWhenUseMobile = downloadWhenUseMobile
            downloadListener?.let {
                info.downloadListener = it
            }
            // 如果任务已经在下载中，只更新 listener 后直接返回，避免无意义的操作
            if (DownloadingList.isDownloading(info)) {
                ZLog.d(TAG, "resumeTask: task is already downloading, just update listener")
                return@let
            }
            // 从数据库读取历史下载进度，而不是重置为 0
            info.finishedLengthBefore = DownloadInfoDBManager.getFinishedBefore(info.downloadID)
            info.finished = info.finishedLengthBefore
            ZLog.d(TAG, "resumeTask restore progress: finishedLengthBefore=${info.finishedLengthBefore}, finished=${info.finished}")
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
        // 先设置状态为暂停，避免 checkDownloadProcess 线程竞态触发 onProgress
        info.setPause(type)
        getDownloadEngine().closeDownload(info.downloadID, false, clearHistory)
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
