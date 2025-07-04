package com.bihe0832.android.framework.ui.main

import android.os.Bundle
import androidx.annotation.LayoutRes
import com.bihe0832.android.common.deprecated.R
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.lib.aaf.tools.AAFException
import me.yokeyword.fragmentation.ISupportFragment


open class CommonActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(getLayoutID())
    }

    /**
     * 布局layout
     * @return
     */
    protected open fun getLayoutID(): Int {
        return R.layout.common_activity_framelayout
    }

    final override fun setContentView(@LayoutRes layoutResID: Int) {
        throw AAFException("please extends com.bihe0832.android.framework.ui.BaseActivity instead of CommonActivity")
    }

    protected fun initToolbar(titleString: String?, needBack: Boolean) {
        initToolbar(titleString, needBack, 0)
    }

    protected fun initToolbar(titleString: String?, needBack: Boolean, iconRes: Int) {
        initToolbar(R.id.common_toolbar, titleString, true, needBack, iconRes)
    }

    protected fun loadRootFragment(toFragment: ISupportFragment) {
        super.loadRootFragment(R.id.common_fragment_content, toFragment)
    }
}