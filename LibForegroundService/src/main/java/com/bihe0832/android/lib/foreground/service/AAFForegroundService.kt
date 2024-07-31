package com.bihe0832.android.lib.foreground.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.bihe0832.android.lib.foreground.service.AAFForegroundServiceManager.ACTION_STOP
import com.bihe0832.android.lib.foreground.service.AAFForegroundServiceManager.ACTION_UPADTE
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
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        ZLog.d(TAG, "$TAG onCreate被调用，service 初始化:${this.hashCode()}")
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelNotify(this, AAFForegroundServiceManager.getNoticeID())
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        ZLog.d(TAG, "$TAG onStartCommand 被调用:${this.hashCode()} $hasInit")
        if (!hasInit) {
            init()
        }
        NotifyManager.sendNotifyNow(
            this,
            AAFForegroundServiceManager.getChannelID(),
            AAFForegroundServiceManager.getCurrentNotification(this),
            AAFForegroundServiceManager.getNoticeID()
        )
        if (ACTION_UPADTE == intent.action) {
            ZLog.d(TAG, "$TAG do noting")
        } else if (ACTION_STOP == intent.action) {
            ZLog.d(TAG, "$TAG do stop")
            ThreadManager.getInstance().start({
                ZLog.d(TAG, "$TAG start stop")
                stopSelf()
            }, 6)
        } else {
            ThreadManager.getInstance().start {
                AAFForegroundServiceManager.doAction(this, intent, flags, startId)
            }
        }
        return START_NOT_STICKY
    }

    private fun init() {
        ZLog.d(TAG, "$TAG init:${this.hashCode()} $hasInit")
        if (hasInit) {
            ZLog.d(TAG, "$TAG onCreate被调用，service 已经被初始化:${this.hashCode()}")
            return
        }
        initForegroundNotify()
        hasInit = true
    }

    private fun initForegroundNotify() {
        startForeground(
            AAFForegroundServiceManager.getNoticeID(),
            AAFForegroundServiceManager.getCurrentNotification(this)
        )
    }

}
