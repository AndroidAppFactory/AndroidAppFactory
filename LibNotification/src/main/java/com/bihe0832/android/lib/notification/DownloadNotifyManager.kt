package com.bihe0832.android.lib.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.view.View
import android.widget.RemoteViews
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.file.format.Formatter
import com.bihe0832.android.lib.media.image.bitmap.BitmapTransUtils
import com.bihe0832.android.lib.media.image.bitmap.BitmapUtil
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.IdGenerator
import com.bihe0832.android.lib.utils.intent.PendingIntentUtils
import com.bihe0832.android.lib.utils.os.DisplayUtil
import java.util.concurrent.ConcurrentHashMap

object DownloadNotifyManager {

    private const val NOTIFICATION_BROADCAST_ACTION =
        "com.bihe0832.android.lib.notification.NotificationBroadcastAction"

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

    private val mIconList by lazy {
        ConcurrentHashMap<Int, Bitmap>()
    }

    private val mIntentID = IdGenerator(1)

    fun cancleNotify(context: Context, noticeID: Int) {
        NotifyManager.cancelNotify(context, noticeID)
        mIconList.remove(noticeID)
    }

    @Synchronized
    fun getNotifyIDByURL(url: String): Int {
        return if (TextUtils.isEmpty(url)) {
            NotifyManager.getNotifyIDByKey("")
        } else {
            NotifyManager.getNotifyIDByKey("DownloadNotifyManager$url")
        }
    }

    fun sendDownloadNotify(
        context: Context,
        downloadURL: String,
        appName: String,
        iconURL: String,
        finished: Long,
        total: Long,
        speed: Long,
        process: Int,
        downloadType: Int,
        channelID: String,
    ): Int {
        return sendDownloadNotify(
            context,
            downloadURL,
            appName,
            iconURL,
            finished,
            total,
            speed,
            process,
            downloadType,
            channelID,
            0,
        )
    }

    fun sendDownloadNotify(
        context: Context,
        downloadURL: String,
        appName: String,
        iconURL: String,
        finished: Long,
        total: Long,
        speed: Long,
        process: Int,
        downloadType: Int,
        channelID: String,
        notifyIDFromParam: Int,
    ): Int {
        var notifyID = if (notifyIDFromParam < 1) {
            getNotifyIDByURL(downloadURL)
        } else {
            notifyIDFromParam
        }
        ThreadManager.getInstance().runOnUIThread {
            context.applicationContext.let { context ->
                val remoteViews = RemoteViews(
                    context.getPackageName(),
                    R.layout.com_bihe0832_download_notification
                )
                updateContent(
                    remoteViews,
                    context,
                    downloadURL,
                    appName,
                    iconURL,
                    finished,
                    total,
                    speed,
                    process,
                    downloadType,
                    channelID,
                    notifyID,
                )
            }
        }
        return notifyID
    }

    private fun updateContent(
        remoteViews: RemoteViews,
        context: Context,
        downloadURL: String,
        appName: String,
        iconURL: String,
        finished: Long,
        total: Long,
        speed: Long,
        process: Int,
        downloadType: Int,
        channelID: String,
        notifyID: Int,
    ) {
        remoteViews.setTextViewText(
            R.id.download_notification_download_progress,
            FileUtils.getFileLength(finished) + "/" + FileUtils.getFileLength(total),
        )
        remoteViews.setProgressBar(R.id.download_notification_progress_bar, 100, process, false)

        when (downloadType) {
            DOWNLOAD_TYPE_DOWNLOADING -> {
                remoteViews.setTextViewText(
                    R.id.download_notification_title,
                    context.getString(R.string.download_notify_downloading) + appName
                )
                if (speed > 0) {
                    remoteViews.setTextViewText(
                        R.id.download_notification_desc,
                        Formatter.formatFileSize(context, speed) + "/s",
                    )
                }
                R.id.download_notification_btn_restart.let {
                    remoteViews.setImageViewResource(it, R.drawable.icon_pause_fill)
                    remoteViews.setViewVisibility(it, View.VISIBLE)
                    remoteViews.setOnClickPendingIntent(
                        it,
                        getPendingIntent(context, downloadURL, notifyID, ACTION_PAUSE),
                    )
                }
                R.id.download_notification_btn_cancel.let {
                    remoteViews.setImageViewResource(it, R.drawable.icon_close_fill)
                    remoteViews.setViewVisibility(it, View.VISIBLE)
                    remoteViews.setOnClickPendingIntent(
                        it,
                        getPendingIntent(context, downloadURL, notifyID, ACTION_DELETE),
                    )
                }
            }

            DOWNLOAD_TYPE_PAUSED -> {
                remoteViews.setTextViewText(
                    R.id.download_notification_title,
                    appName + context.getString(R.string.download_notify_download_paused)
                )
                remoteViews.setTextViewText(R.id.download_notification_desc, "")
                R.id.download_notification_btn_restart.let {
                    remoteViews.setImageViewResource(it, R.drawable.icon_start_fill)
                    remoteViews.setViewVisibility(it, View.VISIBLE)
                    remoteViews.setOnClickPendingIntent(
                        it,
                        getPendingIntent(context, downloadURL, notifyID, ACTION_RESUME),
                    )
                }
                R.id.download_notification_btn_cancel.let {
                    remoteViews.setImageViewResource(it, R.drawable.icon_close_fill)
                    remoteViews.setViewVisibility(it, View.VISIBLE)
                    remoteViews.setOnClickPendingIntent(
                        it,
                        getPendingIntent(context, downloadURL, notifyID, ACTION_DELETE),
                    )
                }
            }

            DOWNLOAD_TYPE_FAILED -> {
                remoteViews.setTextViewText(
                    R.id.download_notification_title,
                    appName + context.getString(R.string.download_notify_download_failed)
                )
                remoteViews.setTextViewText(R.id.download_notification_desc, "")
                R.id.download_notification_btn_restart.let {
                    remoteViews.setImageViewResource(it, R.drawable.icon_start_fill)
                    remoteViews.setViewVisibility(it, View.VISIBLE)
                    remoteViews.setOnClickPendingIntent(
                        it,
                        getPendingIntent(context, downloadURL, notifyID, ACTION_RETRY),
                    )
                }
                R.id.download_notification_btn_cancel.let {
                    remoteViews.setImageViewResource(it, R.drawable.icon_close_fill)
                    remoteViews.setViewVisibility(it, View.VISIBLE)
                    remoteViews.setOnClickPendingIntent(
                        it,
                        getPendingIntent(context, downloadURL, notifyID, ACTION_DELETE),
                    )
                }
            }

            DOWNLOAD_TYPE_FINISHED -> {
                remoteViews.setTextViewText(
                    R.id.download_notification_title,
                    appName + context.getString(R.string.download_notify_download_finished)
                )
                remoteViews.setTextViewText(R.id.download_notification_desc, "")
                remoteViews.setViewVisibility(R.id.download_notification_btn_restart, View.GONE)
                remoteViews.setOnClickPendingIntent(
                    R.id.download_notification_layout,
                    getPendingIntent(context, downloadURL, notifyID, ACTION_INSTALL),
                )
                remoteViews.setViewVisibility(R.id.download_notification_btn_cancel, View.GONE)
                remoteViews.setViewVisibility(R.id.download_notification_btn_restart, View.GONE)
            }
        }

        if (!TextUtils.isEmpty(iconURL)) {
            if (mIconList[notifyID] == null) {
                ThreadManager.getInstance().start {
                    var bitmap = BitmapUtil.getRemoteBitmap(
                        iconURL,
                        DisplayUtil.dip2px(context.applicationContext, 40f),
                        DisplayUtil.dip2px(context.applicationContext, 40f),
                    )
                    if (null == bitmap) {
                        try {
                            bitmap = BitmapFactory.decodeResource(
                                context.applicationContext.resources,
                                R.mipmap.icon
                            )
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }

                    bitmap?.let { bitmap ->
                        mIconList[notifyID] = bitmap
                        ThreadManager.getInstance().runOnUIThread {
                            remoteViews.setImageViewBitmap(
                                R.id.download_notification_logo,
                                BitmapTransUtils.getBitmapWithRound(bitmap, bitmap.width * 0.15f),
                            )
                            NotifyManager.sendNotifyNow(context, remoteViews, channelID, notifyID)
                        }
                    }
                }
            } else {
                mIconList[notifyID]?.let {
                    remoteViews.setImageViewBitmap(
                        R.id.download_notification_logo,
                        BitmapTransUtils.getBitmapWithRound(it, it.width * 0.15f),
                    )
                    NotifyManager.sendNotifyNow(context, remoteViews, channelID, notifyID)
                }
            }
        } else {
            remoteViews.setImageViewResource(R.id.download_notification_logo, R.mipmap.icon)
            NotifyManager.sendNotifyNow(context, remoteViews, channelID, notifyID)
        }
    }

    fun getDownloadBroadcastFilter(context: Context): String {
        return context.packageName + "." + NOTIFICATION_BROADCAST_ACTION
    }

    fun getPendingIntent(
        context: Context,
        url: String,
        notifyID: Int,
        action: String
    ): PendingIntent {
        return PendingIntentUtils.getBroadcastPendingIntent(
            context,
            mIntentID.generate(),
            Intent().apply {
                setAction(getDownloadBroadcastFilter(context))
                putExtra(NOTIFICATION_ID_KEY, notifyID)
                putExtra(ACTION_KEY, action)
                putExtra(NOTIFICATION_URL_KEY, url)
            })
    }
}
