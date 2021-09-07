package com.bihe0832.android.framework.ui

import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.permission.PermissionManager

/**
 * @author hardyshi code@bihe0832.com Created on 2020/12/1.
 */
class PermissionResultOfAAF : PermissionManager.OnPermissionResult {
    override fun onSuccess() {
        ZLog.d("授权成功")
    }

    override fun onUserCancel(permission: String) {
        ZLog.d("你已放弃授权" + PermissionManager.getPermissionDesc(permission) + "，请手动前往应用设置开启")
        ZixieContext.exitAPP()
    }

    override fun onUserDeny(permission: String) {
        ZLog.d("你拒绝了" + PermissionManager.getPermissionDesc(permission) + "权限的申请，请手动前往应用设置开启")
        ZixieContext.exitAPP()
    }

    override fun onFailed(msg: String) {
        ZLog.d("用户授权失败")
        ZixieContext.exitAPP()
    }
}