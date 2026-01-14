package com.bihe0832.android.app.message

import android.app.Activity
import com.bihe0832.android.common.message.base.MessageManager
import com.bihe0832.android.common.message.data.MessageInfoItem
import com.bihe0832.android.common.message.list.BaseMessageFragment

/**
 * AAF 消息列表 Fragment
 *
 * 展示应用内消息列表，继承自 BaseMessageFragment
 * 使用 AAFMessageManager 管理消息数据
 *
 * @author zixie code@bihe0832.com
 * @since 1.0.0
 */
class AAFMessageListFragment : BaseMessageFragment() {

    /**
     * 获取消息管理器
     *
     * @return AAFMessageManager 实例
     */
    override fun getMessageManager(): MessageManager {
        return AAFMessageManager
    }

    /**
     * 显示消息详情
     *
     * @param activity 当前 Activity
     * @param messageInfoItem 消息数据
     * @param showFace 是否以拍脸方式显示
     */
    override fun showMessage(activity: Activity, messageInfoItem: MessageInfoItem, showFace: Boolean) {
        AAFMessageManager.showMessage(activity, messageInfoItem, showFace)
    }
}