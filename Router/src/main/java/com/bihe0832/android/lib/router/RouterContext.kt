package com.bihe0832.android.lib.router

import android.content.Context
import android.net.Uri

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-07-19.
 * Description: Description
 */
object RouterContext {
    private var mRouterCallback: RouterCallback? = null

    fun getGlobalRouterCallback(): RouterCallback?{
        return mRouterCallback
    }

    fun setGlobalRouterCallback(callback: RouterCallback){
        mRouterCallback = callback
    }

    interface RouterCallback {
        fun notFound(context: Context, uri: Uri, source: String)

        fun beforeOpen(context: Context, uri: Uri, source: String): Boolean

        fun afterOpen(context: Context, uri: Uri, source: String)

        fun error(context: Context, uri: Uri, source: String, e: Throwable)
    }

}
