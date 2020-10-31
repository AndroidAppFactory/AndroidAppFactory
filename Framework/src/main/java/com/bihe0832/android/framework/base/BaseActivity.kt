package com.bihe0832.android.framework.base

import android.os.Build
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.WindowManager
import com.bihe0832.android.framework.R
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

    override fun onResume() {
        super.onResume()
        for (fragment in supportFragmentManager.fragments) {
            if (fragment.isAdded) {
                fragment.userVisibleHint = true
            }
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

    protected fun initToolbar(resID :Int, titleString: String?, needBack: Boolean) {
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
    override fun onBackPressedSupport() {
        onBackPressedSupportAction(exitAuto())
    }

    open fun onBack() {
        finish()
    }
}