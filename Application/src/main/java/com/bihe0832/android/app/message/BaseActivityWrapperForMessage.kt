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
 * 消息功能的 Activity/Fragment 扩展
 *
 * 提供消息图标点击、未读数显示、消息拍脸等功能的扩展方法
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/3/9.
 */

/**
 * BaseActivity 扩展：添加消息图标点击事件并自动显示拍脸消息
 *
 * @param messageView 消息图标 ImageView
 * @param unreadView 未读数显示 View
 * @param autoShow 是否自动显示拍脸消息
 */
fun BaseActivity.addMessageIconActionAndShowFace(messageView: ImageView, unreadView: TextViewWithBackground, autoShow: Boolean) {
    setMessageIconActionAndShowFace(this, messageView, unreadView, autoShow)
}

/**
 * BaseActivity 扩展：检查并显示拍脸消息
 */
fun BaseActivity.checkMsgAndShowFace() {
    checkMsgAndShowFace(this)
}

/**
 * BaseFragment 扩展：添加消息图标点击事件并自动显示拍脸消息
 *
 * @param messageView 消息图标 ImageView
 * @param unreadView 未读数显示 View
 * @param autoShow 是否自动显示拍脸消息
 */
fun BaseFragment.addMessageIconActionAndShowFace(messageView: ImageView, unreadView: TextViewWithBackground, autoShow: Boolean) {
    setMessageIconActionAndShowFace(activity!!, messageView, unreadView, autoShow)
}

/**
 * BaseFragment 扩展：检查并显示拍脸消息
 */
fun BaseFragment.checkMsgAndShowFace() {
    activity?.let {
        checkMsgAndShowFace(it)
    }
}

/**
 * 设置消息图标点击跳转，观察消息回调，自动拍脸并更新未读数
 *
 * 用于存在公告按钮的场景
 *
 * @param activity FragmentActivity 实例
 * @param messageView 消息图标 ImageView
 * @param unreadView 未读数显示 View
 * @param autoShow 是否自动显示拍脸消息
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
 * 观察消息回调，自动拍脸
 *
 * 用于只需要拍脸，不存在按钮的场景
 *
 * @param activity FragmentActivity 实例
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
 *
 * @param noticeList 消息列表
 * @param activity FragmentActivity 实例
 * @param unreadView 未读数显示 View
 * @param autoShow 是否自动显示拍脸消息
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
 *
 * 过滤未展示过的消息，依次显示
 *
 * @param activity FragmentActivity 实例
 * @param noticeList 消息列表
 */
private fun checkMsgAndShowFace(activity: FragmentActivity, noticeList: List<MessageInfoItem>?) {
    noticeList?.distinctBy { it.messageID }?.filter { !AAFMessageManager.mAutoShowMessageList.contains(it.messageID) && AAFMessageManager.canShowFace(it, false) }?.forEach {
        AAFMessageManager.mAutoShowMessageList.add(it.messageID)
        AAFMessageManager.showMessage(activity, it, true)
    }
}
