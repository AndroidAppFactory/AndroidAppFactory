/**
 * 文件下载管理器
 *
 * 继承自 DownloadManager，专门用于文件下载场景：
 * - 支持 APK 自动安装
 * - 支持通知栏进度展示
 * - 支持版本检查（避免下载旧版本）
 * - 支持 MD5/SHA256 校验
 *
 * @author zixie code@bihe0832.com
 * Created on 2020-01-10.
 * Description: 文件下载管理器，处理完整文件的下载逻辑
 */
package com.bihe0832.android.lib.download.file

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import com.bihe0832.android.lib.download.DownloadClientConfig
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
import com.bihe0832.android.lib.download.core.DownloadByHttpBase
import com.bihe0832.android.lib.download.core.DownloadExceptionAnalyzer
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
import okhttp3.Protocol
import java.io.File
import com.bihe0832.android.lib.aaf.res.R as ResR

@SuppressLint("StaticFieldLeak")
object DownloadFileManager : DownloadManager() {

    /** 快速恢复下载的最小间隔（毫秒），防止频繁重试导致服务器压力 */
    private const val MIN_RESUME_INTERVAL = 3000L

    /** 下载前的延迟时间（毫秒），用于状态同步 */
    private const val DOWNLOAD_START_DELAY = 1000L

    private val mDownloadEngine by lazy {
        DownloadByHttpForFile(innerDownloadListener, maxNum, isDebug, downloadClientConfig)
    }

    override fun init(
        context: Context,
        maxNum: Int,
        downloadClientConfig: DownloadClientConfig,
        isDebug: Boolean
    ) {
        super.init(context, maxNum, downloadClientConfig, isDebug)
        if (!hasInit) {
            hasInit = true
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
            // 如果已经暂停全部，不再触发进度回调（双重保护，避免竞态）
            if (hasPauseAll()) {
                ZLog.d(TAG, "onProgress skip because hasPauseAll")
                return
            }
            item.status = DownloadStatus.STATUS_DOWNLOADING
            item.downloadListener?.onProgress(item)
            if (item.notificationVisibility()) {
                DownloadFileNotify.notifyProcess(item)
            }
        }

        override fun onPause(item: DownloadItem, @DownloadPauseType pauseType: Int) {
            item.status = DownloadStatus.STATUS_DOWNLOAD_PAUSED
            item.downloadListener?.onPause(item, pauseType)
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
                getContext()?.let { context ->
                    ToastUtil.showLong(
                        context,
                        String.format(
                            context.getString(ResR.string.download_failed_local_is_new),
                            item.downloadTitle
                        )
                    )
                }
            }
            item.downloadListener?.onFail(errorCode, msg, item)
            if (item.notificationVisibility()) {
                DownloadFileNotify.notifyFailed(item)
            }
        }

        override fun onComplete(filePath: String, item: DownloadItem): String {
            item.status = DownloadStatus.STATUS_DOWNLOAD_SUCCEED
            // 下载成功，重置网络异常重试计数
            item.resetNetworkErrorRetryRound()
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
                context?.let {
                    item.packageName = APKUtils.getApkPackageName(it, filePath)
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
                    InstallUtils.installAPP(context, newPath)
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
            val packageInfo = APKUtils.getInstalledPackage(context, info.packageName)
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
                ZLog.e(TAG, "bad para downloadURL:$info")
                innerDownloadListener.onFail(ERR_BAD_URL, "bad para downloadURL", info)
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
                    info.status = DownloadStatus.STATUS_HAS_DOWNLOAD
                    Thread.sleep(DOWNLOAD_START_DELAY)
                    innerDownloadListener.onComplete(info.filePath, info)
                } else {
                    if (downloadAfterAdd) {
                        if (isMobileNet() && !info.isDownloadWhenUseMobile) {
                            ZLog.e(TAG, "当前网络为移动网络，任务暂停:$info")
                            pauseTask(
                                info,
                                DownloadPauseType.PAUSED_BY_NETWORK
                            )
                        } else {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - info.pauseTime < MIN_RESUME_INTERVAL) {
                                ZLog.e(TAG, "resume to quick:$info")
                                innerDownloadListener.onWait(info)
                                (info.pauseTime + MIN_RESUME_INTERVAL - currentTime).let {
                                    if (it > 0) {
                                        Thread.sleep(it)
                                    }
                                }
                            }
                            mDownloadEngine.startDownload(info)
                        }
                    } else {
                        ZLog.e(TAG, "download paused by downloadAfterAdd")
                        pauseTask(info, DownloadPauseType.PAUSED_PENDING_START)
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
        protocol: Protocol,
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
            info.protocol = protocol
            info.rangeStart = rangeStart
            info.contentLength = serverContentLength
            info.localStart = localStart
            info.isDownloadWhenAdd = downloadAfterAdd
            DownloadInfoDBManager.saveDownloadInfo(info)

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            ZLog.e(TAG, "download:$e")
            val internalErrorCode = DownloadExceptionAnalyzer.analyzeException(e)
            // 对外回调时收敛为旧版本错误码
            val externalErrorCode = DownloadExceptionAnalyzer.toExternalErrorCode(internalErrorCode)
            innerDownloadListener.onFail(
                externalErrorCode,
                "update server content exception: ${e.javaClass.simpleName}: ${e.message}",
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
                    info.status = DownloadStatus.STATUS_HAS_DOWNLOAD
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

    override fun getDownloadingTask(): List<DownloadItem> {
        return DownloadingList.getDownloadingItemList(DownloadItem.TYPE_FILE)
    }

    override fun getDownloadEngine(): DownloadByHttpBase {
        return mDownloadEngine
    }

    override fun getInnerDownloadListener(): DownloadListener {
        return innerDownloadListener
    }


}
