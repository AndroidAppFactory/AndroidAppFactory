package com.bihe0832.android.common.accessibility.service

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2023/5/10.
 * Description: Description
 *
 */

fun AccessibilityNodeInfo.getNodeInfoOutBounds(): Rect {
    var outBounds = Rect()
    getBoundsInScreen(outBounds)
    return outBounds
}
