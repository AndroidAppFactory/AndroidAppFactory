package com.bihe0832.android.lib.permission

import android.content.Context
import android.text.TextUtils
import com.bihe0832.android.lib.permission.PermissionManager.getPermissionDesc
import com.bihe0832.android.lib.permission.PermissionManager.getPermissionScene
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.utils.apk.APKUtils

/**
 *
 * @author hardyshi code@bihe0832.com Created on 12/24/20.
 *
 */
class PermissionDialog : CommonDialog {
    constructor(context: Context) : super(context) {
        title = context.resources.getString(R.string.permission_title)
        negative = context.resources.getString(R.string.permission_negtive)
        positive = context.resources.getString(R.string.permission_positive)
    }

    fun show(showPermission: String, canCancel: Boolean, listener: OnDialogListener) {
        var content = PermissionManager.getPermissionContent(showPermission)
        if (TextUtils.isEmpty(content)) {
            content = APKUtils.getAppName(context) + "的" + getPermissionScene(showPermission) + "功能需要您开启" + getPermissionDesc(showPermission) + "权限，缺少权限在使用中可能会出现部分功能异常。"
        }
        showWithContent(content, canCancel, listener)
    }

    private fun showWithContent(content: String, canCancel: Boolean, listener: OnDialogListener) {
        setHtmlContent(content)
        setShouldCanceled(canCancel)
        setOnClickBottomListener(listener)
        show()
    }
}
