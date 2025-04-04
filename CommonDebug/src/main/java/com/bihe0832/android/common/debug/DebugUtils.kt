package com.bihe0832.android.common.debug

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.bihe0832.android.common.debug.module.DebugRootActivity
import com.bihe0832.android.lib.debug.DebugTools
import com.bihe0832.android.lib.ui.dialog.callback.DialogCompletedStringCallback
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils

/**
 * @author zixie code@bihe0832.com
 * Created on 2022/10/28.
 * Description: Description
 */
object DebugUtils {

    fun sendInfo(context: Context?, title: String, content: String) {
        DebugTools.sendInfo(context, title, content, false)
    }

    fun showInfo(context: Context?, title: String, content: List<String>) {
        StringBuilder().apply {
            content.forEach {
                append("$it\n")
            }
        }.let {
            DebugTools.showInfo(context, title, it.toString(), "发送到第三方")
        }
    }

    fun showInfoWithHTML(context: Context?, title: String, content: List<String>) {
        StringBuilder().apply {
            content.forEach {
                append("$it<BR>")
            }
        }.let {
            DebugTools.showInfoWithHTML(context, title, it.toString(), "发送到第三方")
        }
    }

    fun showInfo(context: Context?, title: String, content: String) {
        DebugTools.showInfo(context, title, content, "发送到第三方")
    }

    fun showInputDialog(
        context: Context?,
        titleName: String,
        msg: String,
        defaultValue: String,
        listener: DialogCompletedStringCallback,
    ) {
        DialogUtils.showInputDialog(context!!, titleName, msg, defaultValue, listener)
    }

    fun startDebugActivity(context: Context?, cls: Class<*>, titleName: String) {
        DebugRootActivity.startDebugRootActivity(context, cls, titleName)
    }

    fun startDebugActivity(
        context: Context?,
        cls: Class<*>,
        titleName: String,
        data: Map<String, String>?
    ) {
        DebugRootActivity.startDebugRootActivity(context, cls, titleName, data)
    }

    fun startActivityWithException(context: Context?, cls: String) {
        startActivityWithException(context, Class.forName(cls))
    }

    fun startActivityWithException(context: Context?, cls: Class<*>) {
        startActivityWithException(context, cls, null)
    }

    fun startActivityWithException(context: Context?, cls: Class<*>, data: Map<String, String>?) {
        val intent = Intent(context, cls)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        data?.let {
            for ((key, value) in it) {
                intent.putExtra(key, value)
            }
        }
        context?.startActivity(intent)
    }

    fun startActivityForResultWithException(context: Activity, cls: Class<*>, requestCode: Int) {
        startActivityForResultWithException(context, cls, requestCode, null)
    }

    fun startActivityForResultWithException(fragment: Fragment, cls: Class<*>, requestCode: Int) {
        startActivityForResultWithException(fragment, cls, requestCode, null)
    }

    private fun getIntent(context: Context, cls: Class<*>, data: Map<String, String>?): Intent {
        val intent = Intent(context, cls)
        data?.let {
            for ((key, value) in it) {
                intent.putExtra(key, value)
            }
        }
        return intent
    }

    fun startActivityForResultWithException(
        activity: Activity,
        cls: Class<*>,
        requestCode: Int,
        data: Map<String, String>?,
    ) {
        val intent = getIntent(activity, cls, data)
        activity.startActivityForResult(intent, requestCode)
    }

    fun startActivityForResultWithException(
        fragment: Fragment,
        cls: Class<*>,
        requestCode: Int,
        data: Map<String, String>?,
    ) {
        val intent = getIntent(fragment.context!!, cls, data)
        fragment.startActivityForResult(intent, requestCode)
    }
}
