package com.bihe0832.android.lib.debug.icon

import android.app.Activity
import android.text.TextUtils
import android.view.View
import com.bihe0832.android.lib.floatview.IconManager


/**
 * Created by lwtorlu on 2021/9/8.
 */
object DebugLogTips {

    private var mIconManager: IconManager? = null
    private var mLogView: DebugLogTipsIcon? = null
    private var currentText = ""

    fun initModule(activity: Activity) {
        activity.let { activity ->
            mLogView = DebugLogTipsIcon(activity)
            mIconManager = IconManager(activity)
            mIconManager?.let {
                mLogView?.let {
                    it.visibility = View.VISIBLE
                    mIconManager!!.showViewWithPermissionCheck(
                            it,
                            mIconManager!!.getFullScreenFlag(),
                            it.locationX,
                            it.locationY, null)
                    showResult()
                }
            }

        }
    }

    fun show(text: String) {
        currentText = text
        showResult()
    }


    fun append(text: String) {
        currentText += text
        showResult()
    }

    private fun showResult() {
        if (TextUtils.isEmpty(currentText)) {
            hide()
        } else {
            mLogView?.let {
                it.updateText(currentText)
                it.visibility = View.VISIBLE
            }
        }
    }

    fun hide() {
        mLogView?.visibility = View.GONE
    }
}