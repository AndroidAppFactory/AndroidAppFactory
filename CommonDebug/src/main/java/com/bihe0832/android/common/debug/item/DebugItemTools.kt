package com.bihe0832.android.common.debug.item

import android.graphics.Color
import android.text.TextUtils
import android.view.View
import com.bihe0832.android.common.compose.debug.DebugComposeRootActivity
import com.bihe0832.android.common.debug.R
import com.bihe0832.android.common.debug.base.BaseDebugListFragment
import com.bihe0832.android.common.file.preview.ContentItemData
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ZixieContext.applicationContext
import com.bihe0832.android.framework.router.RouterAction


/**
 * @author zixie code@bihe0832.com Created on 2019-11-21. Description: Description
 */

fun getDebugItemTextColor(isTips: Boolean): Int {
    return if (isTips) {
        applicationContext!!.resources.getColor(R.color.md_theme_onSecondary)
    } else {
        applicationContext!!.resources.getColor(R.color.md_theme_onBackground)
    }
}

fun getDebugItemBackGroundColor(isTips: Boolean): Int {
    return if (isTips) {
        applicationContext!!.resources.getColor(R.color.md_theme_secondary)
    } else {
        applicationContext!!.resources.getColor(R.color.md_theme_background)
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
): ContentItemData {
    return ContentItemData(
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
        if (showBottomLine) {
            ZixieContext.applicationContext!!.getColor(R.color.md_theme_outline)
        } else {
            Color.TRANSPARENT
        }
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
): ContentItemData {
    return getDebugItem(
        content,
        listener,
        longClickListener,
        ContentItemData.DEFAULT_TEXT_SIZE_DP,
        isBold,
        isSingleLine,
        ellipsize,
        ContentItemData.DEFAULT_PADDING_SIZE_DP,
        ContentItemData.DEFAULT_PADDING_SIZE_DP,
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
): ContentItemData {
    return getDebugItem(
        content,
        listener,
        longClickListener,
        10,
        isBold,
        isSingleLine,
        ellipsize,
        12,
        ContentItemData.DEFAULT_PADDING_SIZE_DP,
        isTips,
        true
    )
}

private fun getDebugItem(content: String, isTips: Boolean): ContentItemData {
    return getDebugItem(
        content, null, null, isTips, !isTips, if (isTips) {
            null
        } else {
            TextUtils.TruncateAt.END
        }, isTips
    )
}

fun getTipsItem(content: String): ContentItemData {
    return getDebugItem(content, true)
}

fun getDebugItem(content: String): ContentItemData {
    return getDebugItem(content, false)
}

private fun getDebugItem(
    content: String, listener: View.OnClickListener?, isTips: Boolean
): ContentItemData {
    return getDebugItem(
        content, listener, null, isTips, !isTips, if (isTips) {
            null
        } else {
            TextUtils.TruncateAt.END
        }, isTips
    )
}

fun getTipsItem(content: String, listener: View.OnClickListener?): ContentItemData {
    return getDebugItem(content, listener, true)
}

fun getDebugItem(content: String, listener: View.OnClickListener?): ContentItemData {
    return getDebugItem(content, listener, false)
}

fun getComposeDebugItem(
    content: String,
    composeName: String
): ContentItemData {
    return getDebugItem(content) {
        DebugComposeRootActivity.startComposeActivity(
            ZixieContext.applicationContext!!,
            content,
            composeName
        )
    }
}

fun getLittleDebugItem(
    content: String,
    listener: View.OnClickListener?,
    isSingleLine: Boolean,
    ellipsize: TextUtils.TruncateAt?,
): ContentItemData {
    return getDebugItem(
        content,
        listener,
        null,
        10,
        false,
        isSingleLine,
        ellipsize,
        12,
        ContentItemData.DEFAULT_PADDING_SIZE_DP,
        isTips = false,
        showBottomLine = true
    )
}

fun BaseDebugListFragment.getRouterItem(content: String): ContentItemData {
    return getLittleDebugItem(content, { RouterAction.openFinalURL(content) }, {
        showInfo("复制并分享路由地址", content)
        true
    }, isBold = false, isSingleLine = true, TextUtils.TruncateAt.END, isTips = false)
}