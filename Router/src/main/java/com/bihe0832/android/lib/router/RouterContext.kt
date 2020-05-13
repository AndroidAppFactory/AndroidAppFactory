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
        fun notFound(context: Context, uri: Uri)

        fun beforeOpen(context: Context, uri: Uri): Boolean

        fun afterOpen(context: Context, uri: Uri)

        fun error(context: Context, uri: Uri, e: Throwable)
    }

}
