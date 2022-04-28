package com.bihe0832.android.lib.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import android.text.TextUtils
import android.text.format.Formatter
import android.view.View
import android.widget.RemoteViews
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.image.BitmapUtil
import com.bihe0832.android.lib.utils.IdGenerator
import com.bihe0832.android.lib.utils.os.DisplayUtil

object DownloadNotifyManager {

    private const val NOTIFICATION_BROADCAST_ACTION = "com.bihe0832.android.lib.notification.NotificationBroadcastAction"

    const val NOTIFICATION_ID_KEY = "notificationId"
    const val ACTION_KEY = "action"
    const val NOTIFICATION_URL_KEY = "downloadURL"

    const val ACTION_RESUME = "resume"
    const val ACTION_PAUSE = "pause"
    const val ACTION_DELETE = "delete"
    const val ACTION_RETRY = "retry"
    const val ACTION_INSTALL = "install"

    const val DOWNLOAD_TYPE_DOWNLOADING = 1
    const val DOWNLOAD_TYPE_PAUSED = 2
    const val DOWNLOAD_TYPE_FAILED = 3
    const val DOWNLOAD_TYPE_FINISHED = 4

    private val mListID by lazy {
        HashMap<Int, String>()
    }
    private val mNotifyID = IdGenerator(1)
    private val mIntentID = IdGenerator(1)

    fun cancleNotify(context: Context, noticeID: Int) {
        NotifyManager.cancleNotify(context, noticeID)
    }

    @Synchronized
    fun getNotifyIDByURL(url: String): Int {
        var id = try {
            if (mListID.containsValue(url)) {
                mListID.filter { it.value == url }.keys.first()
            } else {
                mNotifyID.generate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mNotifyID.generate()
        }
        mListID[id] = url
        return id
    }

    fun sendDownloadNotify(context: Context, downloadURL: String, appName: String, iconURL: String, finished: Long, total: Long, speed: Long, process: Int, downloadType: Int, channelID: String): Int {
        return sendDownloadNotify(context, downloadURL, appName, iconURL, finished, total, speed, process, downloadType, channelID, 0)
    }

    fun sendDownloadNotify(context: Context, downloadURL: String, appName: String, iconURL: String, finished: Long, total: Long, speed: Long, process: Int, downloadType: Int, channelID: String, notifyIDFromParam: Int): Int {

        var notifyID = if (notifyIDFromParam < 1) {
            getNotifyIDByURL(downloadURL)
        } else {
            notifyIDFromParam
        }
        ThreadManager.getInstance().runOnUIThread {
            context.applicationContext.let { context ->
                val remoteViews = RemoteViews(context.getPackageName(), R.layout.com_bihe0832_download_notification)
                updateContent(remoteViews, context, downloadURL, appName, iconURL, finished, total, speed, process, downloadType, channelID, notifyID)
            }
        }
        return notifyID
    }

    private fun updateContent(remoteViews: RemoteViews, context: Context, downloadURL: String, appName: String, iconURL: String, finished: Long, total: Long, speed: Long, process: Int, downloadType: Int, channelID: String, notifyID: Int) {

        remoteViews.setTextViewText(R.id.tv_download_progress, Formatter.formatFileSize(context, finished) + "/" + Formatter.formatFileSize(context, total))
        remoteViews.setProgressBar(R.id.progress_bar_download, 100, process, false)

        when (downloadType) {

            DOWNLOAD_TYPE_DOWNLOADING -> {
                remoteViews.setTextViewText(R.id.tv_title, "正在下载" + appName)
                remoteViews.setTextViewText(R.id.tv_download_speed, Formatter.formatFileSize(context, speed) + "/s")
                R.id.btn_restart.let {
                    remoteViews.setImageViewResource(it, R.mipmap.btn_pause)
                    remoteViews.setViewVisibility(it, View.VISIBLE)
                    remoteViews.setOnClickPendingIntent(it, getPendingIntent(context, downloadURL, notifyID, ACTION_PAUSE))
                }
                R.id.btn_cancel.let {
                    remoteViews.setImageViewResource(it, R.mipmap.btn_cancel)
                    remoteViews.setViewVisibility(it, View.VISIBLE)
                    remoteViews.setOnClickPendingIntent(it, getPendingIntent(context, downloadURL, notifyID, ACTION_DELETE))
                }
            }

            DOWNLOAD_TYPE_PAUSED -> {
                remoteViews.setTextViewText(R.id.tv_title, appName + "下载已暂停")
                remoteViews.setTextViewText(R.id.tv_download_speed, "")
                R.id.btn_restart.let {
                    remoteViews.setImageViewResource(it, R.mipmap.btn_restart)
                    remoteViews.setViewVisibility(it, View.VISIBLE)
                    remoteViews.setOnClickPendingIntent(it, getPendingIntent(context, downloadURL, notifyID, ACTION_RESUME))
                }
                R.id.btn_cancel.let {
                    remoteViews.setImageViewResource(it, R.mipmap.btn_cancel)
                    remoteViews.setViewVisibility(it, View.VISIBLE)
                    remoteViews.setOnClickPendingIntent(it, getPendingIntent(context, downloadURL, notifyID, ACTION_DELETE))
                }
            }

            DOWNLOAD_TYPE_FAILED -> {
                remoteViews.setTextViewText(R.id.tv_title, appName + "下载失败")
                remoteViews.setTextViewText(R.id.tv_download_speed, "")
                R.id.btn_restart.let {
                    remoteViews.setImageViewResource(it, R.mipmap.btn_restart)
                    remoteViews.setViewVisibility(it, View.VISIBLE)
                    remoteViews.setOnClickPendingIntent(it, getPendingIntent(context, downloadURL, notifyID, ACTION_RETRY))
                }
                R.id.btn_cancel.let {
                    remoteViews.setImageViewResource(it, R.mipmap.btn_cancel)
                    remoteViews.setViewVisibility(it, View.VISIBLE)
                    remoteViews.setOnClickPendingIntent(it, getPendingIntent(context, downloadURL, notifyID, ACTION_DELETE))
                }
            }

            DOWNLOAD_TYPE_FINISHED -> {
                remoteViews.setTextViewText(R.id.tv_title, appName + "下载完成")
                remoteViews.setTextViewText(R.id.tv_download_speed, "")
                remoteViews.setViewVisibility(R.id.btn_restart, View.GONE)
                remoteViews.setOnClickPendingIntent(R.id.iv_layout, getPendingIntent(context, downloadURL, notifyID, ACTION_INSTALL))
                remoteViews.setViewVisibility(R.id.btn_cancel, View.GONE)
                remoteViews.setViewVisibility(R.id.btn_restart, View.GONE)
            }
        }

        if (!TextUtils.isEmpty(iconURL)) {
            ThreadManager.getInstance().start {
                var bitmap = BitmapUtil.getRemoteBitmap(iconURL, DisplayUtil.dip2px(context.applicationContext, 40f), DisplayUtil.dip2px(context.applicationContext, 40f))
                if (null == bitmap) {
                    try {
                        bitmap = BitmapFactory.decodeResource(context.applicationContext.resources, R.mipmap.icon)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }

                bitmap?.let { bitmap ->
                    ThreadManager.getInstance().runOnUIThread {
                        remoteViews.setImageViewBitmap(R.id.iv_logo, BitmapUtil.getBitmapWithRound(bitmap, bitmap.width * 0.15f))
                        sendNotify(remoteViews, context, channelID, notifyID)
                    }
                }
            }
        } else {
            remoteViews.setImageViewResource(R.id.iv_logo, R.mipmap.icon)
            sendNotify(remoteViews, context, channelID, notifyID)
        }
    }

    private fun sendNotify(remoteViews: RemoteViews, context: Context, channelID: String, notifyID: Int) {
        var notification = NotificationCompat.Builder(context, channelID).apply {
            setOnlyAlertOnce(true)
            setContent(remoteViews)
            //设置小图标
            setSmallIcon(R.mipmap.icon)
            //禁止用户点击删除按钮删除
            setAutoCancel(false)
            //禁止滑动删除
            setOngoing(false)
            //取消右上角的时间显示
            setShowWhen(false)
        }.build()

        NotifyManager.sendNotifyNow(context, channelID, notification, notifyID)
    }

    fun getDownloadBroadcastFilter(context: Context): String {
        return context.packageName + "." + NOTIFICATION_BROADCAST_ACTION
    }

    fun getPendingIntent(context: Context, url: String, notifyID: Int, action: String): PendingIntent {
        return PendingIntent.getBroadcast(context, mIntentID.generate(), Intent().apply {
            setAction(getDownloadBroadcastFilter(context))
            putExtra(NOTIFICATION_ID_KEY, notifyID)
            putExtra(ACTION_KEY, action)
            putExtra(NOTIFICATION_URL_KEY, url)
        }, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}


