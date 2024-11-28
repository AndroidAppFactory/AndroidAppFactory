package com.bihe0832.android.common.debug.item

import android.view.View
import com.bihe0832.android.common.debug.R
import com.bihe0832.android.common.debug.base.BaseDebugListFragment
import com.bihe0832.android.framework.ZixieContext.applicationContext
import com.bihe0832.android.framework.router.RouterAction


/**
 * @author zixie code@bihe0832.com Created on 2019-11-21. Description: Description
 */

fun getTextColor(isTips: Boolean): Int {
    return if (isTips) {
        applicationContext!!.resources.getColor(R.color.colorOnPrimary)
    } else {
        applicationContext!!.resources.getColor(R.color.colorOnBackground)
    }
}

fun getBackGroundColor(isTips: Boolean): Int {
    return if (isTips) {
        applicationContext!!.resources.getColor(R.color.colorAccent)
    } else {
        applicationContext!!.resources.getColor(R.color.windowBackground)
    }
}

fun getItem(
    content: String,
    listener: View.OnClickListener?,
    longClickListener: View.OnLongClickListener?,
    isBold: Boolean,
    isSingleLine: Boolean,
    isTips: Boolean
): DebugItemData {
    return DebugItemData(
        content,
        listener,
        longClickListener,
        DebugItemData.DEFAULT_TEXT_SIZE_DP,
        getTextColor(isTips),
        isBold,
        isSingleLine,
        DebugItemData.DEFAULT_PADDING_SIZE_DP,
        getBackGroundColor(isTips),
        true
    )
}

private fun getItem(content: String, isTips: Boolean): DebugItemData {
    return getItem(content, null, null, isTips, !isTips, isTips)
}

fun getTipsItem(content: String): DebugItemData {
    return getItem(content, true)
}

fun getDebugItem(content: String): DebugItemData {
    return getItem(content, false)
}

private fun getItem(
    content: String, listener: View.OnClickListener?, isTips: Boolean
): DebugItemData {
    return getItem(content, listener, null, isTips, !isTips, isTips)
}

fun getTipsItem(content: String, listener: View.OnClickListener?): DebugItemData {
    return getItem(content, listener, true)
}

fun getDebugItem(content: String, listener: View.OnClickListener?): DebugItemData {
    return getItem(content, listener, false)
}


private fun getInfoItem(
    content: String,
    listener: View.OnClickListener?,
    longClickListener: View.OnLongClickListener?,
    isTips: Boolean
): DebugItemData {
    return getItem(content, listener, longClickListener, isTips, !isTips, isTips)
}

fun BaseDebugListFragment.getRouterItem(content: String): DebugItemData {
    return getInfoItem(content, { RouterAction.openFinalURL(content) }, {
        showInfo("复制并分享路由地址", content)
        true
    }, false)
}