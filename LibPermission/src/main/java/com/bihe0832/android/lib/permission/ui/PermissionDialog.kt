package com.bihe0832.android.lib.permission.ui

import android.content.Context
import android.text.TextUtils
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.permission.PermissionManager.getPermissionContent
import com.bihe0832.android.lib.permission.PermissionManager.getPermissionDesc
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener

/**
 *
 * @author zixie code@bihe0832.com Created on 12/24/20.
 *
 */
open class PermissionDialog : CommonDialog {

    var useDefault: Boolean = true
    var needSpecial: Boolean = true

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

    open fun show(scene: String, tempPermissionList: List<String>, canCancel: Boolean, listener: OnDialogListener) {
        showWithContent(scene, tempPermissionList, canCancel, listener)
    }

    open fun show(scene: String, showPermissionGroupID: String, canCancel: Boolean, listener: OnDialogListener?) {
        showWithContent(scene, mutableListOf(showPermissionGroupID), canCancel, listener)
    }

    private fun showWithContent(scene: String, tempPermissionList: List<String>, canCancel: Boolean, listener: OnDialogListener?) {

        val permissionDesc: String = getPermissionDesc(scene, tempPermissionList, false, needSpecial)
        setTitle(permissionDesc + "权限使用说明")

        val permissionContent: String = getPermissionContent(context, scene, tempPermissionList, false, needSpecial)
        if (!TextUtils.isEmpty(permissionContent)) {
            setHtmlContent(permissionContent)
        } else {
            val defaultPermissionContent: String = getPermissionContent(context, scene, tempPermissionList, useDefault, needSpecial)
            setHtmlContent(defaultPermissionContent)
        }
        setShouldCanceled(canCancel)
        listener?.let {
            setOnClickBottomListener(listener)
        }
        show()
    }
}
