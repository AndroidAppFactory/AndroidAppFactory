package com.bihe0832.android.common.qrcode

import android.Manifest
import android.os.Build
import android.view.View
import com.bihe0832.android.common.permission.AAFPermissionManager
import com.bihe0832.android.common.permission.PermissionResultOfAAF
import com.bihe0832.android.common.photos.getPhotoContent
import com.bihe0832.android.common.qrcode.core.BaseScanFragment
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.media.image.CheckedEnableImageView
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.permission.ui.PermissionsActivityV2
import com.bihe0832.android.lib.utils.os.BuildUtils

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/8/28.
 * Description: Description
 *
 */
open class CommonScanFragment : BaseScanFragment() {
    private var userDeny = false

    fun startScanAction() {
        super.startCamera()
    }

    override fun startCamera() {
        if (PermissionManager.isAllPermissionOK(context!!, Manifest.permission.CAMERA)) {
            startScanAction()
        } else if (!userDeny) {
            PermissionManager.checkPermission(
                context!!,
                RouterConstants.MODULE_NAME_QRCODE_SCAN,
                true,
                PermissionsActivityV2::class.java,
                object : com.bihe0832.android.common.permission.PermissionResultOfAAF(false) {
                    override fun onSuccess() {
                        startScanAction()
                    }

                    override fun onUserCancel(
                        scene: String,
                        permissionGroupID: String,
                        permission: String
                    ) {
                        super.onUserDeny(scene, permissionGroupID, permission)
                        userDeny = true
                    }

                    override fun onFailed(msg: String) {
                        super.onFailed(msg)
                        userDeny = true
                    }

                    override fun onUserDeny(
                        scene: String,
                        permissionGroupID: String,
                        permission: String
                    ) {
                        super.onUserDeny(scene, permissionGroupID, permission)
                        userDeny = true
                    }
                },
                AAFPermissionManager.takePhotoPermission,
            )
        }
    }

    override fun initAlbumAction(btnAlbum: CheckedEnableImageView) {
        btnAlbum.setOnClickListener { view: View? ->
            if (BuildUtils.SDK_INT >= Build.VERSION_CODES.Q) {
                getPhotoContent()
            } else {
                PermissionManager.checkPermission(
                    context!!,
                    RouterConstants.MODULE_NAME_QRCODE_SCAN,
                    false,
                    object : PermissionResultOfAAF(false) {
                        override fun onSuccess() {
                            getPhotoContent()
                        }

                        override fun onUserCancel(
                            scene: String,
                            permissionGroupID: String,
                            permission: String
                        ) {
                            super.onUserDeny(scene, permissionGroupID, permission)
                            userDeny = true
                        }

                        override fun onFailed(msg: String) {
                            super.onFailed(msg)
                            userDeny = true
                        }

                        override fun onUserDeny(
                            scene: String,
                            permissionGroupID: String,
                            permission: String
                        ) {
                            super.onUserDeny(scene, permissionGroupID, permission)
                            userDeny = true
                        }
                    },
                    AAFPermissionManager.selectPhotoPermission,
                )
            }
        }
    }
}