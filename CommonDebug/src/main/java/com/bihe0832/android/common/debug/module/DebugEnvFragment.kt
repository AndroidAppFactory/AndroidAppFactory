package com.bihe0832.android.common.debug.module

import com.bihe0832.android.common.debug.base.BaseDebugListFragment
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.config.Config
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

    fun getChangeEnvSelectDialog(title: String, data: List<String>, index: Int, ins: OnEnvChangedListener): RadioDialog {
        RadioDialog(activity).apply {
            setTitle("${title}切换")
            setHtmlContent("点击下方列表选择将 <font color='#38ADFF'> ${title} </font> 切换为：")
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

    fun showChangeEnvResult(title: String, key: String, value: String, tipsText: String, actionType: Int) {
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
            var tips = "${title}已切换为：<BR> <font color=\"#c0392b\">$tipsText</font> <BR> 点击确认后" + when (actionType) {
                CHANGE_ENV_EXIST_TYPE_EXIST -> "APP会自动退出，手动启动APP后生效"
                CHANGE_ENV_EXIST_TYPE_RESTART -> "APP会自动重启，APP重启后生效。<font color=\"#EC4C40\">重启过程会偶现白屏，请耐心等待</font>"
                else -> "生效"
            }

            DialogUtils.showConfirmDialog(activity!!, "${title}切换", tips, canCancel = false, object :
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
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getInfoItem(itemContent: String): DebugItemData {
        return DebugItemData(itemContent) { showInfo("应用调试信息", itemContent) }
    }

}