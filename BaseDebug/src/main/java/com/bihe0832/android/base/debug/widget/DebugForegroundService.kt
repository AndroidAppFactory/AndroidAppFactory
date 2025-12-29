package com.bihe0832.android.base.debug.widget

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bihe0832.android.lib.foreground.service.BaseForegroundService
import com.bihe0832.android.lib.aaf.res.R as ResR
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.notification.NotifyManager
import com.bihe0832.android.lib.utils.os.BuildUtils

/**
 * Summary
 *
 * @author code@bihe0832.com
 * Created on 2024/1/24.
 * Description:
 */
open class DebugForegroundService : BaseForegroundService() {

    private val NOTICE_ID = 99900
    private val NOTICE_CHANNEL_ID = "ForegroundService"
    override fun getNoticeID(): Int {
        return NOTICE_ID
    }

    override fun getChannelID(): String {
        return NOTICE_CHANNEL_ID
    }

    override fun getCurrentNotification(context: Context): Notification {
        val channelName = NOTICE_CHANNEL_ID
        NotifyManager.createNotificationChannel(context, channelName, NOTICE_CHANNEL_ID)
        // 如果API大于18，需要弹出一个可见通知
        return if (BuildUtils.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            NotificationCompat.Builder(context, NOTICE_CHANNEL_ID).setOngoing(true).setSmallIcon(ResR.mipmap.icon)
                    .setContentText(NOTICE_CHANNEL_ID).setContentTitle(channelName).build()
        } else {
            Notification()
        }
    }

    override fun doAction(context: Context, intent: Intent, flags: Int, startId: Int) {
        ZLog.d("DebugService","DebugService doAction ")
        val intent = Intent()
        intent.setClass(context, DebugService::class.java)
        intent.setAction("Action")
        context.startService(intent)
    }
}
