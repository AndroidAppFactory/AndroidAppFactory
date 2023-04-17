package com.bihe0832.android.framework.router

import android.content.Context
import android.net.Uri
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.lib.router.RouterContext

/**
 * Created by zixie on 2017/6/27.
 *
 */
object RouterInterrupt {

    val MODULE_NAME_ROUTER = "Router"

    interface RouterProcess {
        //是否需要登录
        fun needLogin(uri: Uri, source: String): Boolean

        //是否需要拦截
        fun needInterrupt(uri: Uri, source: String): Boolean

        //拦截后操作
        fun doInterrupt(uri: Uri, source: String): Boolean

        fun afterOpen(context: Context, uri: Uri, source: String) {
            logRouterToFile("afterOpen ->$uri")
        }

        fun notFound(context: Context, uri: Uri, source: String) {
            logRouterToFile("notFound ->$uri")
        }

        fun error(context: Context, uri: Uri, source: String, e: Throwable) {
            logRouterToFile("error ->$uri")
        }
    }

    @Synchronized
    fun init(process: RouterProcess) {
        RouterContext.setGlobalRouterCallback(object : RouterContext.RouterCallback {
            override fun afterOpen(context: Context, uri: Uri, source: String) {
                process.afterOpen(context, uri, source)
            }

            //跳转前拦截
            override fun beforeOpen(context: Context, uri: Uri, source: String): Boolean {
                logRouterToFile("beforeOpen ->$uri")
                return if (process.needInterrupt(uri, source) || process.needLogin(uri, source)) {
                    //拦截处理结果
                    logRouterToFile("needInterrupt uri ->$uri")
                    process.doInterrupt(uri, source)
                } else {
                    false
                }
            }

            override fun error(context: Context, uri: Uri, source: String, e: Throwable) {
                process.error(context, uri, source, e)
            }

            override fun notFound(context: Context, uri: Uri, source: String) {
                process.notFound(context, uri, source)
            }
        })
    }
    
    fun logRouterToFile(msg: String) {
        LoggerFile.logFile(getRouterLogPath(), msg)

    }

    fun getRouterLogPath(): String {
        return LoggerFile.getZixieFileLogPathByModule(MODULE_NAME_ROUTER)

    }
}
