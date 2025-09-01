package com.bihe0832.android.app.router

import com.bihe0832.android.lib.request.URLUtils


/**
 * Created by zixie on 2017/6/27.
 *
 */
//需要拥有权限才能进入的特权，前提是已经登录了
fun getNeedCheckInterceptHostList(): List<String> {
    return listOf(

    )
}

//只需要登录就能进入的特权
fun getNeedLoginInterceptHostList(): List<String> {
    return listOf<String>(

    )
}

//不需要检查，直接跳过的路由
fun getSkipListHostList(): List<String> {
    return listOf<String>(
        RouterConstants.MODULE_NAME_SPLASH
    )
}

fun openWebPage(url: String) {
    val map = HashMap<String, String>()
    map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = URLUtils.encode(url)
    RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_WEB_PAGE, map)
}

