package com.bihe0832.android.common.debug.module

import android.view.View
import com.bihe0832.android.common.debug.base.BaseDebugListFragment
import com.bihe0832.android.common.file.preview.ContentItemData
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.permission.AAFPermissionManager
import com.bihe0832.android.common.permission.PermissionResultOfAAF
import com.bihe0832.android.common.permission.special.PermissionsActivityWithSpecial
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.permission.PermissionManager
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.impl.RadioDialog
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.android.lib.ui.toast.ToastUtil

/**
 * @author zixie code@bihe0832.com Created on 7/16/21.
 */
open class DebugEnvFragment : BaseDebugListFragment() {

    interface OnEnvChangedListener {
        fun onChanged(index: Int)
    }

    fun getEnvDebugItem(content: String, action: View.OnClickListener): ContentItemData {
        return getDebugItem(
            content, action, null, isBold = false, isSingleLine = false,null, isTips = false
        )
    }

    fun getChangeEnvSelectDialog(
        title: String,
        data: List<String>,
        index: Int,
        ins: OnEnvChangedListener
    ): RadioDialog {
        RadioDialog(activity).apply {
            setTitle("${title}切换")
            setHtmlContent("点击下方列表选择将 <font color='#38ADFF'> $title </font> 切换为：")
            setRadioData(data, index, null)
            setPositive("确定")
            setNegative("取消")
            setShouldCanceled(true)
            setOnClickBottomListener(object :
                OnDialogListener {
                override fun onPositiveClick() {
                    dismiss()
                    ins.onChanged(checkedIndex)
                }

                override fun onNegativeClick() {
                    try {
                        dismiss()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onCancel() {
                    dismiss()
                }
            })
        }.let {
            return it
        }
    }

    fun showChangeEnvResult(title: String, key: String, value: String, actionType: Int) {
        showChangeEnvResult(title, key, value, value, actionType)
    }

    fun showChangeEnvResult(
        title: String,
        key: String,
        value: String,
        tipsText: String,
        actionType: Int
    ) {
        try {
            var setResultForServer = Config.writeConfig(key, value)
            if (setResultForServer) {
                showChangeEnvDialog(title, tipsText, actionType)
            } else {
                ToastUtil.showShort(context, "${title}切换失败，请重试")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val CHANGE_ENV_EXIST_TYPE_NOTHING = 0
    val CHANGE_ENV_EXIST_TYPE_EXIST = 1
    val CHANGE_ENV_EXIST_TYPE_RESTART = 2

    fun showChangeEnvDialog(title: String, tipsText: String, actionType: Int) {
        try {
            var tips =
                "${title}已切换为：<BR> <font color=\"#c0392b\">$tipsText</font> <BR> 点击确认后" + when (actionType) {
                    CHANGE_ENV_EXIST_TYPE_EXIST -> "APP会自动退出，手动启动APP后生效"
                    CHANGE_ENV_EXIST_TYPE_RESTART -> "APP会自动重启，APP重启后生效。<font color=\"#EC4C40\">重启过程会偶现白屏，请耐心等待</font>"
                    else -> "生效"
                }

            DialogUtils.showConfirmDialog(
                activity!!,
                "${title}切换",
                tips,
                canCancel = false,
                object :
                    OnDialogListener {
                    fun clickAction(actionType: Int) {
                        when (actionType) {
                            CHANGE_ENV_EXIST_TYPE_EXIST -> ZixieContext.exitAPP()
                            CHANGE_ENV_EXIST_TYPE_RESTART -> ZixieContext.restartApp(0L)
                        }
                    }

                    override fun onPositiveClick() {
                        clickAction(actionType)
                    }

                    override fun onNegativeClick() {
                        clickAction(actionType)
                    }

                    override fun onCancel() {
                        clickAction(actionType)
                    }
                },
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getInfoItem(itemContent: String): ContentItemData {
        return getDebugItem(itemContent) { showInfo("应用调试信息", itemContent) }
    }

    fun requestPermissionForDebug(
        permissonList: List<String>,
        permissionResult: PermissionManager.OnPermissionResult? = null,
    ) {
        if (permissonList.isNullOrEmpty()) {
            return
        }
        val scene = "AAFDebug"
        val groupID = permissonList.first()

        PermissionManager.addPermissionGroup(scene, groupID, permissonList)
        PermissionManager.addPermissionGroupDesc(scene, groupID, "调试权限")
        PermissionManager.addPermissionGroupContent(
            scene,
            groupID,
            "这是一个用于调试过程中的临时权限申请，请同意授权方便开发调试",
        )

        AAFPermissionManager.checkSpecialPermission(
            activity!!,
            scene,
            true,
            mutableListOf(groupID),
            PermissionsActivityWithSpecial::class.java,
            object : PermissionResultOfAAF(false) {
                override fun onSuccess() {
                    super.onSuccess()
                    ZixieContext.showToast("授权成功")
                    permissionResult?.onSuccess()
                }

                override fun onUserCancel(
                    scene: String,
                    permissionGroupID: String,
                    permission: String
                ) {
                    super.onUserCancel(scene, permissionGroupID, permission)
                    ZLog.d("放弃授权")
                    permissionResult?.onUserCancel(scene, permissionGroupID, permission)
                }

                override fun onUserDeny(
                    scene: String,
                    permissionGroupID: String,
                    permission: String
                ) {
                    super.onUserDeny(scene, permissionGroupID, permission)
                    ZLog.d("拒绝授权")
                    permissionResult?.onUserDeny(scene, permissionGroupID, permission)
                }

                override fun onFailed(msg: String) {
                    super.onFailed(msg)
                    ZLog.d("授权失败")
                    permissionResult?.onFailed(msg)
                }
            },
        )
    }
}
