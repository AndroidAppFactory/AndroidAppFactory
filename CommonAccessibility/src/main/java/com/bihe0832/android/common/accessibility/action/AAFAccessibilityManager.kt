package com.bihe0832.android.common.accessibility.action

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Build
import android.provider.Settings
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.bihe0832.android.lib.utils.os.BuildUtils

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2023/5/10.
 * Description: Description
 *
 */
object AAFAccessibilityManager {

    const val TAG = "AAFAccessibilityManager"

    private var isStart: Boolean = false
    private var mAAFAccessibilityActionDispatcher: AAFAccessibilityActionDispatcher? = null

    interface AAFAccessibilityActionDispatcher {

        fun performGlobalAction(action: Int): Boolean

        fun dispatchGesture(gesture: GestureDescription, callback: AccessibilityService.GestureResultCallback?): Boolean
    }

    fun addAccessibilityActionDispatcher(dispatcher: AAFAccessibilityActionDispatcher) {
        mAAFAccessibilityActionDispatcher = dispatcher
    }

    fun start() {
        isStart = true
    }

    fun stop() {
        isStart = false
    }

    fun openSettings(context: Context) {
        IntentUtils.startSettings(context, Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }

    fun performGlobalAction(action: Int): Boolean {
        return mAAFAccessibilityActionDispatcher?.performGlobalAction(action) ?: false
    }

    fun performViewAction(info: AccessibilityNodeInfo, action: Int): Boolean {
        return info.performAction(action)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun performAction(path: Path, callback: AccessibilityService.GestureResultCallback?) {
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.N) {
            val gesture = GestureDescription.Builder()
            gesture.addStroke(GestureDescription.StrokeDescription(path, 0, ViewConfiguration.getTapTimeout().toLong()))
            mAAFAccessibilityActionDispatcher?.dispatchGesture(gesture.build(), callback)
        }
    }


}