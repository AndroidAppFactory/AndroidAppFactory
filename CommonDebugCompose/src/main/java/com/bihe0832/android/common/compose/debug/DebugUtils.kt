package com.bihe0832.android.common.compose.debug

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.bihe0832.android.lib.ui.dialog.callback.DialogCompletedStringCallback
import com.bihe0832.android.lib.ui.dialog.senddata.SendTextUtils
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils

/**
 * @author zixie code@bihe0832.com
 * Created on 2022/10/28.
 * Description: Description
 */
object DebugUtils {

    fun sendInfo(
        context: Context, title: String?, content: String?, showDialog: Boolean
    ) {
        SendTextUtils.sendInfo(
            context,
            title,
            content,
            null,
            null,
            context.getString(R.string.com_bihe0832_share_to_develop_tips),
            content,
            context.getString(R.string.com_bihe0832_share_to_develop),
            showDialog
        )
    }

    fun sendInfo(context: Context, title: String, content: String) {
        sendInfo(context, title, content, true)
    }

    fun showInfo(context: Context, title: String, content: List<String>) {
        sendInfo(context, title, content.joinToString("\n"), true)
    }

    fun showInfoWithHTML(context: Context, title: String, content: List<String>) {
        SendTextUtils.sendInfoWithHTML(
            context,
            title,
            content.joinToString("<BR>"),
            context.getString(R.string.com_bihe0832_share_to_develop_tips),
            context.getString(R.string.com_bihe0832_share_to_develop),
            true
        )
    }

    fun showInfo(context: Context, title: String, content: String) {
        sendInfo(context, title, content, true)
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

    fun startActivityWithException(context: Context?, cls: String) {
        startActivityWithException(context, Class.forName(cls))
    }

    fun startActivityWithException(context: Context?, cls: Class<*>) {
        startActivityWithException(context, cls, null)
    }

    fun startActivityWithException(context: Context?, cls: Class<*>, data: Map<String, String>?) {
        DebugComposeRootActivity.startActivityWithException(context, cls, data)
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
