package com.bihe0832.android.lib.debug.icon

import android.Manifest
import android.app.Activity
import android.view.Gravity
import android.view.View
import com.bihe0832.android.lib.floatview.IconManager
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.thread.ThreadManager


/**
 * Created by lwtorlu on 2021/9/8.
 */
object DebugLogTips {

    private const val SCENE_NAME_DEBUG = "IconManagerDebugLogTips"
    private var mIconManager: IconManager? = null
    private var mLogView: DebugLogTipsIcon? = null

    fun initModule(activity: Activity) {
        initModule(activity, true)
    }

    fun initModule(activity: Activity, useDefault: Boolean) {
        initModule(activity, useDefault, Gravity.RIGHT)
    }

    fun initModule(activity: Activity, useDefault: Boolean, gravity: Int) {
        PermissionManager.addPermissionGroupDesc(SCENE_NAME_DEBUG, Manifest.permission.SYSTEM_ALERT_WINDOW, "悬浮窗")

        PermissionManager.addPermissionGroupScene(SCENE_NAME_DEBUG, Manifest.permission.SYSTEM_ALERT_WINDOW, "调试日志需要通过<font color ='#38ADFF'><b>悬浮窗实时展示</b></font>")

        PermissionManager.addPermissionGroupContent(
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
                        it.getTextView()?.gravity = gravity
                        it.layoutParams = it.findViewById<View>(it.rootId).layoutParams
                        mIconManager!!.showViewWithPermissionCheck(
                                it,
                                SCENE_NAME_DEBUG,
                                mIconManager!!.getNoTouchFlag(),
                                it.locationX,
                                it.locationY, null)
                    }
                    show("")
                }
            }
        }
    }

    fun showView(logView: DebugLogTipsIcon, canTouch: Boolean) {
        ThreadManager.getInstance().runOnUIThread {
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
    }


    fun hideView(logView: DebugLogTipsIcon) {
        ThreadManager.getInstance().runOnUIThread {
            mIconManager?.removeView(logView)
        }
    }

    fun hide() {
        mLogView?.let {
            hideView(it)
        }
    }

    fun show(text: String) {
        ThreadManager.getInstance().runOnUIThread {
            mLogView?.let {
                it.show(text)
                mIconManager!!.showViewWithPermissionCheck(it, SCENE_NAME_DEBUG, mIconManager!!.getNoTouchFlag(), it.locationX,
                        it.locationY, null)
            }
        }
    }

    fun append(text: String) {
        ThreadManager.getInstance().runOnUIThread {
            mLogView?.let {
                it.append(text)
                mIconManager!!.showViewWithPermissionCheck(
                        it,
                        SCENE_NAME_DEBUG,
                        mIconManager!!.getNoTouchFlag(),
                        it.locationX,
                        it.locationY, null)
            }
        }
    }

}