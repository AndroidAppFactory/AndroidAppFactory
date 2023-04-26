package com.bihe0832.android.app.permission

import android.Manifest
import com.bihe0832.android.common.photos.takePhotoPermission
import com.bihe0832.android.lib.permission.PermissionManager

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2023/4/26.
 * Description: Description
 *
 */
object AAFPermissionManager {
    fun initPermission() {

        PermissionManager.addPermissionGroup("", Manifest.permission.CAMERA, takePhotoPermission)
        PermissionManager.addPermissionGroupDesc("", Manifest.permission.CAMERA, "相机")
        PermissionManager.addPermissionGroupScene("", Manifest.permission.CAMERA, "扫描、识别二维码")

    }
}