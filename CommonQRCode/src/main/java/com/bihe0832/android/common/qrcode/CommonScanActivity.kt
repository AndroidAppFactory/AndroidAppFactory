package com.bihe0832.android.common.qrcode

import android.Manifest
import android.os.Build
import android.view.View
import com.bihe0832.android.common.photos.getPhotoContent
import com.bihe0832.android.common.qrcode.core.BaseScanActivity
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.media.image.CheckedEnableImageView
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.permission.ui.PermissionsActivityV2
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.utils.os.BuildUtils
import java.util.Locale

@Module(RouterConstants.MODULE_NAME_QRCODE_SCAN)
open class CommonScanActivity : BaseScanActivity() {

    private var userDeny = false

    fun initPermission() {
        PermissionManager.addPermissionGroup(
            RouterConstants.MODULE_NAME_QRCODE_SCAN,
            Manifest.permission.CAMERA,
            com.bihe0832.android.common.permission.AAFPermissionManager.takePhotoPermission,
        )
        PermissionManager.addPermissionGroupDesc(
            RouterConstants.MODULE_NAME_QRCODE_SCAN,
            Manifest.permission.CAMERA,
            getString(R.string.common_permission_title_camera),
        )
        PermissionManager.addPermissionGroupScene(
            RouterConstants.MODULE_NAME_QRCODE_SCAN,
            Manifest.permission.CAMERA,
            getString(R.string.common_permission_title_qrcode)
        )
    }

    override fun onLocaleChanged(lastLocale: Locale, toLanguageTag: Locale) {
        initPermission()
    }

    override fun initUI() {
        super.initUI()
        initPermission()
    }


    fun startScanAction() {
        super.startCamera()
    }

    override fun startCamera() {
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
                    com.bihe0832.android.common.permission.AAFPermissionManager.selectPhotoPermission,
                )
            }
        }
    }
}
