package com.bihe0832.android.app.message

import android.app.Activity
import com.bihe0832.android.app.R
import com.bihe0832.android.app.api.AAFNetWorkApi
import com.bihe0832.android.common.message.base.MessageManager
import com.bihe0832.android.common.message.data.MessageInfoItem
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener
import com.bihe0832.android.lib.ui.dialog.blockdialog.DependenceBlockDialogManager

/**
 * AAF 消息管理器
 *
 * 负责应用内消息的获取、展示和管理，包括：
 * - 从服务器获取最新消息
 * - 管理消息的已读状态
 * - 控制消息的拍脸展示（自动弹出）
 *
 * @author zixie code@bihe0832.com
 * Created on 2019-10-21.
 */
object AAFMessageManager : MessageManager() {

    /** 已通过拍脸展示的消息 ID 列表，避免重复展示 */
    val mAutoShowMessageList = mutableListOf<String>()

    /** 依赖阻塞对话框管理器，确保消息按顺序展示 */
    private val mDependenceBlockDialogManager by lazy {
        DependenceBlockDialogManager(true)
    }

    /**
     * 从服务器获取最新消息
     *
     * 通过配置的消息 URL 获取消息列表
     */
    override fun fetchNewMsg() {
        fetchMessageByURLList(AAFNetWorkApi.getCommonURL(ThemeResourcesManager.getString(R.string.message_url) ?: "", ""))
    }

    /**
     * 显示消息
     *
     * 使用依赖阻塞管理器确保消息按顺序展示，前一个消息关闭后才显示下一个
     *
     * @param activity 当前 Activity
     * @param messageInfoItem 消息数据
     * @param showFace 是否以拍脸方式显示
     */
    fun showMessage(activity: Activity, messageInfoItem: MessageInfoItem, showFace: Boolean) {
        mDependenceBlockDialogManager.getDependentTaskManager().addTask(messageInfoItem.messageID, {
            ThreadManager.getInstance().runOnUIThread {
                showMessage(activity, messageInfoItem, showFace, object :
                    OnDialogListener {
                    override fun onPositiveClick() {
                        mDependenceBlockDialogManager.getDependentTaskManager().finishTask(messageInfoItem.messageID)
                    }

                    override fun onNegativeClick() {
                        onPositiveClick()
                    }

                    override fun onCancel() {
                        onPositiveClick()
                    }
                })
            }
        }, mutableListOf())
    }
}