package com.bihe0832.android.app.router

import com.bihe0832.android.lib.request.URLUtils


/**
 * 路由辅助方法扩展
 *
 * 提供路由拦截列表配置和常用路由跳转方法
 *
 * @author zixie code@bihe0832.com
 * Created on 2017/6/27.
 */

/**
 * 获取需要权限检查拦截的路由列表
 *
 * 这些路由需要拥有特定权限才能进入（前提是已经登录）
 *
 * @return 需要权限检查的路由列表
 */
fun getNeedCheckInterceptHostList(): List<String> {
    return listOf(

    )
}

/**
 * 获取需要登录拦截的路由列表
 *
 * 这些路由只需要登录就能进入
 *
 * @return 需要登录的路由列表
 */
fun getNeedLoginInterceptHostList(): List<String> {
    return listOf<String>(

    )
}

/**
 * 获取不需要检查，直接跳过的路由列表
 *
 * 这些路由不会被拦截器处理
 *
 * @return 跳过检查的路由列表
 */
fun getSkipListHostList(): List<String> {
    return listOf<String>(
        RouterConstants.MODULE_NAME_SPLASH
    )
}

/**
 * 打开 Web 页面
 *
 * 通过路由跳转到内置浏览器页面
 *
 * @param url 要打开的网页 URL
 */
fun openWebPage(url: String) {
    val map = HashMap<String, String>()
    map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = URLUtils.encode(url)
    RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_WEB_PAGE, map)
}

