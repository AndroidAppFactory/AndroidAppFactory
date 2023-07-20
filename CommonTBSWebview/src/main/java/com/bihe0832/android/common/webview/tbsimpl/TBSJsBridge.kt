package com.bihe0832.android.common.webview.tbsimpl

import android.content.Context
import com.bihe0832.android.common.webview.core.WebViewLoggerFile
import com.bihe0832.android.common.webview.tbs.TBSWebView
import com.bihe0832.android.lib.jsbridge.BaseJsBridge

/**
 * @author zixie code@bihe0832.com
 * Created on 2023/6/2.
 * Description: Description
 */
open class TBSJsBridge(context: Context?) : BaseJsBridge(context) {

    private var mWebView: TBSWebView? = null

    public fun getWebView(): TBSWebView? {
        return mWebView
    }

    constructor(context: Context?, webView: TBSWebView) : this(context) {
        this.mWebView = webView
    }

    override fun loadUrl(s: String) {
        mWebView?.loadUrl(s)
    }

    override fun callback(function: String, result: String, type: ResponseType) {
        WebViewLoggerFile.logCallback(function, result, type)
        super.callback(function, result, type)
    }
}