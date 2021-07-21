package com.bihe0832.android.framework.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import com.bihe0832.android.framework.R
import com.bihe0832.android.lib.immersion.enableActivityImmersive
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.permission.PermissionManager
import me.yokeyword.fragmentation.SupportActivity

open class BaseActivity : SupportActivity() {

    var mToolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableActivityImmersive(getStatusBarColor(), getNavigationBarColor())
    }

    open fun getStatusBarColor(): Int {
        return ContextCompat.getColor(this, R.color.colorPrimary)
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

    protected fun updateTitle(titleString: String?) {
        try {
            titleString?.let {
                mToolbar?.apply {
                    title = it
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected fun initToolbar(resID: Int, needBack: Boolean) {
        initToolbar(resID, null, needBack)
    }

    protected fun initToolbar(resID: Int, titleString: String?, needBack: Boolean) {
        initToolbar(resID, titleString, needBack, 0)
    }

    protected fun initToolbar(resID: Int, titleString: String?, needBack: Boolean, iconRes: Int) {
        try {
            if (null == mToolbar) {
                mToolbar = findViewById(resID)
            }
            updateTitle(titleString)
            mToolbar?.apply {
                if (needBack) {
                    setNavigationOnClickListener { onBackPressedSupport() }
                } else {
                    if (iconRes > 0) {
                        setNavigationIcon(iconRes)
                    } else {
                        setNavigationIcon(R.mipmap.icon)
                    }

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

    override fun finish() {
        intent = null
        super.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ZLog.d("onActivityResult： $this, $requestCode, $resultCode, ${data?.data}")
        try {
            for (fragment in supportFragmentManager.fragments) {
                fragment.onActivityResult(requestCode, resultCode, data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}