package com.bihe0832.android.app.message

import android.app.Activity
import com.bihe0832.android.common.message.base.MessageManager
import com.bihe0832.android.common.message.data.MessageInfoItem
import com.bihe0832.android.common.message.list.BaseMessageFragment


class AAFMessageListFragment : BaseMessageFragment() {

    override fun getMessageManager(): MessageManager {
        return AAFMessageManager
    }

    override fun showMessage(activity: Activity, messageInfoItem: MessageInfoItem, showFace: Boolean) {
        AAFMessageManager.showMessage(activity, messageInfoItem, showFace)
    }
}