package com.bihe0832.android.lib.download.file.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.DownloadItem.TAG
import com.bihe0832.android.lib.download.DownloadPauseType
import com.bihe0832.android.lib.download.core.DownloadTaskList
import com.bihe0832.android.lib.download.file.DownloadFileManager
import com.bihe0832.android.lib.install.InstallUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.notification.DownloadNotifyManager
import com.bihe0832.android.lib.thread.ThreadManager

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2020/6/11.
 * Description: Description
 *
 */
object DownloadFileNotify {

    private const val NOTIFY_CHANNEL = "DOWNLOAD"
    private val downloadReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                ZLog.d(TAG, "user input:$intent")
                intent?.let {
                    val notificationId =
                        it.getIntExtra(DownloadNotifyManager.NOTIFICATION_ID_KEY, -1)
                    val action = it.getStringExtra(DownloadNotifyManager.ACTION_KEY)
                    val downloadURL = it.getStringExtra(DownloadNotifyManager.NOTIFICATION_URL_KEY)
                        ?: ""
                    ZLog.d(TAG, "[DownloadNotificationsManager] onReceive: $action")
                    when (action) {
                        DownloadNotifyManager.ACTION_RESUME -> {
                            DownloadTaskList.getTaskByDownloadURL(downloadURL, "")?.let { item ->
                                DownloadFileManager.resumeTask(
                                    item.downloadID,
                                    item.downloadListener,
                                    true,
                                    item.isDownloadWhenUseMobile
                                )
                            }
                        }

                        DownloadNotifyManager.ACTION_PAUSE -> {
                            DownloadTaskList.getTaskByDownloadURL(downloadURL, "")?.let { item ->
                                DownloadFileManager.pauseTask(
                                    item.downloadID,
                                    DownloadPauseType.PAUSED_BY_USER,
                                    clearHistory = false
                                )
                            }
                        }

                        DownloadNotifyManager.ACTION_DELETE -> {
                            DownloadTaskList.getTaskByDownloadURL(downloadURL, "")?.let { item ->
                                DownloadFileManager.deleteTask(
                                    item.downloadID,
                                    startByUser = false,
                                    deleteFile = false
                                )
                                notifyDelete(item)
                            }

                            ThreadManager.getInstance().start({
                                mApplicationContext?.let {
                                    DownloadNotifyManager.cancleNotify(it, notificationId)
                                }
                            }, 1)

                        }

                        DownloadNotifyManager.ACTION_INSTALL -> {
                            DownloadTaskList.getTaskByDownloadURL(downloadURL, "")?.let { item ->
                                InstallUtils.installAPP(context, item.filePath)
                                notifyDelete(item)
                            }
                        }

                        DownloadNotifyManager.ACTION_RETRY -> {
                            DownloadTaskList.getTaskByDownloadURL(downloadURL, "")?.let { item ->
                                DownloadFileManager.resumeTask(
                                    item.downloadID,
                                    item.downloadListener,
                                    startByUser = true,
                                    downloadWhenUseMobile = true
                                )
                                notifyDelete(item)
                            }
                        }

                        else -> {

                        }
                    }
                }
            }
        }
    }
    private var mApplicationContext: Context? = null

    fun init(context: Context) {
        mApplicationContext = context.applicationContext
        mApplicationContext?.registerReceiver(
            downloadReceiver,
            IntentFilter(DownloadNotifyManager.getDownloadBroadcastFilter(context))
        )
    }

    fun destroy() {
        mApplicationContext?.unregisterReceiver(downloadReceiver)
    }

    @Synchronized
    private fun notify(item: DownloadItem, type: Int) {
        ZLog.d(NOTIFY_CHANNEL, "notify: notifyID: type: $type, ${item.downloadTitle},   info $item")

        if (item.notificationVisibility()) {
            ThreadManager.getInstance().runOnUIThread {
                mApplicationContext?.let {

                    var realID = DownloadNotifyManager.sendDownloadNotify(
                        it,
                        item.downloadURL,
                        item.downloadTitle,
                        item.downloadIcon,
                        item.finished,
                        item.contentLength,
                        item.lastSpeed,
                        (item.process * 100).toInt(),
                        type,
                        NOTIFY_CHANNEL
                    )
                }
            }
        }
    }

    fun notifyProcess(item: DownloadItem) {
        notify(item, DownloadNotifyManager.DOWNLOAD_TYPE_DOWNLOADING)
    }

    fun notifyPause(item: DownloadItem) {
        notify(item, DownloadNotifyManager.DOWNLOAD_TYPE_PAUSED)
    }

    fun notifyFinished(item: DownloadItem) {
        notify(item, DownloadNotifyManager.DOWNLOAD_TYPE_FINISHED)
    }

    fun notifyFailed(item: DownloadItem) {
        notify(item, DownloadNotifyManager.DOWNLOAD_TYPE_FAILED)
    }

    fun notifyDelete(item: DownloadItem) {
        if (item.notificationVisibility()) {
            val notifyID = DownloadNotifyManager.getNotifyIDByURL(item.downloadURL)
            ZLog.d(TAG, "notify delete: id: $notifyID ${item.downloadTitle}")
            mApplicationContext?.let {
                DownloadNotifyManager.cancleNotify(it, notifyID)
            }
        }
    }
}