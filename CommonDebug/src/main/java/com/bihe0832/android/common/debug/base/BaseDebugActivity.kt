package com.bihe0832.android.common.debug.base

import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import com.bihe0832.android.common.compose.debug.DebugUtils
import com.bihe0832.android.common.debug.module.DebugRootActivity
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.lib.ui.dialog.callback.DialogCompletedStringCallback
import com.bihe0832.android.lib.utils.os.BuildUtils

open class BaseDebugActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildUtils.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        }

    }

    protected fun sendInfo(title: String, content: String) {
        DebugUtils.sendInfo(this, title, content)
    }

    protected fun showInfo(title: String, content: List<String>) {
        DebugUtils.showInfo(this, title, content)
    }

    protected fun showInfoWithHTML(title: String, content: List<String>) {
        DebugUtils.showInfoWithHTML(this, title, content)
    }

    protected fun showInfo(title: String, content: String) {
        DebugUtils.showInfo(this, title, content)
    }

    fun showInputDialog(
            titleName: String,
            msg: String,
            defaultValue: String,
            listener: DialogCompletedStringCallback
    ) {
        DebugUtils.showInputDialog(this, titleName, msg, defaultValue, listener)
    }


    protected fun startDebugActivity(cls: Class<*>) {
        startDebugActivity(cls, "")
    }


    protected fun startDebugActivity(cls: Class<*>, titleName: String) {
        DebugRootActivity.startDebugRootActivity(this, cls, titleName)
    }

    protected open fun startActivityWithException(cls: String) {
        DebugUtils.startActivityWithException(this, cls)
    }

    protected open fun startActivityWithException(cls: Class<*>) {
        DebugUtils.startActivityWithException(this, cls)
    }

    protected open fun startActivityWithException(cls: Class<*>, data: Map<String, String>?) {
        DebugUtils.startActivityWithException(this, cls, data)
    }

}
