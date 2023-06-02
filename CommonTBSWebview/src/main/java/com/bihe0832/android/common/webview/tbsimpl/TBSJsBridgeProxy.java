package com.bihe0832.android.common.webview.tbsimpl;


import android.app.Activity;
import android.net.Uri;

import com.bihe0832.android.common.webview.core.WebviewLoggerFile;
import com.bihe0832.android.lib.jsbridge.BaseJsBridge;
import com.bihe0832.android.lib.jsbridge.BaseJsBridgeProxy;
import com.bihe0832.android.lib.webview.tbs.jsbridge.TBSWebView;


/**
 * 调用的方法为js形如：window.location.href="jsb://getAppInfo/1/ZixieCallback?packagename=com.tencent.mm";
 * 分别是：jsb://方法名/序列号/回调方法?参数key=value
 * 回调方法如不需要可以不用。但是需要调用的方法名和系列号必须要
 */
public class TBSJsBridgeProxy extends BaseJsBridgeProxy {

    protected TBSJsBridge mJsBridge;
    private TBSWebView mWebView;


    public TBSJsBridgeProxy(Activity activity, TBSWebView webView) {
        super(activity);
        mWebView = webView;
        this.mJsBridge = new TBSJsBridge(activity, mWebView);
    }


    @Override
    protected BaseJsBridge getJsBridge() {
        return mJsBridge;
    }

    @Override
    protected void reload() {
        mWebView.reload();
    }

    @Override
    protected void goForward() {
        mWebView.goForward();
    }

    @Override
    public void goBack() {
        mWebView.goBack();
    }

    @Override
    public boolean canGoBack() {
        return mWebView.canGoBack();
    }

    @Override
    protected void callAMethod(Uri uri, String hostAsMethodName, int seqid, String callbackName) {
        WebviewLoggerFile.INSTANCE.logCall(uri, hostAsMethodName, seqid, callbackName);
        super.callAMethod(uri, hostAsMethodName, seqid, callbackName);
    }
}
