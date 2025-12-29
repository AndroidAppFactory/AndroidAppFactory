package com.bihe0832.android.lib.ui.dialog.tools

import android.content.Context
import android.text.TextUtils
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.R
import java.util.concurrent.CopyOnWriteArrayList
import com.bihe0832.android.lib.aaf.res.R as ResR


public class UniqueDialogManager {

    private val currentShowList = CopyOnWriteArrayList<String>()

    private var mShowDialogInterface: ShowDialogInterface? = null

    fun setShowDialogInterface(dialogInterface: ShowDialogInterface) {
        mShowDialogInterface = dialogInterface
    }

    interface ShowDialogInterface {
        fun showDialog(context: Context, title: String, content: String, positiveText: String, negativeText: String, canCancel: Boolean, listener: OnDialogListener): Boolean
    }

    fun showUniqueDialog(context: Context, content: String): Boolean {
        return showUniqueDialog(context, content, content, ThemeResourcesManager.getString(ResR.string.dialog_tips_button)!!)
    }

    fun showUniqueDialog(context: Context, content: String, buttonText: String): Boolean {
        return showUniqueDialog(context, content, content, buttonText)
    }

    fun showUniqueDialog(context: Context, id: String, content: String, buttonText: String): Boolean {
        return showUniqueDialog(context, id, "", content, buttonText)
    }

    fun showUniqueDialog(context: Context, id: String, title: String, content: String, buttonText: String): Boolean {
        return showUniqueDialog(context, id, title, content, buttonText, true)
    }

    fun showUniqueDialog(context: Context, id: String, title: String, content: String, buttonText: String, canCancel: Boolean, listener: (() -> Unit)? = null): Boolean {
        return showUniqueDialog(context, id, title, content, buttonText, "", canCancel, object :
            OnDialogListener {
            override fun onPositiveClick() {
                listener?.invoke()
            }

            override fun onNegativeClick() {
                onPositiveClick()
            }

            override fun onCancel() {
                onPositiveClick()
            }
        })
    }


    fun showUniqueDialog(context: Context, id: String, title: String, content: String, positiveText: String, negativeText: String, canCancel: Boolean, listener: OnDialogListener): Boolean {
        if (!TextUtils.isEmpty(id) && currentShowList.contains(id)) {
            return false
        } else {

            val dialogListener = object :
                OnDialogListener {
                override fun onPositiveClick() {
                    currentShowList.remove(id)
                    listener.onPositiveClick()
                }

                override fun onNegativeClick() {
                    currentShowList.remove(id)
                    listener.onNegativeClick()
                }

                override fun onCancel() {
                    currentShowList.remove(id)
                    listener.onCancel()
                }

            }
            var result = false
            if (mShowDialogInterface != null) {
                result = mShowDialogInterface!!.showDialog(context, title, content, positiveText, negativeText, canCancel, dialogListener)
            }

            if (!result) {
                DialogUtils.showConfirmDialog(context, title, content, positiveText, negativeText, canCancel, dialogListener)
                result = true
            }
            if (result) {
                currentShowList.add(id)
            }
            return result
        }
    }
}