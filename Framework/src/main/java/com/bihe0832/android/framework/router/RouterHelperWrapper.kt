package com.bihe0832.android.framework.router

import android.content.Intent
import com.bihe0832.android.framework.R
import com.bihe0832.android.lib.request.URLUtils
import com.bihe0832.android.lib.theme.ThemeResourcesManager


/**
 * Created by zixie on 2017/6/27.
 *
 */

fun openZixieWeb(url: String) {
    val map = HashMap<String, String>()
    map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = URLUtils.encode(url)
    RouterAction.openFinalURL(
        RouterAction.getFinalURL(RouterConstants.MODULE_NAME_WEB_PAGE, map),
        Intent.FLAG_ACTIVITY_NEW_TASK
    )
}

fun openFeedback() {
    val map = HashMap<String, String>()
    map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] =
        URLUtils.encode(ThemeResourcesManager.getString(R.string.feedback_url))
    RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_FEEDBACK, map)
}

fun shareByQrcode(url: String, titleString: String? = null, descString: String? = null) {
    val map = HashMap<String, String>().apply {
        put(RouterConstants.INTENT_EXTRA_KEY_SHARE_DATA_WITH_ENCODE, URLUtils.encode(url))
        titleString?.let {
            put(
                RouterConstants.INTENT_EXTRA_KEY_SHARE_TITLE_WITH_ENCODE,
                URLUtils.encode(titleString)
            )
        }

        descString?.let {
            put(
                RouterConstants.INTENT_EXTRA_KEY_SHARE_DESC_WITH_ENCODE, URLUtils.encode(descString)
            )
        }
    }
    RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_SHARE_QRCODE, map)
}

fun shareAPP(canSendAPK: Boolean = false) {
    val map = HashMap<String, String>().apply {
        if (canSendAPK) {
            put(RouterConstants.INTENT_EXTRA_KEY_SHARE_SEND_APK, true.toString())
        }
    }
    RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_SHARE_APK, map)
}

fun showLogHome(showAction: Boolean) {
    showLogHome(RouterConstants.MODULE_NAME_SHOW_LOG_LIST, showAction)
}

fun showLogHome(routerHost: String,showAction: Boolean) {
    val map = HashMap<String, String>()
    map[RouterConstants.INTENT_EXTRA_KEY_SHOW_LOG_LIST_ACTION] = showAction.toString()
    RouterAction.openPageByRouter(routerHost, map)
}

fun showH5Log(filePath: String) {
    openZixieWeb("file://" + filePath)
}

fun showLog(
    routerHost: String,
    filePath: String,
    sort: Boolean,
    showLine: Boolean,
    showNum: Int,
) {
    val map = HashMap<String, String>()
    map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = filePath
    map[RouterConstants.INTENT_EXTRA_KEY_SHOW_LOG_SORT] = sort.toString()
    map[RouterConstants.INTENT_EXTRA_KEY_SHOW_LOG_NUM] = showNum.toString()
    map[RouterConstants.INTENT_EXTRA_KEY_SHOW_LOG_SHOW_LINE] = showLine.toString()
    RouterAction.openPageByRouter(routerHost, map)
}

fun showLog(filePath: String, sort: Boolean, showLine: Boolean, showNum: Int) {
    showLog(RouterConstants.MODULE_NAME_SHOW_LOG, filePath, sort, showLine, showNum)
}

fun showLog(filePath: String, sort: Boolean, showLine: Boolean) {
    showLog(filePath, sort, showLine, 2000)
}