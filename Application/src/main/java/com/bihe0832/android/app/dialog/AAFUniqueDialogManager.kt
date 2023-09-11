package com.bihe0832.android.app.dialog

import android.content.Context
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.android.lib.ui.dialog.tools.UniqueDialogManager

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/8/9.
 * Description: Description
 *
 */
object AAFUniqueDialogManager {

    val tipsUniqueDialogManager by lazy {
        UniqueDialogManager().apply {
            setShowDialogInterface(object : UniqueDialogManager.ShowDialogInterface {
                override fun showDialog(context: Context, title: String, content: String, positiveText: String, negativeText: String, canCancel: Boolean, listener: OnDialogListener): Boolean {
                    DialogUtils.showConfirmDialog(
                            context,
                            title,
                            content,
                            positiveText,
                            negativeText,
                            canCancel,
                            listener

                    )
                    return true
                }
            })
        }
    }
}