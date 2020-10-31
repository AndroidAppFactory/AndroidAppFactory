package com.bihe0832.android.framework.base

import android.os.Bundle
import com.bihe0832.android.framework.R


open class CommonActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.common_activity_framelayout)
    }

    protected fun initToolbar(titleString: String?, needBack: Boolean) {
        initToolbar(R.id.common_toolbar, titleString, needBack)
    }

}