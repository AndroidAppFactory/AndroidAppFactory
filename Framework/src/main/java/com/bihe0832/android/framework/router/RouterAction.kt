package com.bihe0832.android.framework.router

import android.app.Activity
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.router.Routers
import com.bihe0832.android.framework.ZixieContext


/**
 * Created by hardyshi on 2017/6/27.
 *
 */
object RouterAction {


    /**
     * 通过传入path和参数，获取最终完整的路由链接，调用示例
     *
     * RouterHelper.getFinalURL("zixie", RouterConstants.MODULE_NAME_TEST,mutableMapOf(
     *      RouterConstants.INTENT_EXTRA_KEY_TEST_ITEM_TAB to 1
     * ))
     *
     * return man://test?tab=1
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

    /**
     * 通过传入path和参数，获取最终完整的路由链接，调用示例
     *
     * RouterHelper.getFinalURL("zixie", RouterConstants.MODULE_NAME_TEST)
     *
     * return man://test
     *
     */
    fun getFinalURL(schema: String, path: String): String {
        return "${schema}://$path"
    }

    fun open(schema: String, path: String, para: Map<String, String>?) {
        Routers.open(ZixieContext.applicationContext, getFinalURL(schema, path, para))
    }

    fun open(schema: String, path: String) {
        Routers.open(ZixieContext.applicationContext, "${schema}://$path")
    }

    fun openFinalURL(path: String) {
        Routers.open(ZixieContext.applicationContext, path)
    }

    fun openForResult(schema: String, activity: Activity, path: String, requestCode: Int) {
        Routers.openForResult(activity, getFinalURL(schema, path), requestCode)
    }

    fun openForResult(schema: String, activity: Activity, path: String, para: Map<String, String>?, requestCode: Int) {
        Routers.openForResult(activity, getFinalURL(schema, path, para), requestCode)
    }
}
