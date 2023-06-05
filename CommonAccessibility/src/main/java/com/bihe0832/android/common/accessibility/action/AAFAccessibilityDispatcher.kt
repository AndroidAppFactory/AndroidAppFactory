package com.bihe0832.android.common.accessibility.action

import android.accessibilityservice.AccessibilityService
import android.graphics.Path
import android.view.accessibility.AccessibilityNodeInfo
import com.bihe0832.android.common.accessibility.view.PositionConfig
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.ConvertUtils

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/5/10.
 * Description: 根据提供的位置，或者位置左边的key完成一次模拟操作
 *
 */
object AAFAccessibilityDispatcher {

    fun doClickAction(key: String, callback: AccessibilityService.GestureResultCallback?) {
        PositionConfig.getPositionList(key).let {
            val centerX = ConvertUtils.parseInt(ConvertUtils.getSafeValueFromList(it, 2, ""), -1)
            val centerY = ConvertUtils.parseInt(ConvertUtils.getSafeValueFromList(it, 3, ""), -1)
            if (centerX > 0 || centerY > 0) {
                ZLog.d(AAFAccessibilityManager.TAG, "[${key}] doClickAction  (centerX, centeY):$centerX $centerY")
                doClickAction(centerX, centerY, callback)
            } else {
                ZLog.d(AAFAccessibilityManager.TAG, "[${key}] doClickAction bad (centerX, centeY):$centerX $centerY")
            }
        }
    }

    fun doClickAction(locX: Int, locY: Int, callback: AccessibilityService.GestureResultCallback?) {
        if (locX < 0 || locY < -1) {
            return
        }
        val path = Path()
        path.reset()
        path.moveTo(locX.toFloat(), locY.toFloat())
        path.lineTo(locX.toFloat(), locY.toFloat())
        AAFAccessibilityManager.performAction(path, callback)
    }

    fun performGlobalAction(action: Int): Boolean {
        return AAFAccessibilityManager.performGlobalAction(action)
    }

    fun performViewAction(info: AccessibilityNodeInfo, action: Int): Boolean {
        return info.performAction(action)
    }

}