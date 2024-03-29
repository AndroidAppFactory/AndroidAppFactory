package com.bihe0832.android.common.permission

import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.permission.PermissionManager

/**
 * @author zixie code@bihe0832.com Created on 2020/12/1.
 */
open class PermissionResultOfAAF(private val exist: Boolean) : PermissionManager.OnPermissionResult {
    override fun onSuccess() {
        ZLog.d("授权成功")
    }

    override fun onUserCancel(scene: String, permissionGroupID: String,permission: String) {
        ZLog.d("onUserCancel scene:$scene, permissionGroupID: $permissionGroupID, permission:$permission")
        ZLog.d("你已放弃授权" + PermissionManager.getPermissionDesc(scene, permissionGroupID,true,true) + "权限，将会影响" + PermissionManager.getPermissionScene(scene, permissionGroupID,true,true) + "，如有需要请手动前往应用设置开启")
        if (exist) {
            ZixieContext.exitAPP()
        }
    }

    override fun onUserDeny(scene: String, permissionGroupID: String,permission: String) {
        ZLog.d("onUserCancel scene:$scene, permissionGroupID: $permissionGroupID, permission:$permission")
        ZLog.d("你拒绝授权" + PermissionManager.getPermissionDesc(scene, permissionGroupID,true,true) + "权限，将会影响" + PermissionManager.getPermissionScene(scene, permissionGroupID,true,true) + "，如有需要请手动前往应用设置开启")
        if (exist) {
            ZixieContext.exitAPP()
        }
    }

    override fun onFailed(msg: String) {
        ZLog.d("用户授权失败")
        if (exist) {
            ZixieContext.exitAPP()
        }
    }
}