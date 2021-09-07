package com.bihe0832.android.lib.floatview;

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.widget.LinearLayout
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.utils.os.DisplayUtil


class IconManager(activity: Activity) {

    private val REQUEST_ACTIVITY_FLOAT_PERMISSION = 1010

    private val TAG = "IconManager"

    private var mActivity: Activity? = null
    private var mPermissionReqShow: Boolean = false

    //小悬浮窗
    private var mIconView: IconView? = null
    private var mIconUrl = ""

    //当前小悬浮窗的状态，是否是展示态
    private var mIconViewIsShowing = false

    init {
        mActivity = activity
        mIconView = IconView(mActivity)
    }

    constructor(activity: Activity, view: IconView) : this(activity) {
        mIconView = view
    }

    constructor(activity: Activity, ResID: Drawable?) : this(activity) {
        mIconView = IconView(mActivity, ResID)
    }

    constructor(activity: Activity, iconUrl: String?) : this(activity) {
        mIconView = IconView(mActivity)
        mIconView?.setIconImage(iconUrl)
    }


    private var mWindowManager: WindowManager? = null//窗口管理

    fun getIconLocationX(): Int {
        return if (mIconView?.iconLocationX ?: 0 > 0) {
            mIconView!!.iconLocationX
        } else {
            DisplayUtil.dip2px(mActivity, 92f).toInt()
        }
    }

    fun getIconLocationY(): Int {
        return if (mIconView?.iconLocationY ?: 0 > 0) {
            mIconView!!.iconLocationY
        } else {
            DisplayUtil.dip2px(mActivity, 92f).toInt()
        }
    }

    private fun getBasicWindowManagerLayoutParams(): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL)
        params.format = PixelFormat.TRANSLUCENT
        params.gravity = Gravity.LEFT or Gravity.TOP
        return params
    }

    private fun getWindowManager(): WindowManager {
        if (mWindowManager == null) {
            mWindowManager = mActivity!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }
        return mWindowManager!!
    }

    fun setIconHasNew(isNew: Boolean) {
        mIconView?.setHasNew(isNew)
    }

    fun setIconClickListener(listener: View.OnClickListener) {
        mIconView?.setClickListener(listener)
    }


    fun showIconWithpermission(result: PermissionManager.OnPermissionResult?) {
        if (mPermissionReqShow) {
            return
        }
        mPermissionReqShow = true
        PermissionManager.checkPermission(mActivity, true, object : PermissionManager.OnPermissionResult {
            override fun onFailed(msg: String) {
                result?.onFailed(msg)
                mPermissionReqShow = false
            }

            override fun onSuccess() {
                result?.onSuccess()
                showIcon()
                mPermissionReqShow = false
            }

            override fun onUserCancel(permission: String) {
                result?.onUserCancel(permission)
                mPermissionReqShow = false
            }

            override fun onUserDeny(permission: String) {
                result?.onUserDeny(permission)
                mPermissionReqShow = false
            }

        }, Manifest.permission.SYSTEM_ALERT_WINDOW)
    }

    @Synchronized
    fun showIcon(): Boolean {
        if (!PermissionManager.hasPermission(mActivity!!, Manifest.permission.SYSTEM_ALERT_WINDOW)) {
            Log.d(TAG, "showIcon checkFloatPermission is bad")
            return false
        }
        Log.d(TAG, "showIcon")
        if (null == mIconView) {
            Log.d(TAG, "mIconView is null")
            return false
        }
        if (!mIconView!!.hasBeenAdded) {
            //添加Icon
            val iconWindowParams = getBasicWindowManagerLayoutParams()
            iconWindowParams.width = IconView.sViewWidth
            iconWindowParams.height = IconView.sViewHeight
            val le = mIconView!!.iconLocationX
            val isLeft = le < DisplayUtil.getRealScreenSizeX(mActivity) / 2
            if (isLeft) {
                iconWindowParams.x = 0
            } else {
                iconWindowParams.x = DisplayUtil.getRealScreenSizeX(mActivity) - IconView.sViewWidth
            }
            iconWindowParams.y = mIconView!!.iconLocationY
            iconWindowParams.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)

            //总是出现在应用程序窗口之上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                iconWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                iconWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE
            }

            mIconView!!.layoutParams = iconWindowParams
            mIconView!!.setParams(iconWindowParams)

            if (mIconView!!.parent != null) {
                getWindowManager().updateViewLayout(mIconView, mIconView!!.layoutParams)
            } else {
                getWindowManager().addView(mIconView, mIconView!!.layoutParams)
            }
            //更新&缓存icon图标
            if (mIconUrl.isNotEmpty()) {
                mIconView!!.setIconImage(mIconUrl)
            }
        }
        mIconView!!.hasBeenAdded = true
        mIconView!!.visibility = View.VISIBLE
        mIconViewIsShowing = true
        mIconView!!.updateReddot()
        mIconView!!.showIcon()
        return true
    }

    fun getFullScreenFlag(): Int {
        return (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
    }

    fun getInputFlag(): Int {
        return (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun showViewCanInput(view: View, x: Int?, y: Int?) {
        showView(view, getInputFlag(), x, y)
    }

    fun showViewFullScreen(view: View, x: Int?, y: Int?) {
        showView(view, getFullScreenFlag(), x, y)
    }

    fun showViewWithpermission(view: View, flag: Int, x: Int?, y: Int?, result: PermissionManager.OnPermissionResult?) {
        if (mPermissionReqShow) {
            return
        }
        mPermissionReqShow = true
        PermissionManager.checkPermission(mActivity, true, object : PermissionManager.OnPermissionResult {
            override fun onFailed(msg: String) {
                result?.onFailed(msg)
                mPermissionReqShow = false
            }

            override fun onSuccess() {
                result?.onSuccess()
                showView(view, flag, x, y)
                mPermissionReqShow = false
            }

            override fun onUserCancel(permission: String) {
                result?.onUserCancel(permission)
                mPermissionReqShow = false
            }

            override fun onUserDeny(permission: String) {
                result?.onUserDeny(permission)
                mPermissionReqShow = false
            }

        }, Manifest.permission.SYSTEM_ALERT_WINDOW)
    }

    fun showView(view: View, flag: Int, x: Int?, y: Int?) {
        //添加Icon
        if (!PermissionManager.hasPermission(mActivity!!, Manifest.permission.SYSTEM_ALERT_WINDOW)) {
            Log.d(TAG, "showView checkFloatPermission is bad")
            return
        }
        val iconWindowParams = getBasicWindowManagerLayoutParams()
        if (null == view.layoutParams) {
            view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        iconWindowParams.width = view.layoutParams.width
        iconWindowParams.height = view.layoutParams.height
        iconWindowParams.x = x ?: DisplayUtil.dip2px(mActivity, 92f).toInt()
        iconWindowParams.y = y ?: DisplayUtil.dip2px(mActivity, 92f).toInt()
        iconWindowParams.flags = flag

        //总是出现在应用程序窗口之上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            iconWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            iconWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE
        }

        view.layoutParams = iconWindowParams
        if (view is BaseIconView) {
            view.setParams(iconWindowParams)
        }
        if (view.parent != null) {
            getWindowManager().updateViewLayout(view, view.layoutParams)
        } else {
            getWindowManager().addView(view, view.layoutParams)
        }
    }

    fun removeView(view: View) {
        if (view.parent != null) {
            getWindowManager().removeView(view)
        }
    }

    fun hideIcon() {
        Log.d(TAG, "hideIcon")
        mIconView?.visibility = View.GONE
        mIconViewIsShowing = false
    }

    fun hideIconWithAnimation(listener: Animation.AnimationListener?) {
        Log.d(TAG, "hideIcon")
        mIconView?.let {
            it.doIconClickAnimation(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    listener?.onAnimationStart(animation)
                }

                override fun onAnimationEnd(animation: Animation) {
                    it.visibility = View.INVISIBLE
                    listener?.onAnimationEnd(animation)
                }

                override fun onAnimationRepeat(animation: Animation) {
                    listener?.onAnimationRepeat(animation)
                }
            })

        }
        mIconViewIsShowing = false
    }

    fun closeIcon() {
        Log.d(TAG, "closeAllView")
        mIconView?.let {
            try {
                getWindowManager().removeView(mIconView)
            } catch (e: Exception) {

            }
        }
        mIconView?.apply {
            visibility = View.GONE
            clearViewAnimation()
            onDestroy()
        }

        mIconView = null
    }
}


