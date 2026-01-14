package com.bihe0832.android.app.dialog

import android.content.Context
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils
import com.bihe0832.android.lib.ui.dialog.tools.UniqueDialogManager

/**
 * AAF 唯一对话框管理器
 *
 * 提供全局唯一对话框的管理能力，确保同一时间只显示一个对话框
 * 避免多个对话框重叠显示的问题
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/8/9.
 */
object AAFUniqueDialogManager {

    /**
     * 提示类型的唯一对话框管理器
     *
     * 用于管理确认类型的对话框，确保同时只显示一个
     */
    val tipsUniqueDialogManager by lazy {
        UniqueDialogManager().apply {
            setShowDialogInterface(object : UniqueDialogManager.ShowDialogInterface {
                /**
                 * 显示确认对话框
                 *
                 * @param context 上下文
                 * @param title 标题
                 * @param content 内容
                 * @param positiveText 确认按钮文本
                 * @param negativeText 取消按钮文本
                 * @param canCancel 是否可取消
                 * @param listener 对话框回调监听
                 * @return 是否成功显示
                 */
                override fun showDialog(context: Context, title: String, content: String, positiveText: String, negativeText: String, canCancel: Boolean, listener: OnDialogListener): Boolean {
                    DialogUtils.showConfirmDialog(
                            context,
                            title,
                            content,
                            positiveText,
                            negativeText,
                            canCancel,
                            listener

                    )
                    return true
                }
            })
        }
    }
}