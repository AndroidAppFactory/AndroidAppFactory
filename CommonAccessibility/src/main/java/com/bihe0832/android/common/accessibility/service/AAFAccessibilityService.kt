package com.bihe0832.android.common.accessibility.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import com.bihe0832.android.common.accessibility.action.AAFAccessibilityManager
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.os.BuildUtils


class AAFAccessibilityService : AccessibilityService() {

    /**
     * 当系统成功连接到无障碍服务时，会调用此方法。使用此方法可为服务执行任何一次性设置步骤，包括连接到用户反馈系统服务，如音频管理器或设备振动器。如果您要在运行时设置服务的配置或做出一次性调整，从此处调用 setServiceInfo() 非常方便。
     */
    override fun onServiceConnected() {
        super.onServiceConnected()
        AAFAccessibilityManager.start()
        AAFAccessibilityManager.addAccessibilityActionDispatcher(object : AAFAccessibilityManager.AAFAccessibilityActionDispatcher {
            override fun performGlobalAction(action: Int): Boolean {
                return this@AAFAccessibilityService.performGlobalAction(action)
            }

            override fun dispatchGesture(gesture: GestureDescription, callback: GestureResultCallback?): Boolean {
                return if (BuildUtils.SDK_INT >= Build.VERSION_CODES.N) {
                    this@AAFAccessibilityService.dispatchGesture(gesture, callback, null)
                } else {
                    false
                }
            }

        })
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
        AAFAccessibilityManager.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        AAFAccessibilityManager.stop()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!ZixieContext.isOfficial()) {
            event?.source?.getNodeInfoOutBounds()?.let {
                if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                    ZLog.d(AAFAccessibilityManager.TAG, "onAccessibilityEvent:(left, top, centerX, centerY)  ${it.left} ${it.top} ${it.centerX()}  ${it.centerY()} ${event?.className} ${event?.text} ")
                }
            }
        }
    }

    override fun onInterrupt() {
    }
}