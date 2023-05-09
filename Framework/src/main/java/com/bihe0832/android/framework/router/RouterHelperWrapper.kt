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
    RouterAction.openFinalURL(RouterAction.getFinalURL(RouterConstants.MODULE_NAME_WEB_PAGE, map), Intent.FLAG_ACTIVITY_NEW_TASK)
}

fun openFeedback() {
    val map = HashMap<String, String>()
    map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = URLUtils.encode(ThemeResourcesManager.getString(R.string.feedback_url))
    RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_FEEDBACK, map)
}

fun shareByQrcode(url: String, titleString: String? = null, descString: String? = null) {
    val map = HashMap<String, String>().apply {
        put(RouterConstants.INTENT_EXTRA_VALUE_SHARE_DATA_WITH_ENCODE, URLUtils.encode(url))
        titleString?.let {
            put(RouterConstants.INTENT_EXTRA_VALUE_SHARE_TITLE_WITH_ENCODE, URLUtils.encode(titleString))
        }

        descString?.let {
            put(RouterConstants.INTENT_EXTRA_VALUE_SHARE_DESC_WITH_ENCODE, URLUtils.encode(descString))
        }
    }
    RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_SHARE_QRCODE, map)
}