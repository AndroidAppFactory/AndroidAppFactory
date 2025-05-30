package com.bihe0832.android.common.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.location.LocationManager
import android.provider.Settings
import androidx.core.location.LocationManagerCompat
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.notification.NotifyManager
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.permission.ui.PermissionsActivity
import com.bihe0832.android.lib.permission.wrapper.openFloatSettings

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/4/26.
 * Description: Description
 *
 */
object AAFPermissionManager {

    val selectPhotoPermission =
        mutableListOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    val takePhotoPermission = mutableListOf(Manifest.permission.CAMERA)
    val locationPermission =
        mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    fun initPermission(context: Context) {
        PermissionManager.addPermissionGroup(
            "",
            Manifest.permission.ACCESS_COARSE_LOCATION,
            locationPermission
        )
        PermissionManager.addPermissionGroupDesc(
            "",
            Manifest.permission.ACCESS_COARSE_LOCATION,
            context.getString(R.string.common_permission_title_location)
        )
        PermissionManager.addPermissionGroupScene(
            "",
            Manifest.permission.ACCESS_COARSE_LOCATION,
            context.getString(R.string.common_permission_scene_location),
        )

        PermissionManager.addPermissionGroup("", Manifest.permission.CAMERA, takePhotoPermission)
        PermissionManager.addPermissionGroupDesc(
            "",
            Manifest.permission.CAMERA,
            context.getString(R.string.common_permission_title_camera)
        )
        PermissionManager.addPermissionGroupScene(
            "",
            Manifest.permission.CAMERA,
            context.getString(R.string.common_permission_title_qrcode)
        )
    }

    fun hasFloatPermission(): Boolean {
        return PermissionManager.isAllPermissionOK(
            ZixieContext.applicationContext!!,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
        )
    }

    fun openFloatPermission() {
        return openFloatSettings(ZixieContext.applicationContext!!)
    }

    fun hasNotifyPermission(): Boolean {
        return NotifyManager.areNotificationsEnabled(ZixieContext.applicationContext!!)
    }

    fun openNotifyPermission() {
        NotifyManager.showNotificationsSettings(ZixieContext.applicationContext!!)
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    fun permissionExtraCheckIsOK(context: Context, permission: String): Boolean {
        if (permission == Manifest.permission.ACCESS_COARSE_LOCATION) {
            if (!isLocationEnabled(context)) {
                return false
            }
        }
        return true
    }

    fun checkSpecialPermission(
        activity: Activity,
        scene: String,
        canCancel: Boolean,
        tempPermissionList: List<String>,
        permissionsActivityClass: Class<out PermissionsActivity>,
        result: PermissionManager.OnPermissionResult?,
    ) {
        PermissionManager.checkPermission(
            activity,
            scene,
            canCancel,
            permissionsActivityClass,
            object : PermissionManager.OnPermissionResult {
                override fun onFailed(msg: String) {
                    result?.onFailed(msg)
                }

                override fun onSuccess() {
                    var isOK = true
                    for (permission in tempPermissionList) {
                        if (!permissionExtraCheckIsOK(activity, permission)) {
                            isOK = false
                            try {
                                PermissionManager.startPermissionActivity(
                                    activity,
                                    scene,
                                    canCancel,
                                    permissionsActivityClass,
                                    tempPermissionList,
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                                result?.onFailed("start permission activity failed")
                            }
                            break
                        }
                    }

                    if (isOK) {
                        result?.onSuccess()
                    }
                }

                override fun onUserCancel(
                    scene: String,
                    permissionGroupID: String,
                    permission: String
                ) {
                    result?.onUserCancel(scene, permissionGroupID, permission)
                }

                override fun onUserDeny(
                    scene: String,
                    permissionGroupID: String,
                    permission: String
                ) {
                    result?.onUserDeny(scene, permissionGroupID, permission)
                }
            },
            tempPermissionList,
        )
    }
}
