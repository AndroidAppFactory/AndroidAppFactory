package com.bihe0832.android.framework.router

import android.content.Context
import android.net.Uri
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.router.RouterContext

/**
 * Created by hardyshi on 2017/6/27.
 *
 */
object RouterInterrupt {

    val TAG = "RouterInterrupt->"
    interface RouterProcess {
        //是否需要登录
        fun needLogin(uri: Uri): Boolean
        //是否需要拦截
        fun needInterrupt(uri: Uri): Boolean
        //拦截后操作
        fun doInterrupt(uri: Uri): Boolean
    }

    @Synchronized
    fun init(process: RouterProcess) {
        RouterContext.setGlobalRouterCallback(object : RouterContext.RouterCallback {
            override fun afterOpen(context: Context, uri: Uri) {
                ZLog.d(TAG,"：afterOpen ->$uri")
            }

            //跳转前拦截
            override fun beforeOpen(context: Context, uri: Uri): Boolean {
                ZLog.d(TAG,"：beforeOpen ->$uri")
                return if (process.needInterrupt(uri) || process.needLogin(uri)) {
                    intercept(uri, process)
                } else {
                    false
                }
            }

            override fun error(context: Context, uri: Uri, e: Throwable) {
                ZLog.d(TAG," ：error ->$uri")
            }

            override fun notFound(context: Context, uri: Uri) {
                ZLog.d(TAG," ：notFound ->$uri")
            }
        })
    }


    //拦截处理结果
    fun intercept(uri: Uri, process: RouterProcess): Boolean {
        ZLog.d(TAG,"：beforeOpen uri ->$uri")
        return process.doInterrupt(uri)
    }
}
