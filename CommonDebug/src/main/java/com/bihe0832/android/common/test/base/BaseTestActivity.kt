package com.bihe0832.android.common.test.base

import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.lib.utils.os.BuildUtils

open class BaseTestActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildUtils.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        }

    }
}
