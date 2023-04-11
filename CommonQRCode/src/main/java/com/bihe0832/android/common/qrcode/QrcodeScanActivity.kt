package com.bihe0832.android.common.qrcode

import android.Manifest
import android.os.Build
import android.view.View
import com.bihe0832.android.common.photos.getPhotoContent
import com.bihe0832.android.common.photos.selectPhotoPermission
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.framework.permission.PermissionResultOfAAF
import com.bihe0832.android.lib.media.image.CheckedEnableImageView
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.permission.ui.PermissionsActivityV2
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.utils.os.BuildUtils
import com.google.zxing.activity.BaseCaptureActivity

@Module(RouterConstants.MODULE_NAME_QRCODE_SCAN)
class QrcodeScanActivity : BaseCaptureActivity() {
    init {
        PermissionManager.addPermissionGroup(RouterConstants.MODULE_NAME_QRCODE_SCAN, Manifest.permission.CAMERA, mutableListOf<String>().apply {
            add(Manifest.permission.CAMERA)
        })
        PermissionManager.addPermissionGroupDesc(RouterConstants.MODULE_NAME_QRCODE_SCAN, Manifest.permission.CAMERA, "相机")
        PermissionManager.addPermissionGroupScene(RouterConstants.MODULE_NAME_QRCODE_SCAN, Manifest.permission.CAMERA, "扫描、识别二维码")
    }

    private var userDeny = false

    override fun startScan() {
        if (PermissionManager.isAllPermissionOK(this, Manifest.permission.CAMERA)) {
            startScanAction()
        } else if (!userDeny) {
            PermissionManager.checkPermission(this, RouterConstants.MODULE_NAME_QRCODE_SCAN, true, PermissionsActivityV2::class.java, object : PermissionResultOfAAF(false) {
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

            }, mutableListOf(Manifest.permission.CAMERA))
        }
    }

    override fun initAlbumAction(btnAlbum: CheckedEnableImageView) {
        btnAlbum.setOnClickListener { view: View? ->
            if (BuildUtils.SDK_INT >= Build.VERSION_CODES.Q) {
                getPhotoContent()
            } else {
                PermissionManager.checkPermission(this, RouterConstants.MODULE_NAME_QRCODE_SCAN, false, object : PermissionResultOfAAF(false) {
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
                }, selectPhotoPermission)
            }
        }
    }
}