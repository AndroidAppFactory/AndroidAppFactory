package com.bihe0832.android.common.debug.item

import android.text.TextUtils
import android.view.View
import com.bihe0832.android.common.debug.R
import com.bihe0832.android.common.debug.base.BaseDebugListFragment
import com.bihe0832.android.framework.ZixieContext.applicationContext
import com.bihe0832.android.framework.router.RouterAction


/**
 * @author zixie code@bihe0832.com Created on 2019-11-21. Description: Description
 */

fun getDebugItemTextColor(isTips: Boolean): Int {
    return if (isTips) {
        applicationContext!!.resources.getColor(R.color.colorOnPrimary)
    } else {
        applicationContext!!.resources.getColor(R.color.colorOnBackground)
    }
}

fun getDebugItemBackGroundColor(isTips: Boolean): Int {
    return if (isTips) {
        applicationContext!!.resources.getColor(R.color.colorAccent)
    } else {
        applicationContext!!.resources.getColor(R.color.windowBackground)
    }
}

fun getDebugItem(
    content: String,
    listener: View.OnClickListener?,
    longClickListener: View.OnLongClickListener?,
    textSizeDP: Int,
    isBold: Boolean,
    isSingleLine: Boolean,
    ellipsize: TextUtils.TruncateAt?,
    paddingTopDp: Int,
    paddingLeftDp: Int,
    isTips: Boolean,
    showBottomLine: Boolean
): DebugItemData {
    return DebugItemData(
        content,
        listener,
        longClickListener,
        textSizeDP,
        getDebugItemTextColor(isTips),
        isBold,
        isSingleLine,
        ellipsize,
        paddingTopDp,
        paddingLeftDp,
        getDebugItemBackGroundColor(isTips),
        showBottomLine
    )
}

fun getDebugItem(
    content: String,
    listener: View.OnClickListener?,
    longClickListener: View.OnLongClickListener?,
    isBold: Boolean,
    isSingleLine: Boolean,
    ellipsize: TextUtils.TruncateAt?,
    isTips: Boolean
): DebugItemData {
    return getDebugItem(
        content,
        listener,
        longClickListener,
        DebugItemData.DEFAULT_TEXT_SIZE_DP,
        isBold,
        isSingleLine,
        ellipsize,
        DebugItemData.DEFAULT_PADDING_SIZE_DP,
        DebugItemData.DEFAULT_PADDING_SIZE_DP,
        isTips,
        true
    )
}

fun getLittleDebugItem(
    content: String,
    listener: View.OnClickListener?,
    longClickListener: View.OnLongClickListener?,
    isBold: Boolean,
    isSingleLine: Boolean,
    ellipsize: TextUtils.TruncateAt?,
    isTips: Boolean
): DebugItemData {
    return getDebugItem(
        content,
        listener,
        longClickListener,
        10,
        isBold,
        isSingleLine,
        ellipsize,
        12,
        DebugItemData.DEFAULT_PADDING_SIZE_DP,
        isTips,
        true
    )
}

private fun getDebugItem(content: String, isTips: Boolean): DebugItemData {
    return getDebugItem(
        content, null, null, isTips, !isTips, if (isTips) {
            null
        } else {
            TextUtils.TruncateAt.END
        }, isTips
    )
}

fun getTipsItem(content: String): DebugItemData {
    return getDebugItem(content, true)
}

fun getDebugItem(content: String): DebugItemData {
    return getDebugItem(content, false)
}

private fun getDebugItem(
    content: String, listener: View.OnClickListener?, isTips: Boolean
): DebugItemData {
    return getDebugItem(
        content, listener, null, isTips, !isTips, if (isTips) {
            null
        } else {
            TextUtils.TruncateAt.END
        }, isTips
    )
}

fun getTipsItem(content: String, listener: View.OnClickListener?): DebugItemData {
    return getDebugItem(content, listener, true)
}

fun getDebugItem(content: String, listener: View.OnClickListener?): DebugItemData {
    return getDebugItem(content, listener, false)
}


fun getLittleDebugItem(
    content: String,
    listener: View.OnClickListener?,
    isSingleLine: Boolean,
    ellipsize: TextUtils.TruncateAt?,
): DebugItemData {
    return getDebugItem(
        content,
        listener,
        null,
        10,
        false,
        isSingleLine,
        ellipsize,
        12,
        DebugItemData.DEFAULT_PADDING_SIZE_DP,
        isTips = false,
        showBottomLine = true
    )
}

fun BaseDebugListFragment.getRouterItem(content: String): DebugItemData {
    return getLittleDebugItem(content, { RouterAction.openFinalURL(content) }, {
        showInfo("复制并分享路由地址", content)
        true
    }, isBold = false, isSingleLine = true, TextUtils.TruncateAt.END, isTips = false)
}