package com.bihe0832.android.framework.ui

import com.bihe0832.android.framework.R as FrameworkR


/**
 * 建议主题使用 AAF.ActivityTheme.Bottom
 */
open class BaseBottomActivity : BaseActivity() {

    override fun finish() {
        super.finish()
        overridePendingTransition(0, getExitAnimation())
    }

    fun getExitAnimation(): Int {
        return FrameworkR.anim.fade_out
    }
}