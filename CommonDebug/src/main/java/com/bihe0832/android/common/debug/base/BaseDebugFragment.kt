package com.bihe0832.android.common.debug.base

import com.bihe0832.android.common.debug.DebugUtils
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.ui.dialog.callback.DialogCompletedStringCallback

open class BaseDebugFragment : BaseFragment() {
    val TAG = this.javaClass.simpleName

    protected fun sendInfo(title: String, content: String) {
        DebugUtils.sendInfo(context!!, title, content)
    }

    protected fun showInfo(title: String, content: List<String>) {
        DebugUtils.showInfo(context!!, title, content)
    }

    protected fun showInfoWithHTML(title: String, content: List<String>) {
        DebugUtils.showInfoWithHTML(context!!, title, content)
    }

    protected fun showInfo(title: String, content: String) {
        DebugUtils.showInfo(context!!, title, content)
    }

    fun showInputDialog(
            titleName: String,
            msg: String,
            defaultValue: String,
            listener: DialogCompletedStringCallback
    ) {
        DebugUtils.showInputDialog(context, titleName, msg, defaultValue, listener)
    }


    protected fun startDebugActivity(cls: Class<*>) {
        startDebugActivity(cls, "")
    }


    protected fun startDebugActivity(cls: Class<*>, titleName: String) {
        DebugUtils.startDebugActivity(context, cls, titleName)
    }

    protected open fun startActivityWithException(cls: String) {
        DebugUtils.startActivityWithException(context, cls)
    }

    protected open fun startActivityWithException(cls: Class<*>) {
        DebugUtils.startActivityWithException(context, cls)
    }

    protected open fun startActivityWithException(cls: Class<*>, data: Map<String, String>?) {
        DebugUtils.startActivityWithException(context, cls, data)
    }


}