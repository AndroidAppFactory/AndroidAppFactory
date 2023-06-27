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
 * @author zixie code@bihe0832.com
 * Created on 2023/3/9.
 * Description: 根据更新和消息未读数目汇总红点数目
 *
 */

fun CommonActivityWithNavigationDrawer.addRedDotAction(unreadView: TextViewWithBackground) {
    updateRedDot(unreadView)
    AAFMessageManager.getMessageLiveData().observe(this) { t ->
        updateRedDot(unreadView)
    }
    UpdateInfoLiveData.observe(this) { t ->
        updateRedDot(unreadView)
    }
}

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


