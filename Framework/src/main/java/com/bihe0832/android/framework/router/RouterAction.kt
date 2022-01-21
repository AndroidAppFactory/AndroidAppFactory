package com.bihe0832.android.framework.router

import android.app.Activity
import android.content.Intent
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.router.Routers


/**
 * Created by hardyshi on 2017/6/27.
 *
 */
object RouterAction {

    val SCHEME by lazy {
        ZixieContext.applicationContext?.getString(R.string.router_schema) ?: "zixie"
    }

    /**
     * 通过传入path和参数，获取最终完整的路由链接，调用示例
     *
     * RouterHelper.getFinalURL("zixie", RouterConstants.MODULE_NAME_TEST,mutableMapOf(
     *      RouterConstants.INTENT_EXTRA_KEY_TEST_ITEM_TAB to 1
     * ))
     *
     *
     */
    fun getFinalURL(schema: String, path: String, para: Map<String, String>?): String {
        var url = "${schema}://$path"
        para?.apply {
            if (this.isNotEmpty()) {
                url = "$url?"
                for ((key, value) in para) {
                    url = "$url$key=$value&"
                }
            }
        }
        ZLog.d("Router:$url")
        return url
    }

    fun getFinalURL(pathHost: String, para: Map<String, String>?): String {
        return getFinalURL(SCHEME, pathHost, para)
    }

    /**
     * 通过传入path和参数，获取最终完整的路由链接，调用示例
     *
     * RouterHelper.getFinalURL("zixie", RouterConstants.MODULE_NAME_TEST)
     *
     * return zixie://test
     *
     */
    fun getFinalURL(schema: String, path: String): String {
        return "${schema}://$path"
    }

    fun getFinalURL(pathHost: String): String {
        return getFinalURL(SCHEME, pathHost)
    }


    fun open(schema: String, path: String) {
        Routers.open(
            ZixieContext.applicationContext,
            "${schema}://$path",
            Intent.FLAG_ACTIVITY_SINGLE_TOP
        )
    }

    fun open(schema: String, path: String, para: Map<String, String>?) {
        Routers.open(
            ZixieContext.applicationContext,
            getFinalURL(schema, path, para),
            Intent.FLAG_ACTIVITY_SINGLE_TOP
        )
    }

    fun openFinalURL(path: String) {
        openFinalURL(path, Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }

    fun openFinalURL(path: String, startFlag: Int) {
        Routers.open(ZixieContext.applicationContext, path, startFlag)
    }

    fun openForResult(schema: String, activity: Activity, path: String, requestCode: Int) {
        Routers.openForResult(
            activity,
            getFinalURL(schema, path),
            requestCode,
            Intent.FLAG_ACTIVITY_SINGLE_TOP
        )
    }

    fun openForResult(
        schema: String,
        activity: Activity,
        path: String,
        para: Map<String, String>?,
        requestCode: Int
    ) {
        Routers.openForResult(
            activity,
            getFinalURL(schema, path, para),
            requestCode,
            Intent.FLAG_ACTIVITY_SINGLE_TOP
        )
    }

    fun openForResult(
        activity: Activity,
        url: String,
        requestCode: Int
    ) {
        Routers.openForResult(
            activity,
            url,
            requestCode,
            Intent.FLAG_ACTIVITY_SINGLE_TOP
        )
    }

    fun openPageByRouter(pathHost: String, para: Map<String, String>?) {
        open(SCHEME, pathHost, para)
    }

    fun openPageByRouter(pathHost: String) {
        open(SCHEME, pathHost)
    }
}
