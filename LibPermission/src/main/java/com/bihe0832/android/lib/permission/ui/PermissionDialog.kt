package com.bihe0832.android.lib.permission.ui

import android.content.Context
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener

/**
 *
 * @author zixie code@bihe0832.com Created on 12/24/20.
 *
 */
open class PermissionDialog : CommonDialog {

    override fun getLayoutID(): Int {
        return super.getLayoutID()
    }

    constructor(context: Context, themeResId: Int) : super(context, themeResId) {
        initContent()
    }

    constructor(context: Context) : super(context) {
        initContent()
    }

    private fun initContent() {
        title = PermissionManager.getTitle(context)
        negative = PermissionManager.getNegativeText(context)
        positive = PermissionManager.getPositiveText(context)
    }

    open fun show(sceneID: String, tempPermissionList: List<String>, canCancel: Boolean, listener: OnDialogListener) {
        showWithContent(PermissionManager.getPermissionContent(context, sceneID, tempPermissionList, true), canCancel, listener)
    }

    open fun show(scene: String, showPermission: String, canCancel: Boolean, listener: OnDialogListener) {
        var content = PermissionManager.getPermissionContent(context, scene, showPermission, true)
        showWithContent(content, canCancel, listener)
    }

    private fun showWithContent(content: String, canCancel: Boolean, listener: OnDialogListener) {
        setHtmlContent(content)
        setShouldCanceled(canCancel)
        setOnClickBottomListener(listener)
        show()
    }
}
