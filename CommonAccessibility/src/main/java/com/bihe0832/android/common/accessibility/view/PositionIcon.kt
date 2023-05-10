package com.bihe0832.android.common.accessibility.view

import android.content.Context
import android.view.MotionEvent
import androidx.core.view.isVisible
import com.bihe0832.android.common.accessibility.R
import com.bihe0832.android.lib.floatview.BaseIconView
import com.bihe0832.android.lib.ui.custom.view.background.TextViewWithBackground
import com.bihe0832.android.lib.utils.ConvertUtils

open class PositionIcon(context: Context) : BaseIconView(context) {

    protected var mLocationKey: String = ""
    private var mStartLocationX: Int = 0
    private var mStartLocationY: Int = 0
    private var mCallbackClick: Boolean = true

    fun setActived(shouldNotifyClick: Boolean) {
        mCallbackClick = shouldNotifyClick
    }

    fun getLocationKey(): String {
        return mLocationKey
    }

    fun setLocationInfo(locationKey: String, startLocationX: Int, startLocationY: Int) {
        setStartLocationX(startLocationX)
        setStartLocationY(startLocationY)
        this.mLocationKey = locationKey
    }

    fun setStartLocationX(location: Int) {
        if (location > 0) {
            this.mStartLocationX = location
        } else {
            this.mStartLocationX = 0
        }
    }

    fun setStartLocationY(location: Int) {
        if (location > 0) {
            this.mStartLocationX = location
        } else {
            this.mStartLocationX = 0
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
        if (mLocationKey.isEmpty()) {
            return mStartLocationX
        }
        val location = PositionConfig.getPositionList(mLocationKey)
        return ConvertUtils.parseInt(ConvertUtils.getSafeValueFromList(location, 0, ""), mStartLocationX)
    }

    override fun getDefaultY(): Int {
        if (mLocationKey.isEmpty()) {
            return mStartLocationY
        }
        val location = PositionConfig.getPositionList(mLocationKey)
        return ConvertUtils.parseInt(ConvertUtils.getSafeValueFromList(location, 1, ""), mStartLocationY)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (mLocationKey.isNotEmpty() && event?.action == MotionEvent.ACTION_UP) {
            updatePosition()
            if (mCallbackClick) {
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
            PositionConfig.writePosition(mLocationKey, outLocation[0], outLocation[1], centerX, centerY)
        }
    }

    override fun ignoreStatusBar(): Boolean {
        return true
    }
}