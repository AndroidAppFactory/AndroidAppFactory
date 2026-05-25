package com.bihe0832.android.base.debug.webview;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.JavascriptInterface;

import com.bihe0832.android.common.webview.CommonWebviewFragment;
import com.bihe0832.android.lib.log.ZLog;

/**
 * 测试 WebView Renderer 进程崩溃恢复。
 *
 * 验证 onRenderProcessGone 修复逻辑：
 * 1. 先加载正常网页（确保 Renderer 进程存活）
 * 2. 页面加载完成后，顶部会注入红色「触发 Renderer 崩溃」按钮
 * 3. 点击按钮后，通过 JavascriptInterface 回调 Native 执行 loadUrl("chrome://crash")
 * 4. 观察：应用不闪退，显示错误页
 * 5. 点击"重试"验证 WebView 能正常重建并再次注入按钮
 */
public class DebugRenderProcessGoneFragment extends CommonWebviewFragment {

    private static final String TAG = "DebugRenderProcessGone";
    private static final String JS_INTERFACE_NAME = "_DebugCrashBridge";

    @Override
    protected void parseBundle(Bundle bundle, boolean isOnCreate) {
        super.parseBundle(bundle, isOnCreate);
        mIntentUrl = "https://www.qq.com";
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        // 添加 JavascriptInterface，让注入的 JS 按钮能回调 Native
        if (mWebView != null) {
            mWebView.addJavascriptInterface(new CrashBridge(), JS_INTERFACE_NAME);
        }
    }

    @Override
    protected void recreateWebView() {
        super.recreateWebView();
        // 重建后重新注入 JavascriptInterface
        if (mWebView != null) {
            mWebView.addJavascriptInterface(new CrashBridge(), JS_INTERFACE_NAME);
        }
    }

    @Override
    protected void onWebClientPageFinished() {
        super.onWebClientPageFinished();
        injectCrashButton();
    }

    private void injectCrashButton() {
        if (mWebView != null) {
            String js = "javascript:(function(){" +
                    "if(document.getElementById('_render_crash_btn'))return;" +
                    "var d=document.createElement('div');" +
                    "d.id='_render_crash_btn';" +
                    "d.style.cssText='position:fixed;top:0;left:0;right:0;z-index:999999;" +
                    "background:#ff4444;padding:14px 12px;text-align:center;" +
                    "font-size:15px;color:#fff;font-weight:bold;cursor:pointer;" +
                    "box-shadow:0 2px 8px rgba(0,0,0,0.3);';" +
                    "d.innerText='\\u26A0 \\u70B9\\u51FB\\u6B64\\u5904\\u89E6\\u53D1 Renderer \\u5D29\\u6E83';" +
                    "d.onclick=function(){" + JS_INTERFACE_NAME + ".triggerCrash();};" +
                    "document.body.insertBefore(d,document.body.firstChild);" +
                    "document.body.style.paddingTop='50px';" +
                    "})()";
            mWebView.loadUrl(js);
            ZLog.i(TAG, "Injected crash test button via JavascriptInterface");
        }
    }

    /**
     * JS 回调 Native 的桥接对象
     */
    private class CrashBridge {
        @JavascriptInterface
        public void triggerCrash() {
            ZLog.e(TAG, "CrashBridge.triggerCrash() called from JS, loading chrome://crash");
            new Handler(Looper.getMainLooper()).post(() -> {
                if (mWebView != null) {
                    mWebView.loadUrl("chrome://crash");
                }
            });
        }
    }
}
