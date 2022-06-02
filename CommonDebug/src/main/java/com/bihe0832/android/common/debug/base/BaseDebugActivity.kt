package com.bihe0832.android.common.debug.base

import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.lib.utils.os.BuildUtils

open class BaseDebugActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildUtils.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        }

    }
}
