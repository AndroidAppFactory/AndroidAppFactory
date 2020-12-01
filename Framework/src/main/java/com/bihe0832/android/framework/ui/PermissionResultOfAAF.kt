package com.bihe0832.android.framework.ui

import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.permission.PermissionManager

/**
 * @author hardyshi code@bihe0832.com Created on 2020/12/1.
 */
class PermissionResultOfAAF : PermissionManager.OnPermissionResult {
    override fun onSuccess() {
        ZixieContext.showDebug("用户授权成功")
    }

    override fun onUserCancel() {
        ZixieContext.showLongToast("用户放弃授权")
        ZixieContext.exitAPP()
    }

    override fun onUserDeny() {
        ZixieContext.showDebug("用户拒绝授权")
        ZixieContext.exitAPP()
    }

    override fun onFailed(msg: String) {
        ZixieContext.showDebug("用户授权失败")
        ZixieContext.exitAPP()
    }
}