package com.bihe0832.android.lib.webview.tbs.jsbridge

import android.content.Context
import com.bihe0832.android.lib.jsbridge.BaseJsBridge

open class TBSJsBridge(context: Context?) : BaseJsBridge(context) {

    var mWebView: TBSWebView? = null

    constructor(context: Context?, webView: TBSWebView) : this(context) {
        this.mWebView = webView
    }

    override fun loadUrl(s: String) {
        mWebView?.loadUrl(s)
    }
}