package com.bihe0832.android.common.webview.log;

import android.app.Activity;
import android.net.Uri;

import com.bihe0832.android.lib.webview.BaseWebView;
import com.bihe0832.android.lib.webview.jsbridge.BaseJsBridgeProxy;
import com.bihe0832.android.lib.webview.jsbridge.JsBridge;
import com.tencent.smtt.sdk.WebView;
import java.net.URLDecoder;

/**
 * @author zixie code@bihe0832.com Created on 7/16/21.
 */
public class MyBaseJsBridgeProxy extends BaseJsBridgeProxy {

    public MyBaseJsBridgeProxy(BaseWebView webView, Activity activity) {
        super(webView, activity, new MyJSBridge(webView, activity));
    }

    public MyBaseJsBridgeProxy(BaseWebView webView, Activity activity, JsBridge jsBridge) {
        super(webView, activity, jsBridge);
    }

    @Override
    protected void callAMethod(Uri uri, String hostAsMethodName, int seqid, String callbackName) {
        WebviewLoggerFile.INSTANCE.log("---------------------- JsBridge call start ----------------------");
        WebviewLoggerFile.INSTANCE.log("uri:" + URLDecoder.decode(uri.toString()));
        WebviewLoggerFile.INSTANCE.log("hostAsMethodName:" + hostAsMethodName);
        WebviewLoggerFile.INSTANCE.log("seqid:" + seqid);
        WebviewLoggerFile.INSTANCE.log("callbackName:" + callbackName);
        WebviewLoggerFile.INSTANCE.log("---------------------- JsBridge call end ----------------------");
        super.callAMethod(uri, hostAsMethodName, seqid, callbackName);
    }
}