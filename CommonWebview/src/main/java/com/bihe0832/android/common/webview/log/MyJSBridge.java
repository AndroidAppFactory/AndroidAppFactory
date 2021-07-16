package com.bihe0832.android.common.webview.log;


import android.content.Context;
import com.bihe0832.android.lib.webview.jsbridge.JsBridge;
import com.tencent.smtt.sdk.WebView;

/**
 * @author hardyshi code@bihe0832.com Created on 7/16/21.
 */
public class MyJSBridge extends JsBridge {

    public MyJSBridge(WebView webView, Context context) {
        super(webView, context);
    }

    @Override
    public void callback(String function, String result, ResponseType type) {

        WebviewLoggerFile.INSTANCE.log("---------------------- JsBridge callback start ----------------------");
        WebviewLoggerFile.INSTANCE.log("function:" + function);
        WebviewLoggerFile.INSTANCE.log("result:" + result);
        WebviewLoggerFile.INSTANCE.log("type:" + type.toString());
        WebviewLoggerFile.INSTANCE.log("---------------------- JsBridge callback end ----------------------");
        super.callback(function, result, type);
    }
}