package com.bihe0832.android.app.message

import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.lib.aaf.res.R as ResR
import com.bihe0832.android.common.message.data.MessageInfoItem
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.ui.custom.view.background.TextViewWithBackground
import com.bihe0832.android.lib.ui.custom.view.background.changeStatusWithUnreadMsg
import com.bihe0832.android.lib.utils.os.DisplayUtil

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/3/9.
 * Description: Description
 *
 */

fun BaseActivity.addMessageIconActionAndShowFace(messageView: ImageView, unreadView: TextViewWithBackground, autoShow: Boolean) {
    setMessageIconActionAndShowFace(this, messageView, unreadView, autoShow)
}

fun BaseActivity.checkMsgAndShowFace() {
    checkMsgAndShowFace(this)
}

fun BaseFragment.addMessageIconActionAndShowFace(messageView: ImageView, unreadView: TextViewWithBackground, autoShow: Boolean) {
    setMessageIconActionAndShowFace(activity!!, messageView, unreadView, autoShow)
}

fun BaseFragment.checkMsgAndShowFace() {
    activity?.let {
        checkMsgAndShowFace(it)
    }
}

/**
 * 设置点击跳转，观察回调，自动拍脸并更新未读数
 *
 * 用于存在公告按钮的场景
 */
@Synchronized
private fun setMessageIconActionAndShowFace(activity: FragmentActivity, messageView: ImageView, unreadView: TextViewWithBackground, autoShow: Boolean) {
    messageView.apply {
        setColorFilter(resources.getColor(ResR.color.colorOnPrimary))
        setOnClickListener {
            RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_MESSAGE)
        }
    }

    AAFMessageManager.getMessageLiveData().value.let { updateMessageMenuAndShowFace(it, activity, unreadView, autoShow) }
    AAFMessageManager.getMessageLiveData().observe(activity) { noticeList ->
        updateMessageMenuAndShowFace(noticeList, activity, unreadView, autoShow)
    }
}

/**
 * 观察回调，自动拍脸
 *
 * 用于只需要拍脸，不存在按钮的场景
 */
@Synchronized
private fun checkMsgAndShowFace(activity: FragmentActivity) {
    AAFMessageManager.getMessageLiveData().value?.let { noticeList ->
        checkMsgAndShowFace(activity, noticeList)
    }
    AAFMessageManager.getMessageLiveData().observe(activity) { noticeList ->
        checkMsgAndShowFace(activity, noticeList)
    }
}


/**
 * 更新未读数目，并检查是否有拍脸待弹出
 */
@Synchronized
fun updateMessageMenuAndShowFace(noticeList: List<MessageInfoItem>?, activity: FragmentActivity, unreadView: TextViewWithBackground, autoShow: Boolean) {
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
        if (autoShow) {
            checkMsgAndShowFace(activity, noticeList)
        }
    }
}

/**
 * 仅检查有没有拍脸立即弹出
 */
private fun checkMsgAndShowFace(activity: FragmentActivity, noticeList: List<MessageInfoItem>?) {
    noticeList?.distinctBy { it.messageID }?.filter { !AAFMessageManager.mAutoShowMessageList.contains(it.messageID) && AAFMessageManager.canShowFace(it, false) }?.forEach {
        AAFMessageManager.mAutoShowMessageList.add(it.messageID)
        AAFMessageManager.showMessage(activity, it, true)
    }
}
