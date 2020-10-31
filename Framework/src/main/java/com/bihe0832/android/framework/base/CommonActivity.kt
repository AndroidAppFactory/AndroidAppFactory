package com.bihe0832.android.framework.base

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.bihe0832.android.framework.R


open class CommonActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.common_activity_framelayout)
    }

    protected fun initToolbar(titleString: String?, needBack: Boolean) {
        try {
            if (null == mToolbar) {
                mToolbar = findViewById(R.id.common_toolbar)
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
}