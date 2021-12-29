package com.bihe0832.android.framework.router

import com.bihe0832.android.lib.request.URLUtils
import java.util.*


/**
 * Created by hardyshi on 2017/6/27.
 *
 */


fun openWebPage(url: String) {
    val map = HashMap<String, String>()
    map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = URLUtils.encode(url)
    RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_WEB_PAGE, map)
}