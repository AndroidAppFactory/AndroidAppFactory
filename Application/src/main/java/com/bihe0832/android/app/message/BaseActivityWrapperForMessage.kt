package com.bihe0832.android.app.message

import android.widget.ImageView
import com.bihe0832.android.app.R
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.common.message.data.MessageInfoItem
import com.bihe0832.android.framework.ui.BaseActivity
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

fun BaseActivity.addMessageAction(messageView: ImageView, unreadView: TextViewWithBackground) {
    AAFMessageManager.getMessageLiveData().value.let { updateMessageMenuAndShowFace(it, this, unreadView) }
    AAFMessageManager.getMessageLiveData().observe(this) { t ->
        updateMessageMenuAndShowFace(t, this, unreadView)
    }

    messageView.apply {
        setColorFilter(resources.getColor(R.color.colorOnPrimary))
        setOnClickListener {
            RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_MESSAGE)
        }
    }
}

@Synchronized
fun updateMessageMenuAndShowFace(noticeList: List<MessageInfoItem>?, activity: BaseActivity, unreadView: TextViewWithBackground) {
    if (noticeList.isNullOrEmpty()) {
        unreadView.changeStatusWithUnreadMsg(-1, DisplayUtil.dip2px(activity, 8f))
    } else {
        noticeList.filter { !it.hasRead() }.let {
            if (it.isNotEmpty()) {
                unreadView.changeStatusWithUnreadMsg(it.size, DisplayUtil.dip2px(activity, 8f))
            } else {
                unreadView.changeStatusWithUnreadMsg(-1, DisplayUtil.dip2px(activity, 8f))
            }
        }

        noticeList.distinctBy { it.messageID }.filter { !mAutoShowMessageList.contains(it.messageID) && AAFMessageManager.canShowFace(it, false) }.forEach {
            mAutoShowMessageList.add(it.messageID)
            AAFMessageManager.showMessage(activity, it, true)
        }
    }

}