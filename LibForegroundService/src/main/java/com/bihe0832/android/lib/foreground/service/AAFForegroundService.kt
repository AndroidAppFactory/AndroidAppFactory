package com.bihe0832.android.lib.foreground.service

import android.app.Notification
import android.content.Context
import android.content.Intent

/**
 * Summary
 *
 * @author code@bihe0832.com
 * Created on 2024/1/24.
 * Description:
 */
open class AAFForegroundService : BaseForegroundService() {
    override fun getNoticeID(): Int {
        return AAFForegroundServiceManager.getNoticeID()
    }

    override fun getChannelID(): String {
        return AAFForegroundServiceManager.getChannelID()
    }

    override fun getCurrentNotification(context: Context): Notification {
        return AAFForegroundServiceManager.getCurrentNotification(this)
    }

    override fun doAction(context: Context, intent: Intent, flags: Int, startId: Int) {
        AAFForegroundServiceManager.doAction(this, intent, flags, startId)
    }
}
