package com.bihe0832.android.app.ui.navigation

import com.bihe0832.android.app.message.AAFMessageManager
import com.bihe0832.android.common.main.CommonActivityWithNavigationDrawer
import com.bihe0832.android.framework.update.UpdateInfoLiveData
import com.bihe0832.android.lib.ui.custom.view.background.TextViewWithBackground
import com.bihe0832.android.lib.ui.custom.view.background.changeStatusWithUnreadMsg
import com.bihe0832.android.lib.utils.os.DisplayUtil

/**
 * 导航栏红点管理扩展
 *
 * 根据更新状态和消息未读数目汇总红点数目
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/3/9.
 */

/**
 * 添加红点更新监听
 *
 * 监听消息和更新状态变化，自动更新红点显示
 *
 * @param unreadView 红点显示 View
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

/**
 * 更新红点显示
 *
 * 根据更新状态和未读消息数计算红点数目
 *
 * @param unreadView 红点显示 View
 */
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


