package com.bihe0832.android.framework.ui

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import com.bihe0832.android.framework.R
import com.bihe0832.android.lib.immersion.enableActivityImmersive
import com.bihe0832.android.lib.permission.PermissionManager
import me.yokeyword.fragmentation.SupportActivity

open class BaseActivity : SupportActivity() {

    var mToolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableActivityImmersive(getStatusBarColor(), getNavigationBarColor(), false)
    }

    open fun getStatusBarColor(): Int {
        return ContextCompat.getColor(this, R.color.colorPrimaryDark)
    }

    open fun getNavigationBarColor(): Int {
        return ContextCompat.getColor(this, R.color.navigationBarColor)
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

    override fun onBackPressedSupport() {
        onBackPressedSupportAction(exitAuto())
    }

    open fun onBack() {
        finish()
    }
}