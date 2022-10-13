package com.bihe0832.android.base.debug.permission

import android.content.Context
import android.os.Bundle
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.lib.permission.ui.PermissionDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import kotlinx.android.synthetic.main.dialog_test_permission.*

/**
 *
 * @author zixie code@bihe0832.com Created on 12/24/20.
 *
 */
class DebugPermissionDialog : PermissionDialog {

    constructor(context: Context) : super(context)


    override fun getLayoutID(): Int {
        return R.layout.dialog_test_permission
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialog_content.setOnClickListener {
            onClickBottomListener?.onPositiveClick()
        }
        dialog_title.setOnClickListener {
            onClickBottomListener?.onNegativeClick()
        }
    }

    override fun show(scene: String, showPermissionGroupID: String, canCancel: Boolean, listener: OnDialogListener?) {
        setShouldCanceled(canCancel)
        setOnClickBottomListener(listener)
        show()
    }

    override fun show(scene: String, tempPermissionGroupList: List<String>, canCancel: Boolean, listener: OnDialogListener) {
        setShouldCanceled(canCancel)
        setOnClickBottomListener(listener)
        show()
    }
}
