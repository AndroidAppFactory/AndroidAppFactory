package com.bihe0832.android.lib.foreground.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
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
abstract class BaseForegroundService : Service() {

    companion object {
        const val TAG = "AAFForegroundService"
        const val ACTION_UPADTE = "AAFForegroundServiceManager.update"
        const val ACTION_STOP = "AAFForegroundServiceManager.stop"
        const val INTENT_KEY_PERMISSION = "AAFForegroundServiceManager.notify.permission"
    }

    abstract fun getNoticeID(): Int

    abstract fun getChannelID(): String

    abstract fun getCurrentNotification(context: Context): Notification

    abstract fun doAction(context: Context, intent: Intent, flags: Int, startId: Int)

    internal var hasInit = false
    internal var hasForeground = false
    internal var hasPermission = false

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
        cancelNotify(this, getNoticeID())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        ZLog.d(TAG, "\nonStartCommand 被调用:${this.hashCode()} $hasInit")
        ZLog.d(TAG, intent)
        hasPermission = "1" == intent.getStringExtra(INTENT_KEY_PERMISSION)
        init()
        if (ACTION_UPADTE == intent.action) {
            ZLog.d(TAG, " do noting")
        } else if (ACTION_STOP == intent.action) {
            ZLog.d(TAG, " do stop")
            ThreadManager.getInstance().start({
                ZLog.d(TAG, " start stop")
                stopForeground(true)
                stopSelf()
            }, 1)
        } else {
            ThreadManager.getInstance().start {
                doAction(this, intent, flags, startId)
            }
        }
        return START_NOT_STICKY
    }


    protected open fun init() {
        ZLog.d(TAG, "init:${this.hashCode()} hasInit：$hasInit hasForeground：$hasForeground")
        if (!hasForeground) {
            initForegroundNotify()
        }
        hasInit = true
    }

    private fun initForegroundNotify() {
        if (hasPermission || NotifyManager.areNotificationsEnabled(this)) {
            ZLog.d(TAG, "startForeground:${this.hashCode()}")
            try {
                startForeground(getNoticeID(), getCurrentNotification(this))
                hasForeground = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            ZLog.d(TAG, "startForeground skip:${this.hashCode()}")
        }
    }
}
