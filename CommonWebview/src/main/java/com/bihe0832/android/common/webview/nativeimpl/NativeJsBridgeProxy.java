package com.bihe0832.android.common.webview.nativeimpl;


import android.app.Activity;
import android.net.Uri;

import com.bihe0832.android.common.webview.core.WebViewLoggerFile;
import com.bihe0832.android.lib.jsbridge.BaseJsBridge;
import com.bihe0832.android.lib.jsbridge.BaseJsBridgeProxy;


/**
 * 调用的方法为js形如：window.location.href="jsb://getAppInfo/1/ZixieCallback?packagename=com.tencent.mm";
 * 分别是：jsb://方法名/序列号/回调方法?参数key=value
 * 回调方法如不需要可以不用。但是需要调用的方法名和系列号必须要
 */
public class NativeJsBridgeProxy extends BaseJsBridgeProxy {

    protected NativeJsBridge mJsBridge;

    public NativeWebView getWebView() {
        return mJsBridge.getWebView();
    }

    public NativeJsBridgeProxy(Activity activity, NativeWebView webView) {
        super(activity);
        this.mJsBridge = new NativeJsBridge(activity, webView);
    }

    @Override
    protected BaseJsBridge getJsBridge() {
        return mJsBridge;
    }

    @Override
    protected void reload() {
        try {
            getWebView().reload();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void goForward() {
        try {
            getWebView().goForward();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void goBack() {
        try {
            getWebView().goBack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean canGoBack() {
        try {
            return getWebView().canGoBack();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean canGoForward() {
        try {
            return getWebView().canGoForward();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void callAMethod(Uri uri, String hostAsMethodName, int seqid, String callbackName) {
        WebViewLoggerFile.INSTANCE.logCall(uri, hostAsMethodName, seqid, callbackName);
        super.callAMethod(uri, hostAsMethodName, seqid, callbackName);
    }
}
