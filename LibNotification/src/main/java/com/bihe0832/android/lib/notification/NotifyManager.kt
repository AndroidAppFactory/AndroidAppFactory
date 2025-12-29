package com.bihe0832.android.lib.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bihe0832.android.lib.utils.IdGenerator
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.bihe0832.android.lib.utils.os.BuildUtils
import java.util.concurrent.ConcurrentHashMap
import com.bihe0832.android.lib.aaf.res.R as ResR


object NotifyManager {

    private val mNotificationChannel = HashMap<String, NotificationChannel>()
    private val mNotifyIDGenerator = IdGenerator(1)

    private val mListID by lazy {
        ConcurrentHashMap<Int, String>()
    }

    @Synchronized
    fun getNotifyIDByKey(key: String): Int {
        var id = try {
            if (TextUtils.isEmpty(key)) {
                generatorNoticeID()
            } else {
                if (mListID.containsValue(key)) {
                    mListID.filter { it.value == key }.keys.first()
                } else {
                    generatorNoticeID()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            generatorNoticeID()
        }
        mListID[id] = key
        return id
    }

    fun generatorNoticeID(): Int {
        return mNotifyIDGenerator.generate()
    }

    fun createNotificationChannel(context: Context, channelName: CharSequence, channelId: String): NotificationChannel? {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.O) {
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

    fun getPendingIntent(context: Context, action: String?): PendingIntent? {
        var intent = if (!TextUtils.isEmpty(action)) {
            try {
                Intent(Intent.ACTION_VIEW, Uri.parse(action))
            } catch (e: Exception) {
                e.printStackTrace()
                context.packageManager.getLaunchIntentForPackage(context.packageName)
            }
        } else {
            context.packageManager.getLaunchIntentForPackage(context.packageName)
        }

        try {
            return PendingIntent.getActivity(context, generatorNoticeID(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun sendNotifyNow(context: Context, title: String, subTitle: String?, content: String?, action: String?, channelID: String): Int {
        var noticeID = generatorNoticeID()
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
                setSmallIcon(ResR.mipmap.icon)
                //设置通知时间
                setWhen(System.currentTimeMillis())
                //设置通知方式，声音，震动，呼吸灯等效果，这里通知方式为声音
                setDefaults(Notification.DEFAULT_SOUND)
                setContentIntent(getPendingIntent(context, action))
            }.build().let {
                sendNotifyNow(context, channelID, it, noticeID)
            }
        }
        return noticeID
    }

    fun sendNotifyNow(context: Context, remoteViews: RemoteViews, channelID: String, notifyID: Int) {
        context.applicationContext.let { context ->
            NotificationCompat.Builder(context, channelID).apply {
                setOnlyAlertOnce(true)
                setContent(remoteViews)
                //设置小图标
                setSmallIcon(ResR.mipmap.icon)
                //禁止用户点击删除按钮删除
                setAutoCancel(false)
                //禁止滑动删除
                setOngoing(false)
                //设置通知时间
                setWhen(System.currentTimeMillis())
                //设置通知方式，声音，震动，呼吸灯等效果，这里通知方式为声音
                setDefaults(Notification.DEFAULT_SOUND)
            }.build().let {
                sendNotifyNow(context, channelID, it, notifyID)
            }
        }
    }

    fun sendNotifyNow(context: Context, channelID: String, notification: Notification, notifyID: Int) {
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.O) {
            if (!mNotificationChannel.contains(channelID)) {
                createNotificationChannel(context, APKUtils.getAppName(context) + channelID, channelId = channelID)?.let {
                    mNotificationChannel.put(channelID, it)
                }
            }
        }
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(notifyID, notification)
    }

    fun cancelNotify(context: Context, noticeID: Int) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(noticeID)
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        return if (BuildUtils.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            false
        }
    }

    fun showNotificationsSettings(context: Context): Boolean {
        return if (IntentUtils.startAppSettings(context, Settings.ACTION_APP_NOTIFICATION_SETTINGS)) {
            true
        } else {
            return IntentUtils.startAppDetailSettings(context)
        }
    }
}


