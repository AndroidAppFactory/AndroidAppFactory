package com.bihe0832.android.base.test.permission

import android.content.Context
import android.os.Bundle
import com.bihe0832.android.base.test.R
import com.bihe0832.android.lib.permission.ui.PermissionDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import kotlinx.android.synthetic.main.dialog_test_layout.*

/**
 *
 * @author hardyshi code@bihe0832.com Created on 12/24/20.
 *
 */
class TestPermissionDialog : PermissionDialog {

    constructor(context: Context) : super(context)

    private var mListener: OnDialogListener? = null

    override fun getLayoutID(): Int {
        return R.layout.dialog_test_layout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialog_content.setOnClickListener {
            mListener?.onPositiveClick()
        }
        dialog_title.setOnClickListener {
            mListener?.onNegativeClick()
        }
    }

    override fun show(showPermission: String, canCancel: Boolean, listener: OnDialogListener) {
        mListener = listener
        setShouldCanceled(canCancel)
        setOnClickBottomListener(listener)
        show()
    }
}
