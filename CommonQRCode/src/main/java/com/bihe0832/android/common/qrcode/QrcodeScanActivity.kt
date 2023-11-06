package com.bihe0832.android.common.qrcode

import android.Manifest
import android.os.Build
import android.view.View
import com.bihe0832.android.common.photos.getPhotoContent
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.media.image.CheckedEnableImageView
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.permission.ui.PermissionsActivityV2
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.google.zxing.activity.BaseCaptureActivity

@Module(RouterConstants.MODULE_NAME_QRCODE_SCAN)
open class QrcodeScanActivity : BaseCaptureActivity() {
    init {
        PermissionManager.addPermissionGroup(
            RouterConstants.MODULE_NAME_QRCODE_SCAN,
            Manifest.permission.CAMERA,
            com.bihe0832.android.common.permission.AAFPermissionManager.takePhotoPermission,
        )
        PermissionManager.addPermissionGroupDesc(
            RouterConstants.MODULE_NAME_QRCODE_SCAN,
            Manifest.permission.CAMERA,
            "相机",
        )
        PermissionManager.addPermissionGroupScene(
            RouterConstants.MODULE_NAME_QRCODE_SCAN,
            Manifest.permission.CAMERA,
            "扫描、识别二维码",
        )
    }

    private var userDeny = false

    override fun startScan() {
        if (PermissionManager.isAllPermissionOK(this, Manifest.permission.CAMERA)) {
            startScanAction()
        } else if (!userDeny) {
            PermissionManager.checkPermission(
                this,
                RouterConstants.MODULE_NAME_QRCODE_SCAN,
                true,
                PermissionsActivityV2::class.java,
                object : com.bihe0832.android.common.permission.PermissionResultOfAAF(false) {
                    override fun onSuccess() {
                        startScanAction()
                    }

                    override fun onUserCancel(scene: String, permissionGroupID: String, permission: String) {
                        super.onUserDeny(scene, permissionGroupID, permission)
                        userDeny = true
                    }

                    override fun onFailed(msg: String) {
                        super.onFailed(msg)
                        userDeny = true
                    }

                    override fun onUserDeny(scene: String, permissionGroupID: String, permission: String) {
                        super.onUserDeny(scene, permissionGroupID, permission)
                        userDeny = true
                    }
                },
                com.bihe0832.android.common.permission.AAFPermissionManager.takePhotoPermission,
            )
        }
    }

    override fun initAlbumAction(btnAlbum: CheckedEnableImageView) {
        btnAlbum.setOnClickListener { view: View? ->
            if (BuildUtils.SDK_INT >= Build.VERSION_CODES.Q) {
                getPhotoContent()
            } else {
                PermissionManager.checkPermission(
                    this,
                    RouterConstants.MODULE_NAME_QRCODE_SCAN,
                    false,
                    object : com.bihe0832.android.common.permission.PermissionResultOfAAF(false) {
                        override fun onSuccess() {
                            getPhotoContent()
                        }

                        override fun onUserCancel(scene: String, permissionGroupID: String, permission: String) {
                            super.onUserDeny(scene, permissionGroupID, permission)
                            userDeny = true
                        }

                        override fun onFailed(msg: String) {
                            super.onFailed(msg)
                            userDeny = true
                        }

                        override fun onUserDeny(scene: String, permissionGroupID: String, permission: String) {
                            super.onUserDeny(scene, permissionGroupID, permission)
                            userDeny = true
                        }
                    },
                    com.bihe0832.android.common.permission.AAFPermissionManager.selectPhotoPermission,
                )
            }
        }
    }
}
