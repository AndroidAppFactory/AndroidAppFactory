package com.bihe0832.android.lib.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.text.TextUtils
import android.widget.RemoteViews
import com.bihe0832.android.lib.utils.IdGenerator
import com.bihe0832.android.lib.utils.apk.APKUtils
import java.util.*

object NotifyManager {

    private val mNotificationChannel = HashMap<String, NotificationChannel>()
    private val mNotifyID = IdGenerator(1)

    fun createNotificationChannel(context: Context, channelName: CharSequence, channelId: String): NotificationChannel? {
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
        var noticeID = mNotifyID.generate()
        context.applicationContext.let { context ->
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
                sendNotifyNow(context, channelID, it, noticeID)
            }
        }
        return noticeID
    }

    fun cancleNotify(context: Context, noticeID: Int) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(noticeID)
    }

    fun sendNotifyNow(context: Context, channelID: String, notification: Notification, notifyID: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!mNotificationChannel.contains(channelID)) {
                createNotificationChannel(context, APKUtils.getAppName(context) + channelID, channelId = channelID)?.let {
                    mNotificationChannel.put(channelID, it)
                }
            }
        }
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(notifyID, notification)
    }
}


