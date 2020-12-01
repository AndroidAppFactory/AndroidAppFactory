package com.bihe0832.android.framework.ui

import android.os.Build
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.WindowManager
import com.bihe0832.android.framework.R
import com.bihe0832.android.lib.permission.PermissionManager
import me.yokeyword.fragmentation.SupportActivity

open class BaseActivity : SupportActivity() {

    var mToolbar: Toolbar? = null
    val CREATE_TIME = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            //透明导航栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
    }

    open fun getPermissionList(): List<String> {
        return ArrayList()
    }

    open fun getPermissionResult(): PermissionManager.OnPermissionResult {
        return PermissionResultOfAAF()
    }

    override fun onResume() {
        super.onResume()
        if (getPermissionList().isNotEmpty()) {
            PermissionManager.checkPermission(this, false, getPermissionResult(), *getPermissionList().toTypedArray())
        }
    }

    override fun onPause() {
        super.onPause()
        for (fragment in supportFragmentManager.fragments) {
            if (fragment.isAdded) {
                fragment.userVisibleHint = false
            }
        }
    }

    protected open fun exitAuto(): Boolean {
        return true
    }

    protected fun initToolbar(resID: Int, titleString: String?, needBack: Boolean) {
        try {
            if (null == mToolbar) {
                mToolbar = findViewById(resID)
            }
            mToolbar?.apply {
                title = titleString
                if (needBack) {
                    setNavigationOnClickListener { onBackPressedSupport() }
                } else {
                    setNavigationIcon(R.mipmap.icon)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    protected fun hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) { // lower api
            this.window.decorView?.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val decorView = window.decorView
            val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    or View.SYSTEM_UI_FLAG_IMMERSIVE)
            decorView.systemUiVisibility = uiOptions

        }
    }

    override fun onBackPressedSupport() {
        onBackPressedSupportAction(exitAuto())
    }

    open fun onBack() {
        finish()
    }
}