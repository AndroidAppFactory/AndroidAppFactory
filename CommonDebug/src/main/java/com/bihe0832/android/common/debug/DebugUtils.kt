package com.bihe0832.android.common.debug

import android.content.Context
import android.content.Intent
import com.bihe0832.android.lib.debug.DebugTools
import com.bihe0832.android.lib.ui.dialog.impl.DialogUtils
import com.bihe0832.android.lib.ui.dialog.input.InputDialogCompletedCallback

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
            listener: InputDialogCompletedCallback
    ) {
        DialogUtils.showInputDialog(context!!, titleName, msg, defaultValue, listener)
    }


    fun startDebugActivity(context: Context?, cls: Class<*>, titleName: String) {
        HashMap<String, String>().apply {
            put(DebugMainActivity.DEBUG_MODULE_CLASS_NAME, cls.name)
            put(DebugMainActivity.DEBUG_MODULE_TITLE_NAME, titleName)
        }.let {
            startActivityWithException(context, DebugMainActivity::class.java, it)
        }
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
}