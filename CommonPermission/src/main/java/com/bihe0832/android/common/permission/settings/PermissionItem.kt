package com.bihe0832.android.common.permission.settings

import android.app.Activity
import android.view.View
import android.widget.CompoundButton
import com.bihe0832.android.common.about.R
import com.bihe0832.android.common.permission.PermissionResultOfAAF
import com.bihe0832.android.common.settings.card.SettingsData
import com.bihe0832.android.common.settings.card.SettingsDataV2
import com.bihe0832.android.framework.ui.main.CommonRootActivity
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.permission.ui.PermissionsActivityV2
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.utils.intent.IntentUtils

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/4/26.
 * Description: Description
 *
 */
object PermissionItem {

    fun getPermission(cls: Class<out PermissionFragment>): SettingsData {
        val title = "隐私及权限设置"
        return SettingsData(title).apply {
            mItemIconRes = R.drawable.icon_privacy_tip
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                CommonRootActivity.startCommonRootActivity(it.context, cls, title)
            }
        }
    }

    fun getRecommandSetting(): SettingsDataV2 {
        return SettingsDataV2().apply {
            title = "个性化内容推荐"
            description = "开启后，将根据您的喜好为您推荐个性化内容"
            isChecked = false
            onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->

            }
        }
    }


    fun getPermissionSetting(activity: Activity, permissionGroup: String): SettingsDataV2 {
        return getPermissionSetting(activity, "", permissionGroup)
    }

    fun getPermissionSetting(activity: Activity, scene: String, permissionGroup: String): SettingsDataV2 {
        PermissionManager.logPermissionConfigInfo()
        val permissionDesc: String = PermissionManager.getPermissionDesc(scene, permissionGroup, useDefault = false, needSpecial = false) + "权限"
        val permissionScene: String = PermissionManager.getPermissionScene(scene, permissionGroup, useDefault = false, needSpecial = false)
        return getPermissionSetting(activity, scene, permissionDesc, permissionScene, permissionGroup)
    }

    fun getPermissionSetting(activity: Activity, scene: String, permissionDesc: String, permissionScene: String, permissionGroup: String): SettingsDataV2 {
        return SettingsDataV2().apply {
            title = permissionDesc
            description = permissionScene
            val hasPermission: Boolean = PermissionManager.isAllPermissionOK(activity, permissionGroup)
            tips = if (hasPermission) {
                TextFactoryUtils.getSpecialText("已开启", ThemeResourcesManager.getColor(R.color.textColorSecondary)!!)
            } else {
                TextFactoryUtils.getSpecialText("去设置", ThemeResourcesManager.getColor(R.color.textColorPrimary)!!)
            }
            onClickListener = View.OnClickListener {
                if (hasPermission) {
                    showPermissionCloseDialog(activity, "关闭$permissionDesc", "关闭${permissionDesc}后将不能$description", permissionGroup)
                } else {
                    PermissionManager.checkPermission(activity, scene, true, PermissionsActivityV2::class.java, PermissionResultOfAAF(false), mutableListOf<String>().apply {
                        add(permissionGroup)
                    })
                }
            }
        }
    }

    private fun showPermissionCloseDialog(activity: Activity, titleString: String, permissionCloseDesc: String, permission: String) {
        CommonDialog(activity).apply {
            title = titleString
            setHtmlContent(permissionCloseDesc)
            positive = "去设置"
            negative = "取消"
            shouldCanceled = false
            onClickBottomListener = object : OnDialogListener {
                override fun onPositiveClick() {
                    dismiss()
                    IntentUtils.startAppSettings(activity, permission)
                }

                override fun onNegativeClick() {
                    dismiss()
                }

                override fun onCancel() {
                    dismiss()
                }

            }
        }.let {
            it.show()
        }
    }
}