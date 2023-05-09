package com.bihe0832.android.app.ui.navigation

import com.bihe0832.android.app.message.AAFMessageManager
import com.bihe0832.android.common.main.CommonActivityWithNavigationDrawer
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.framework.update.UpdateInfoLiveData
import com.bihe0832.android.lib.ui.custom.view.background.TextViewWithBackground
import com.bihe0832.android.lib.ui.custom.view.background.changeStatusWithUnreadMsg
import com.bihe0832.android.lib.utils.os.DisplayUtil

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2023/3/9.
 * Description: Description
 *
 */

private val mAutoShowMessageList = mutableListOf<String>()

private fun updateRedDot(unreadView: TextViewWithBackground) {
    val hasNewUpdate = UpdateInfoLiveData.value?.canShowNew() ?: false
    val msgNum = AAFMessageManager.getUnreadNum()
    if (msgNum > 0) {
        if (hasNewUpdate) {
            msgNum + 1
        } else {
            msgNum
        }
    } else {
        if (hasNewUpdate) {
            0
        } else {
            -1
        }
    }.let {
        unreadView.changeStatusWithUnreadMsg(it, DisplayUtil.dip2px(unreadView.context, 8f))
    }
}

fun CommonActivityWithNavigationDrawer.addRedDotAction(unreadView: TextViewWithBackground) {
    updateRedDot(unreadView)
    AAFMessageManager.getMessageLiveData().observe(this) { t ->
        updateRedDot(unreadView)
    }
    UpdateInfoLiveData.observe(this) { t ->
        updateRedDot(unreadView)
    }
}

@Synchronized
fun checkMsgAndShowFace(activity: BaseActivity) {
    AAFMessageManager.getMessageLiveData().value?.let { noticeList ->
        noticeList.distinctBy { it.messageID }.filter { !mAutoShowMessageList.contains(it.messageID) && AAFMessageManager.canShowFace(it, false) }.forEach {
            mAutoShowMessageList.add(it.messageID)
            AAFMessageManager.showMessage(activity, it, true)
        }
    }

}