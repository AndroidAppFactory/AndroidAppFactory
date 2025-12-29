package com.bihe0832.android.common.permission.settings

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.CompoundButton
import com.bihe0832.android.common.permission.PermissionResultOfAAF
import com.bihe0832.android.common.settings.card.SettingsDataGo
import com.bihe0832.android.common.settings.card.SettingsDataSwitch
import com.bihe0832.android.framework.ui.main.CommonRootActivity
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.permission.ui.PermissionsActivityV2
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.utils.intent.IntentUtils
import com.bihe0832.android.model.res.R as ModelResR
import com.bihe0832.android.lib.aaf.res.R as ResR

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/4/26.
 * Description: Description
 *
 */
object PermissionItem {

    fun getPermission(context: Context, cls: Class<out PermissionFragment>): SettingsDataGo {
        val title = context.getString(ModelResR.string.common_permission_item_title_privacy)
        return SettingsDataGo(title).apply {
            mItemIconRes = ResR.drawable.icon_privacy_tip
            mShowDriver = true
            mShowGo = true
            mHeaderListener = View.OnClickListener {
                CommonRootActivity.startCommonRootActivity(it.context, cls, title)
            }
        }
    }

    fun getRecommandSetting(context: Context): SettingsDataSwitch {
        return SettingsDataSwitch().apply {
            title = context.getString(ModelResR.string.common_permission_item_title_recommend)
            description = context.getString(ModelResR.string.common_permission_item_desc_recommend)
            isChecked = false
            onCheckedChangeListener =
                CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                }
        }
    }

    fun getPermissionSetting(activity: Activity, permissionGroup: String): SettingsDataSwitch {
        return getPermissionSetting(activity, "", permissionGroup)
    }

    fun getPermissionSetting(
        activity: Activity,
        scene: String,
        permissionGroup: String
    ): SettingsDataSwitch {
        PermissionManager.logPermissionConfigInfo()
        val permissionDesc: String = PermissionManager.getPermissionDesc(
            scene,
            permissionGroup,
            useDefault = false,
            needSpecial = false,
        ) + activity.getString(ModelResR.string.common_permission_desc_ext)
        val permissionScene: String =
            PermissionManager.getPermissionScene(
                scene,
                permissionGroup,
                useDefault = false,
                needSpecial = false
            )
        return getPermissionSetting(
            activity,
            scene,
            permissionDesc,
            permissionScene,
            permissionGroup
        )
    }

    fun getPermissionSetting(
        activity: Activity,
        scene: String,
        permissionDesc: String,
        permissionScene: String,
        permissionGroup: String,
    ): SettingsDataSwitch {
        return SettingsDataSwitch().apply {
            title = permissionDesc
            description = permissionScene
            val hasPermission: Boolean =
                PermissionManager.isAllPermissionOK(activity, permissionGroup)
            tips = if (hasPermission) {
                TextFactoryUtils.getSpecialText(
                    activity.getString(ModelResR.string.common_permission_status_enabled),
                    ThemeResourcesManager.getColor(ResR.color.textColorSecondary)!!
                )
            } else {
                TextFactoryUtils.getSpecialText(
                    activity.getString(ModelResR.string.common_permission_action_setting),
                    ThemeResourcesManager.getColor(ResR.color.textColorPrimary)!!
                )
            }
            onClickListener = View.OnClickListener {
                if (hasPermission) {
                    showPermissionCloseDialog(
                        activity,
                        activity.getString(ModelResR.string.common_permission_action_close) + " " + permissionDesc,
                        activity.getString(ModelResR.string.common_permission_action_close) + " " + permissionDesc + " " +
                                activity.getString(ModelResR.string.common_permission_desc_close) + " $description",
                        permissionGroup,
                    )
                } else {
                    PermissionManager.checkPermission(
                        activity,
                        scene,
                        true,
                        PermissionsActivityV2::class.java,
                        PermissionResultOfAAF(false),
                        mutableListOf<String>().apply {
                            add(permissionGroup)
                        },
                    )
                }
            }
        }
    }

    private fun showPermissionCloseDialog(
        activity: Activity,
        titleString: String,
        permissionCloseDesc: String,
        permission: String,
    ) {
        CommonDialog(activity).apply {
            title = titleString
            setHtmlContent(permissionCloseDesc)
            positive = activity.getString(ModelResR.string.common_permission_action_setting)
            negative = activity.getString(ModelResR.string.common_permission_action_cancel)
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
        }.show()
    }
}
