package com.bihe0832.android.framework.ui.main

import android.os.Bundle
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.ui.BaseActivity


open class CommonActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.common_activity_framelayout)
    }

    protected fun initToolbar(titleString: String?, needBack: Boolean) {
        initToolbar(titleString, needBack, 0)
    }

    protected fun initToolbar(titleString: String?, needBack: Boolean, iconRes: Int) {
        initToolbar(R.id.common_toolbar, titleString, needBack, iconRes)
    }

}