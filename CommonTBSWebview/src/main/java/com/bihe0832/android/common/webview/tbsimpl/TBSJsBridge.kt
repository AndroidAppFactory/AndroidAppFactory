package com.bihe0832.android.common.webview.tbsimpl

import android.content.Context
import com.bihe0832.android.common.webview.core.WebviewLoggerFile
import com.bihe0832.android.common.webview.core.WebviewLoggerFile.log
import com.bihe0832.android.lib.jsbridge.BaseJsBridge
import com.bihe0832.android.lib.webview.tbs.jsbridge.TBSWebView

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2023/6/2.
 * Description: Description
 */
open class TBSJsBridge(context: Context?) : BaseJsBridge(context) {

    var mWebView: TBSWebView? = null

    constructor(context: Context?, webView: TBSWebView) : this(context) {
        this.mWebView = webView
    }

    override fun loadUrl(s: String) {
        mWebView?.loadUrl(s)
    }

    override fun callback(function: String, result: String, type: ResponseType) {
        WebviewLoggerFile.logCallback(function, result, type)
        super.callback(function, result, type)
    }
}