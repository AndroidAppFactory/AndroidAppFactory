package com.bihe0832.android.base.debug.permission

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.lib.permission.ui.PermissionDialog
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener

/**
 *
 * @author zixie code@bihe0832.com Created on 12/24/20.
 *
 */
class  DebugPermissionDialog : PermissionDialog {

    constructor(context: Context) : super(context)


    override fun getLayoutID(): Int {
        return R.layout.dialog_test_permission
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<TextView>(R.id.dialog_content).setOnClickListener {
            onClickBottomListener?.onPositiveClick()
        }
        findViewById<TextView>(R.id.dialog_title).setOnClickListener {
            onClickBottomListener?.onNegativeClick()
        }
    }

    override fun show(scene: String, showPermissionGroupID: String, canCancel: Boolean, listener: OnDialogListener?) {
        shouldCanceled = canCancel
        setOnClickBottomListener(listener)
        show()
    }

    override fun show(scene: String, tempPermissionList: List<String>, canCancel: Boolean, listener: OnDialogListener) {
        shouldCanceled = canCancel
        setOnClickBottomListener(listener)
        show()
    }
}
