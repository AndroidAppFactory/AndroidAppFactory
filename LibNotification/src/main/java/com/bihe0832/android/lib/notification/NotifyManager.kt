package com.bihe0832.android.lib.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.text.TextUtils
import android.text.format.Formatter
import android.view.View
import android.widget.RemoteViews
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.IdGenerator
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import java.util.*

object NotifyManager {

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

    private val mNotificationChannel = HashMap<String, NotificationChannel>()


    private val mNotifyID = IdGenerator(1)
    private val mIntentID = IdGenerator(1)


    private fun createNotificationChannel(context: Context, channelName: CharSequence, channelId: String): NotificationChannel? {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.setSound(null, null)
            channel.enableLights(false)
            channel.enableVibration(false)
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
            return channel
        }
        return null
    }

    fun sendNotifyNow(context: Context, title: String, subTitle: String?, content: String?, action: String?, channelID: String): Int {
        var noticeID = 1
        context.applicationContext.let { context ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!mNotificationChannel.contains(channelID)) {
                    createNotificationChannel(context, APKUtils.getAppName(context) + channelID, channelId = channelID)?.let {
                        mNotificationChannel.put(channelID, it)
                    }
                }
            }

            NotificationCompat.Builder(context, channelID).apply {
                setContentTitle(title)
                //设置内容
                if (content?.isNotEmpty() == true) {
                    setContentText(content)
                }

                if (subTitle?.isNotEmpty() == true) {
                    setSubText(subTitle)
                }
                setAutoCancel(true)
                //设置小图标
                setSmallIcon(R.mipmap.icon)
                //设置通知时间
                setWhen(System.currentTimeMillis())
                //设置通知方式，声音，震动，呼吸灯等效果，这里通知方式为声音
                setDefaults(Notification.DEFAULT_SOUND)
                if (!TextUtils.isEmpty(action)) {
                    try {
                        val uri = Uri.parse(action)
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                        setContentIntent(pendingIntent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    APKUtils.startApp(context, context.packageName, APKUtils.getAppName(context))
                }

            }.build().let {
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(noticeID, it)
            }
        }
        return noticeID
    }

    fun cancleNotify(context: Context, noticeID: Int) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(noticeID)
    }

    fun sendDownloadNotify(context: Context, appName: String, iconURL: String, finished: Long, total: Long, speed: Long, process: Int, downloadType: Int, channelID: String, notifyIDFromParam: Int): Int {

        var notifyID = if (notifyIDFromParam < 1) {
            mNotifyID.generate()
        } else {
            notifyIDFromParam
        }
        ThreadManager.getInstance().runOnUIThread {
            context.applicationContext.let { context ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!mNotificationChannel.contains(channelID)) {
                        createNotificationChannel(context, APKUtils.getAppName(context) + channelID, channelId = channelID)?.let {
                            mNotificationChannel.put(channelID, it)
                        }
                    }
                }

                val remoteViews = RemoteViews(context.getPackageName(), R.layout.download_notification)

                if (notifyIDFromParam < 1) {
                    if (!TextUtils.isEmpty(iconURL)) {
                        Glide.with(context.applicationContext)
                                .asBitmap()
                                .load(iconURL)
                                .into(object : SimpleTarget<Bitmap>() {
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                        remoteViews.setImageViewBitmap(R.id.iv_logo, resource)
                                        updateContent(remoteViews, context, appName, finished, total, speed, process, downloadType, channelID, notifyID)
                                    }
                                })
                    } else {
                        remoteViews.setImageViewResource(R.id.iv_logo, R.mipmap.icon)
                        updateContent(remoteViews, context, appName, finished, total, speed, process, downloadType, channelID, notifyID)
                    }
                }
            }
        }
        return notifyID
    }

    fun updateContent(remoteViews: RemoteViews, context: Context, appName: String, finished: Long, total: Long, speed: Long, process: Int, downloadType: Int, channelID: String, notifyID: Int) {
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
        NotificationCompat.Builder(context, channelID).apply {
            setOnlyAlertOnce(true)
            setContent(remoteViews)
            //设置小图标
            setSmallIcon(R.mipmap.icon)
            //禁止用户点击删除按钮删除
            setAutoCancel(false)
            //禁止滑动删除
            setOngoing(true)
            //取消右上角的时间显示
            setShowWhen(false)
        }.build().let {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(notifyID, it)
        }
    }

    fun getPendingIntent(context: Context, notifyID: Int, action: String): PendingIntent {
        return PendingIntent.getBroadcast(context, mIntentID.generate(), Intent().apply {
            setAction(NOTIFICATION_BROADCAST_ACTION)
            putExtra(NOTIFICATION_ID_KEY, notifyID)
            putExtra(ACTION_KEY, action)
        }, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}


