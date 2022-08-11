package com.bihe0832.android.framework.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.constant.Constants
import com.bihe0832.android.lib.immersion.enableActivityImmersive
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.permission.ui.PermissionsActivity
import com.bihe0832.android.lib.permission.ui.PermissionsActivityV2
import com.bihe0832.android.lib.ui.common.ColorTools
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.os.DisplayUtil
import me.yokeyword.fragmentation.SupportActivity

open class BaseActivity : SupportActivity() {

    var mToolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resetDensity()) {
            DisplayUtil.resetDensity(this, ConvertUtils.parseFloat(resources.getString(R.string.custom_density), Constants.CUSTOM_DENSITY))
        }
        if (getStatusBarColor() == Color.TRANSPARENT) {
            enableActivityImmersive(ColorTools.getColorWithAlpha(0f, ContextCompat.getColor(this, R.color.primary)), getNavigationBarColor())
        } else {
            enableActivityImmersive(getStatusBarColor(), getNavigationBarColor())
        }
    }


    open fun resetDensity(): Boolean {
        return true
    }

    open fun getStatusBarColor(): Int {
        return ContextCompat.getColor(this, R.color.colorPrimary)
    }

    open fun getNavigationBarColor(): Int {
        return ContextCompat.getColor(this, R.color.navigationBarColor)
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
                    setNavigationOnClickListener { onBackPressed() }
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

    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * 页面返回时的特殊处理，例如检查是否是唯一页面，如果是需要跳转到main
     */
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