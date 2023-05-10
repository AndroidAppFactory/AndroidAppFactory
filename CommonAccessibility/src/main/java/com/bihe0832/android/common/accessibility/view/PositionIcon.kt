package com.bihe0832.android.common.accessibility.view

import android.content.Context
import android.view.MotionEvent
import androidx.core.view.isVisible
import com.bihe0832.android.common.accessibility.R
import com.bihe0832.android.lib.floatview.BaseIconView
import com.bihe0832.android.lib.ui.custom.view.background.TextViewWithBackground
import com.bihe0832.android.lib.utils.ConvertUtils

class PositionIcon(context: Context) : BaseIconView(context) {

    private var locationKey: String = ""
    private var startLocationX: Int = 0
    private var startLocationY: Int = 0
    private var callbackClick: Boolean = false

    fun shouldNotifyClick(shouldNotifyClick: Boolean) {
        callbackClick = shouldNotifyClick
    }

    fun setLocationInfo(locationKey: String, startLocationX: Int, startLocationY: Int) {
        setStartLocationX(startLocationX)
        setStartLocationY(startLocationY)
        this.locationKey = locationKey
    }

    fun setStartLocationX(location: Int) {
        if (location > 0) {
            this.startLocationX = location
        } else {
            this.startLocationX = 0
        }
    }

    fun setStartLocationY(location: Int) {
        if (location > 0) {
            this.startLocationX = location
        } else {
            this.startLocationX = 0
        }
    }

    fun setText(text: String) {
        findViewById<TextViewWithBackground>(R.id.position_text).text = text
    }

    fun getText(): String {
        return findViewById<TextViewWithBackground>(R.id.position_text).text.toString()
    }

    override fun getLayoutId(): Int {
        return R.layout.com_bihe0832_accessibility_position
    }

    override fun getRootId(): Int {
        return R.id.position_layout
    }

    override fun getDefaultX(): Int {
        if (locationKey.isEmpty()) {
            return startLocationX
        }
        val location = PositionConfig.getPositionList(locationKey)
        return ConvertUtils.parseInt(ConvertUtils.getSafeValueFromList(location, 0, ""), startLocationX)
    }

    override fun getDefaultY(): Int {
        if (locationKey.isEmpty()) {
            return startLocationY
        }
        val location = PositionConfig.getPositionList(locationKey)
        return ConvertUtils.parseInt(ConvertUtils.getSafeValueFromList(location, 1, ""), startLocationY)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (locationKey.isNotEmpty() && event?.action == MotionEvent.ACTION_UP) {
            updatePosition()
            if (callbackClick) {
                performClick()
            }
        }
        return super.onTouchEvent(event)
    }

    fun updatePosition() {
        if (isVisible) {
            val outLocation = IntArray(2)
            getLocationOnScreen(outLocation)
            var centerX = outLocation[0] + (right - left) / 2
            var centerY = outLocation[1] + (bottom - top) / 2
            PositionConfig.writePosition(locationKey, outLocation[0], outLocation[1], centerX, centerY)
        }
    }

    override fun ignoreStatusBar(): Boolean {
        return true
    }
}