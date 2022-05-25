/*
 * *
 *  * Created by zixie < code@bihe0832.com > on 2022/5/25 上午10:43
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/5/25 上午10:43
 *
 */
package com.bihe0832.android.lib.ui.dialog.impl

import android.content.Context
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.R
import com.bihe0832.android.lib.ui.dialog.input.InputDialog
import com.bihe0832.android.lib.ui.dialog.input.InputDialogCallback
import com.bihe0832.android.lib.ui.dialog.input.InputDialogCompletedCallback

/**
 * @author zixie code@bihe0832.com
 * Created on 2022/5/25.
 * Description: Description
 */
object DialogUtils {
    fun showInputDialog(context: Context?, titleName: String?, msg: String?, positive: String?,
                        negtive: String?, canCanceledOnTouchOutside: Boolean?, inputType: Int, defaultValue: String?, hint: String?,
                        listener: InputDialogCallback?) {
        InputDialog.showInputDialog(context, titleName, msg, positive, negtive, canCanceledOnTouchOutside, inputType, defaultValue, hint, listener)
    }

    fun showInputDialog(context: Context?, titleName: String?, msg: String?, defaultValue: String?,
                        listener: InputDialogCompletedCallback?) {
        InputDialog.showInputDialog(context, titleName, msg, defaultValue, listener)
    }

    fun showConfirmDialog(context: Context, message: String, canCancel: Boolean, callback: OnDialogListener) {
        showConfirmDialog(context, context.getString(R.string.dialog_title), message, context.getString(R.string.dialog_button_ok), context.getString(R.string.dialog_button_cancel), canCancel, callback)
    }

    fun showConfirmDialog(context: Context, title: String, message: String, canCancel: Boolean, callback: OnDialogListener) {
        showConfirmDialog(context, title, message, context.getString(R.string.dialog_button_ok), context.getString(R.string.dialog_button_cancel), canCancel, callback)
    }

    fun showConfirmDialog(context: Context, title: String, message: String, postiveStr: String?, negativeStr: String?, canCancel: Boolean, callback: OnDialogListener) {
        val dialog = CommonDialog(context)
        dialog.setTitle(title)
        dialog.setHtmlContent(message.toString())
        dialog.positive = postiveStr
        dialog.negative = negativeStr
        dialog.setOnClickBottomListener(object : OnDialogListener {
            override fun onPositiveClick() {
                dialog.dismiss()
                callback.onPositiveClick()
            }

            override fun onNegativeClick() {
                dialog.dismiss()
                callback.onNegativeClick()
            }

            override fun onCancel() {
                dialog.dismiss()
                callback.onCancel()
            }
        })
        dialog.setShouldCanceled(canCancel)
        dialog.show()
    }


    fun showAlertDialog(context: Context, title: String?, message: String?) {
        val dialog = CommonDialog(context)
        dialog.title = title
        dialog.setHtmlContent(message)
        dialog.positive = context.getString(R.string.dialog_button_ok)
        dialog.setOnClickBottomListener(object : OnDialogListener {
            override fun onPositiveClick() {
                dialog.dismiss()
            }

            override fun onNegativeClick() {
                dialog.dismiss()
            }

            override fun onCancel() {
                dialog.dismiss()
            }
        })
        dialog.setShouldCanceled(true)
        dialog.show()
    }

}