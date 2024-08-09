package com.bihe0832.android.lib.foreground.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.bihe0832.android.lib.foreground.service.AAFForegroundServiceManager.ACTION_STOP
import com.bihe0832.android.lib.foreground.service.AAFForegroundServiceManager.ACTION_UPADTE
import com.bihe0832.android.lib.foreground.service.AAFForegroundServiceManager.INTENT_KEY_PERMISSION
import com.bihe0832.android.lib.foreground.service.AAFForegroundServiceManager.TAG
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.notification.NotifyManager
import com.bihe0832.android.lib.notification.NotifyManager.cancelNotify
import com.bihe0832.android.lib.thread.ThreadManager

/**
 * Summary
 *
 * @author code@bihe0832.com
 * Created on 2024/1/24.
 * Description:
 */
open class AAFForegroundService : Service() {


    private var hasInit = false
    private var hasForeground = false
    private var hasPermission = false
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        ZLog.d(TAG, "\nonCreate被调用:${this.hashCode()}")
    }

    override fun onDestroy() {
        super.onDestroy()
        ZLog.d(TAG, "\nonDestroy被调用:${this.hashCode()}")
        cancelNotify(this, AAFForegroundServiceManager.getNoticeID())
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        ZLog.d(TAG, "\nonStartCommand 被调用:${this.hashCode()} $hasInit")
        ZLog.d(TAG, intent)
        hasPermission = "1" == intent.getStringExtra(INTENT_KEY_PERMISSION)
        init()
        NotifyManager.sendNotifyNow(
            this,
            AAFForegroundServiceManager.getChannelID(),
            AAFForegroundServiceManager.getCurrentNotification(this),
            AAFForegroundServiceManager.getNoticeID()
        )
        if (ACTION_UPADTE == intent.action) {
            ZLog.d(TAG, " do noting")
        } else if (ACTION_STOP == intent.action) {
            ZLog.d(TAG, " do stop")
            ThreadManager.getInstance().start({
                ZLog.d(TAG, " start stop")
                stopSelf()
            }, 1)
        } else {
            ThreadManager.getInstance().start {
                AAFForegroundServiceManager.doAction(this, intent, flags, startId)
            }
        }
        return START_NOT_STICKY
    }

    protected open fun init() {
        ZLog.d(TAG, "init:${this.hashCode()} hasInit：$hasInit hasForeground：$hasForeground")
        if (!hasInit || !hasForeground) {
            initForegroundNotify()
        }
        hasInit = true
    }

    private fun initForegroundNotify() {
        if (hasPermission) {
            hasForeground = true
            ZLog.d(TAG, "startForeground:${this.hashCode()}")
            startForeground(
                AAFForegroundServiceManager.getNoticeID(), AAFForegroundServiceManager.getCurrentNotification(this)
            )
        } else {
            ZLog.d(TAG, "startForeground skip:${this.hashCode()}")
        }
    }
}
