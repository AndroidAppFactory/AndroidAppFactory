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

    override fun onUserCancel(scene: String, permission: String) {
        ZLog.d("你已放弃授权" + PermissionManager.getPermissionDesc(scene, permission) +"权限，将会影响" + PermissionManager.getPermissionScene(scene, permission) + "，如有需要请手动前往应用设置开启")
        ZixieContext.exitAPP()
    }

    override fun onUserDeny(scene: String, permission: String) {
        ZLog.d("你拒绝授权" + PermissionManager.getPermissionDesc(scene, permission) +"权限，将会影响" + PermissionManager.getPermissionScene(scene, permission) + "，如有需要请手动前往应用设置开启")
        ZixieContext.exitAPP()
    }

    override fun onFailed(msg: String) {
        ZLog.d("用户授权失败")
        ZixieContext.exitAPP()
    }
}