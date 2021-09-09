package com.bihe0832.android.lib.debug.icon

import android.Manifest
import android.app.Activity
import android.text.TextUtils
import android.view.View
import com.bihe0832.android.lib.floatview.IconManager
import com.bihe0832.android.lib.permission.PermissionManager


/**
 * Created by lwtorlu on 2021/9/8.
 */
object DebugLogTips {

    private const val SCENE_NAME_DEBUG = "IconManagerDebugLogTips"
    private var mIconManager: IconManager? = null
    private var mLogView: DebugLogTipsIcon? = null
    private var currentText = ""

    fun initModule(activity: Activity) {
        PermissionManager.addPermissionContent(
                SCENE_NAME_DEBUG,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                "调试日志需要通过<font color ='#38ADFF'><b>悬浮窗实时展示</b></font>。当前手机尚未授权，请点击「" + PermissionManager.getPositiveText(activity) + "」前往设置！"
        )
        activity.let { activity ->
            mLogView = DebugLogTipsIcon(activity)
            mIconManager = IconManager(activity)
            mIconManager?.let {
                mLogView?.let {
                    it.visibility = View.VISIBLE
                    mIconManager!!.showViewWithPermissionCheck(
                            it,
                            SCENE_NAME_DEBUG,
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