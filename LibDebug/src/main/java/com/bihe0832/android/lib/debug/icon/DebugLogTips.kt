package com.bihe0832.android.lib.debug.icon

import android.Manifest
import android.app.Activity
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

    fun initModule(activity: Activity, useDefault: Boolean) {
        PermissionManager.addPermissionContent(
                SCENE_NAME_DEBUG,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                "调试日志需要通过<font color ='#38ADFF'><b>悬浮窗实时展示</b></font>。当前手机尚未授权，请点击「" + PermissionManager.getPositiveText(activity) + "」前往设置！"
        )
        activity.let { activity ->
            mIconManager = IconManager(activity)
            if (useDefault) {
                mLogView = DebugLogTipsIcon(activity)
                mIconManager?.let {
                    mLogView?.let {
                        it.visibility = View.VISIBLE
                        mIconManager!!.showViewWithPermissionCheck(
                                it,
                                SCENE_NAME_DEBUG,
                                mIconManager!!.getFullScreenFlag(),
                                it.locationX,
                                it.locationY, null)
                    }
                    show("")
                }
            }
        }
    }

    fun showView(logView: DebugLogTipsIcon, canTouch: Boolean) {
        mIconManager?.let {
            logView.visibility = View.VISIBLE
            mIconManager!!.showViewWithPermissionCheck(
                    logView,
                    SCENE_NAME_DEBUG,
                    if (canTouch) mIconManager!!.getFullScreenFlag() else mIconManager!!.getNoTouchFlag(),
                    logView.locationX,
                    logView.locationY, null)
        }
    }


    fun hideView(logView: DebugLogTipsIcon) {
        mIconManager?.let {
            it.removeView(logView)
        }
    }

    fun show(text: String) {
        mLogView?.show(text)
    }


    fun append(text: String) {
        mLogView?.append(text)
    }

}