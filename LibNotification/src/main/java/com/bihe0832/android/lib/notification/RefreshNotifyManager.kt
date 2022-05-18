package com.bihe0832.android.lib.notification

import android.content.Context
import android.text.TextUtils
import android.widget.RemoteViews
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.apk.APKUtils
import com.bihe0832.android.lib.utils.time.DateUtil

object RefreshNotifyManager {

    @Synchronized
    fun getNotifyIDByKey(key: String): Int {
        return if (TextUtils.isEmpty(key)) {
            NotifyManager.getNotifyIDByKey("")
        } else {
            NotifyManager.getNotifyIDByKey("RefreshNotifyManager$key")
        }
    }

    fun getNotificationSubTitle(context: Context, subTitle: String, time: Long): String {
        return APKUtils.getAppName(context) + " • " + subTitle + " • <Strong>" + DateUtil.getDateCompareResult1(time) + "</Strong>"
    }

    fun sendDownloadNotify(context: Context, key: String, title: String, subTitleContent: String, content: String, firstTime: Long): Int {
        return sendDownloadNotify(context, key, title, getNotificationSubTitle(context, subTitleContent, firstTime), content, "", "")
    }

    fun sendDownloadNotify(context: Context, key: String, title: String, subTitle: String, content: String): Int {
        return sendDownloadNotify(context, key, title, subTitle, content, "", "")
    }

    fun sendDownloadNotify(context: Context, key: String, title: String, subTitle: String, content: String, action: String, channelID: String): Int {
        var notifyID = getNotifyIDByKey(key)
        ThreadManager.getInstance().runOnUIThread {
            context.applicationContext.let { context ->
                val remoteViews = RemoteViews(context.packageName, R.layout.com_bihe0832_refresh_notification)
                remoteViews.setTextViewText(R.id.refresh_notification_sub_title, TextFactoryUtils.getSpannedTextByHtml(subTitle))
                remoteViews.setTextViewText(R.id.refresh_notification_title, TextFactoryUtils.getSpannedTextByHtml(title))
                remoteViews.setTextViewText(R.id.refresh_notification_desc, TextFactoryUtils.getSpannedTextByHtml(content))
                remoteViews.setOnClickPendingIntent(R.id.refresh_notification_layout, NotifyManager.getPendingIntent(context, action))
                NotifyManager.sendNotifyNow(context, remoteViews, channelID, notifyID)
            }
        }
        return notifyID
    }
}


