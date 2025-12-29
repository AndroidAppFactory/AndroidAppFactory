package com.bihe0832.android.lib.lock.screen.permission

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.bihe0832.android.lib.lock.screen.R
import com.bihe0832.android.lib.aaf.res.R as ResR
import com.bihe0832.android.lib.lock.screen.service.LockScreenService
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.permission.ui.PermissionDialog
import com.bihe0832.android.lib.permission.wrapper.openFloatSettings
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.utils.os.BuildUtils

object LockScreenPermission {

    val mLockScreenPermission = mutableListOf(
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.DISABLE_KEYGUARD,
        Manifest.permission.SYSTEM_ALERT_WINDOW
    )

    const val SCENE = "lock"

    fun init(context: Context) {
        PermissionManager.addPermissionGroup(
            SCENE,
            Manifest.permission.WAKE_LOCK,
            mLockScreenPermission
        )
        PermissionManager.addPermissionGroupDesc(
            SCENE,
            Manifest.permission.WAKE_LOCK,
            context.getString(ResR.string.com_bihe0832_lock_screen_permission_desc_lock)
        )
        PermissionManager.addPermissionGroupScene(
            SCENE,
            Manifest.permission.WAKE_LOCK,
            context.getString(ResR.string.com_bihe0832_lock_screen_permission_scene_lock)
        )
    }

    fun startLockService(context: Context, cls: Class<out LockScreenService?>) {
        ZLog.e(LockScreenService.TAG, "startLockService ${cls.name}")
        init(context)
        try {
            val intent = Intent()
            intent.setComponent(ComponentName(context.packageName, cls.name))
            if (BuildUtils.SDK_INT >= Build.VERSION_CODES.O) {
                context!!.startForegroundService(intent)
            } else {
                context!!.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun startLockServiceWithPermission(context: Context, cls: Class<out LockScreenService?>) {
        if (PermissionManager.isAllPermissionOK(context, mLockScreenPermission)) {
            startLockService(context, cls)
        } else {
            PermissionDialog(context).apply {
                negative = context.getString(ResR.string.com_bihe0832_lock_screen_permission_force)
                positive = context.getString(ResR.string.com_bihe0832_lock_screen_permission_enabled)
                needSpecial = true
            }.let {
                it.show(
                    SCENE,
                    mLockScreenPermission,
                    true,
                    object :
                        OnDialogListener {
                        override fun onPositiveClick() {
                            startLockService(context, cls)
                            openFloatSettings(context)
                            it.dismiss()
                        }

                        override fun onNegativeClick() {
                            startLockService(context, cls)
                            it.dismiss()
                        }

                        override fun onCancel() {
                            startLockService(context, cls)
                            it.dismiss()
                        }
                    },
                )
            }
        }
    }
}
