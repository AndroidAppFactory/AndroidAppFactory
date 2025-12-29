package com.bihe0832.android.base.debug.permission

import android.Manifest
import android.app.Activity
import android.view.View
import com.bihe0832.android.base.compose.debug.R as DebugComposeR
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.permission.AAFPermissionManager
import com.bihe0832.android.common.permission.PermissionResultOfAAF
import com.bihe0832.android.common.permission.special.PermissionsActivityWithSpecial
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.permission.ui.PermissionDialog
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener

class DebugPermissionFragment : DebugEnvFragment() {

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugItem("自定义内容权限弹框(多语言)", View.OnClickListener { testCustomPermission(activity) }))
            add(getDebugItem("通用权限弹框", View.OnClickListener { testCommonPermission(activity) }))
            add(getDebugItem("通用权限弹框V2", View.OnClickListener { testCommonPermissionV2(activity) }))
            add(getDebugItem("权限拒绝通用弹框", View.OnClickListener { testCommonPermissionDialog() }))
            add(getDebugItem("申请通用权限（相机）", View.OnClickListener { checkCommonPermision() }))
            add(getDebugItem("申请特殊权限（位置）", View.OnClickListener { checkSpecialPermision() }))
            add(
                getDebugItem(
                    "调试中临时申请指定权限",
                    View.OnClickListener {
                        requestPermissionForDebug(
                            listOf(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                            ),
                        )
                    },
                ),
            )

            add(
                getDebugItem(
                    "查看当前自定义的权限信息",
                    View.OnClickListener { PermissionManager.logPermissionConfigInfo() },
                ),
            )
        }
    }

    private fun checkCommonPermision() {
        PermissionManager.checkPermission(context!!, Manifest.permission.CAMERA)
    }

    private fun checkSpecialPermision() {
        AAFPermissionManager.checkSpecialPermission(
            activity!!,
            "",
            true,
            mutableListOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            PermissionsActivityWithSpecial::class.java,
            PermissionResultOfAAF(false),
        )
    }

    private fun testCustomPermission(activity: Activity?) {
        PermissionManager.addPermissionGroupContent(
            HashMap<String, String>().apply {
                put(
                    Manifest.permission.CAMERA,
                    activity?.getString(DebugComposeR.string.name_permission_debug)?:"",
                )
            },
        )
        activity?.let { it ->
            DebugPermissionDialog(it).let { permissionDialog ->
                permissionDialog.show(
                    "",
                    Manifest.permission.CAMERA,
                    true,
                    object : OnDialogListener {
                        override fun onPositiveClick() {
//                    openFloatPermissionSettings(context)
                            permissionDialog.dismiss()
                        }

                        override fun onNegativeClick() {
                            permissionDialog.dismiss()
                        }

                        override fun onCancel() {
                            permissionDialog.dismiss()
                        }
                    },
                )
            }
        }
    }

    private fun testCommonPermission(activity: Activity?) {
        activity?.let { it ->
            PermissionDialog(it).let { permissionDialog ->
                permissionDialog.show(
                    "",
                    Manifest.permission.CAMERA,
                    true,
                    object : OnDialogListener {
                        override fun onPositiveClick() {
//                    openFloatPermissionSettings(context)
                            permissionDialog.dismiss()
                        }

                        override fun onNegativeClick() {
                            permissionDialog.dismiss()
                        }

                        override fun onCancel() {
                            permissionDialog.dismiss()
                        }
                    },
                )
            }
        }
    }

    private fun testCommonPermissionV2(activity: Activity?) {
        PermissionManager.checkPermission(
            activity,
            "",
            false,
            DebugPermissionsActivity::class.java,
            null,
            mutableListOf<String>().apply {
                add(Manifest.permission.ACCESS_COARSE_LOCATION)
            },
        )
    }

    private fun testCommonPermissionDialog() {
        PermissionDialog(activity!!).let {
            it.show(
                "",
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                true,
                object :
                    OnDialogListener {
                    override fun onPositiveClick() {
                        it.dismiss()
                    }

                    override fun onNegativeClick() {
                        it.dismiss()
                    }

                    override fun onCancel() {
                        it.dismiss()
                    }
                },
            )
        }
    }
}
