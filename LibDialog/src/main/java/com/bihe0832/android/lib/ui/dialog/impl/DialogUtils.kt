/*
 * *
 *  * Created by zixie < code@bihe0832.com > on 2022/5/25 上午10:43
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/5/25 上午10:43
 *
 */
package com.bihe0832.android.lib.ui.dialog.impl

import android.content.Context
import android.view.inputmethod.EditorInfo
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.R
import com.bihe0832.android.lib.ui.dialog.input.InputDialogCallback
import com.bihe0832.android.lib.ui.dialog.input.InputDialogCompletedCallback
import com.bihe0832.android.lib.ui.dialog.input.InputDialogUtils

/**
 * @author zixie code@bihe0832.com
 * Created on 2022/5/25.
 * Description: Description
 */
object DialogUtils {

    fun showInputDialog(context: Context, titleName: String, msg: String, positive: String, negtive: String, canCanceledOnTouchOutside: Boolean, inputType: Int, defaultValue: String, hint: String, listener: InputDialogCallback?) {
        InputDialogUtils.showInputDialog(context, titleName, msg, positive, negtive, canCanceledOnTouchOutside, inputType, defaultValue, hint, listener)
    }

    fun showInputDialog(context: Context, title: String, msg: String, hint: String, value: String, inputType: Int, callback: InputDialogCompletedCallback?) {
        showInputDialog(context, title, msg, context.getString(R.string.dialog_button_ok), "", true, if (inputType == 0) EditorInfo.TYPE_CLASS_TEXT else inputType, value, hint, object : InputDialogCallback {
            override fun onPositiveClick(input: String) {
                callback?.onInputCompleted(input)
            }

            override fun onNegativeClick(s: String) {

            }

            override fun onCancel(s: String) {

            }
        })
    }


    fun showInputDialog(context: Context, titleName: String, msg: String, defaultValue: String, listener: InputDialogCompletedCallback?) {
        showInputDialog(context, titleName, msg, context.getString(R.string.dialog_input_hint), defaultValue, EditorInfo.TYPE_CLASS_TEXT, listener)
    }

    fun showInputDialog(context: Context, titleName: String, defaultValue: String, listener: InputDialogCompletedCallback?) {
        showInputDialog(context, titleName, "", defaultValue, listener)
    }

    fun showConfirmDialog(context: Context, title: String, message: String, positiveStr: String?, negativeStr: String?, canCancel: Boolean, callback: OnDialogListener) {
        val dialog = CommonDialog(context)
        dialog.setTitle(title)
        dialog.setHtmlContent(message)
        dialog.positive = positiveStr
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

    fun showConfirmDialog(context: Context, title: String, message: String, positiveStr: String?, negativeStr: String?, callback: OnDialogListener) {
        showConfirmDialog(context, title, message, positiveStr, negativeStr, true, callback)
    }

    fun showConfirmDialog(context: Context, title: String, message: String, canCancel: Boolean, callback: OnDialogListener) {
        showConfirmDialog(context, title, message, context.getString(R.string.dialog_button_ok), context.getString(R.string.dialog_button_cancel), canCancel, callback)
    }

    fun showConfirmDialog(context: Context, message: String, canCancel: Boolean, callback: OnDialogListener) {
        showConfirmDialog(context, context.getString(R.string.dialog_title), message, context.getString(R.string.dialog_button_ok), context.getString(R.string.dialog_button_cancel), canCancel, callback)
    }

    fun showAlertDialog(context: Context, title: String?, message: String?, positive: String, canCancel: Boolean, callback: OnDialogListener?) {
        val dialog = CommonDialog(context)
        dialog.title = title
        dialog.setHtmlContent(message)
        dialog.positive = positive
        dialog.shouldCanceled = canCancel
        dialog.setOnClickBottomListener(object : OnDialogListener {
            override fun onPositiveClick() {
                dialog.dismiss()
                callback?.onPositiveClick()
            }

            override fun onNegativeClick() {
                dialog.dismiss()
                callback?.onNegativeClick()
            }

            override fun onCancel() {
                dialog.dismiss()
                callback?.onCancel()
            }
        })
        dialog.show()
    }

    fun showAlertDialog(context: Context, title: String?, message: String?, canCancel: Boolean, callback: OnDialogListener?) {
        showAlertDialog(context, title, message, context.getString(R.string.dialog_tips_button), canCancel, callback)
    }

    fun showAlertDialog(context: Context, title: String?, message: String?, canCancel: Boolean) {
        showAlertDialog(context, title, message, canCancel, null)
    }

    fun showAlertDialog(context: Context, message: String?) {
        showAlertDialog(context, context.getString(R.string.dialog_title), message, true)
    }

}