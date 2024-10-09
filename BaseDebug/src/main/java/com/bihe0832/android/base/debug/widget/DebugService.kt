package com.bihe0832.android.base.debug.widget

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.bihe0832.android.lib.log.ZLog

/**
 * Summary
 *
 * @author code@bihe0832.com
 * Created on 2024/1/24.
 * Description:
 */
open class DebugService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ZLog.d("DebugService", "\nonStartCommand 被调用:${this.hashCode()}")
        ZLog.d("DebugService", intent)
        return super.onStartCommand(intent, flags, startId)
    }

}
