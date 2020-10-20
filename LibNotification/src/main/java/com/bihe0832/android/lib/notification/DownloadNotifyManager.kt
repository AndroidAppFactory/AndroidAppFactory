package com.bihe0832.android.lib.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.text.TextUtils
import android.text.format.Formatter
import android.view.View
import android.widget.RemoteViews
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.IdGenerator
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import java.util.*

object DownloadNotifyManager {

    const val NOTIFICATION_BROADCAST_ACTION = "com.bihe0832.android.lib.notification.NotificationBroadcastAction"

    const val NOTIFICATION_ID_KEY = "notificationId"
    const val ACTION_KEY = "action"
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
    private fun getNotifyIDByURL(url: String): Int {
        var id = try {
            return if (mListID.containsValue(url)) {
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
        context.applicationContext.let { context ->
            val remoteViews = RemoteViews(context.getPackageName(), R.layout.download_notification)
            updateContent(remoteViews, context, appName, iconURL, finished, total, speed, process, downloadType, channelID, notifyID)
        }
        return notifyID
    }

    private fun updateContent(remoteViews: RemoteViews, context: Context, appName: String, iconURL: String, finished: Long, total: Long, speed: Long, process: Int, downloadType: Int, channelID: String, notifyID: Int) {
        ThreadManager.getInstance().runOnUIThread {
            if (!TextUtils.isEmpty(iconURL)) {
                Glide.with(context.applicationContext)
                        .asBitmap()
                        .load(iconURL)
                        .into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                remoteViews.setImageViewBitmap(R.id.iv_logo, resource)
                            }
                        })
            } else {
                remoteViews.setImageViewResource(R.id.iv_logo, R.mipmap.icon)
            }
            remoteViews.setTextViewText(R.id.tv_download_progress, Formatter.formatFileSize(context, finished) + "/" + Formatter.formatFileSize(context, total))
            remoteViews.setProgressBar(R.id.progress_bar_download, 100, process, false)

            when (downloadType) {

                DOWNLOAD_TYPE_DOWNLOADING -> {
                    remoteViews.setTextViewText(R.id.tv_title, "正在下载" + appName)
                    remoteViews.setTextViewText(R.id.tv_download_speed, Formatter.formatFileSize(context, speed) + "/s")
                    R.id.btn_restart.let {
                        remoteViews.setImageViewResource(it, R.mipmap.btn_pause)
                        remoteViews.setViewVisibility(it, View.VISIBLE)
                        remoteViews.setOnClickPendingIntent(it, getPendingIntent(context, notifyID, ACTION_PAUSE))
                    }
                    R.id.btn_cancel.let {
                        remoteViews.setImageViewResource(it, R.mipmap.btn_cancel)
                        remoteViews.setViewVisibility(it, View.VISIBLE)
                        remoteViews.setOnClickPendingIntent(it, getPendingIntent(context, notifyID, ACTION_DELETE))
                    }
                }

                DOWNLOAD_TYPE_PAUSED -> {
                    remoteViews.setTextViewText(R.id.tv_title, appName + "下载已暂停")
                    remoteViews.setTextViewText(R.id.tv_download_speed, "")
                    R.id.btn_restart.let {
                        remoteViews.setImageViewResource(it, R.mipmap.btn_restart)
                        remoteViews.setViewVisibility(it, View.VISIBLE)
                        remoteViews.setOnClickPendingIntent(it, getPendingIntent(context, notifyID, ACTION_RESUME))
                    }
                    R.id.btn_cancel.let {
                        remoteViews.setImageViewResource(it, R.mipmap.btn_cancel)
                        remoteViews.setViewVisibility(it, View.VISIBLE)
                        remoteViews.setOnClickPendingIntent(it, getPendingIntent(context, notifyID, ACTION_DELETE))
                    }
                }

                DOWNLOAD_TYPE_FAILED -> {
                    remoteViews.setTextViewText(R.id.tv_title, appName + "下载失败")
                    remoteViews.setTextViewText(R.id.tv_download_speed, "")
                    R.id.btn_restart.let {
                        remoteViews.setImageViewResource(it, R.mipmap.btn_restart)
                        remoteViews.setViewVisibility(it, View.VISIBLE)
                        remoteViews.setOnClickPendingIntent(it, getPendingIntent(context, notifyID, ACTION_RETRY))
                    }
                    R.id.btn_cancel.let {
                        remoteViews.setImageViewResource(it, R.mipmap.btn_cancel)
                        remoteViews.setViewVisibility(it, View.VISIBLE)
                        remoteViews.setOnClickPendingIntent(it, getPendingIntent(context, notifyID, ACTION_DELETE))
                    }
                }

                DOWNLOAD_TYPE_FINISHED -> {
                    remoteViews.setTextViewText(R.id.tv_title, appName + "下载完成")
                    remoteViews.setTextViewText(R.id.tv_download_speed, "")
                    remoteViews.setViewVisibility(R.id.btn_restart, View.GONE)
                    remoteViews.setOnClickPendingIntent(R.id.iv_layout, getPendingIntent(context, notifyID, ACTION_INSTALL))
                    remoteViews.setViewVisibility(R.id.btn_cancel, View.GONE)
                    remoteViews.setViewVisibility(R.id.btn_restart, View.GONE)
                }
            }
        }

        NotifyManager.sendNotifyNow(remoteViews, context, channelID, notifyID)
    }

    fun getPendingIntent(context: Context, notifyID: Int, action: String): PendingIntent {
        return PendingIntent.getBroadcast(context, mIntentID.generate(), Intent().apply {
            setAction(NOTIFICATION_BROADCAST_ACTION)
            putExtra(NOTIFICATION_ID_KEY, notifyID)
            putExtra(ACTION_KEY, action)
        }, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}


