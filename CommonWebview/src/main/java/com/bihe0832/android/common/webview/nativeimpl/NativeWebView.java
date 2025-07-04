package com.bihe0832.android.common.webview.nativeimpl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.bihe0832.android.lib.utils.os.BuildUtils;


public class NativeWebView extends WebView {

    private static final String APP_CACAHE_DIRNAME = "/webcache";

    private OnScrollChangedCallback mOnScrollChangedCallback;
    protected boolean hasDoActionBeforeLoadURL = false;

    public NativeWebView(Context arg0) {
        super(arg0);
        initBaseWebView();
    }

    @Override
    public void reload() {
        hasDoActionBeforeLoadURL = false;
        super.reload();
    }

    public boolean hasDoActionBeforeLoadURL() {
        return hasDoActionBeforeLoadURL;
    }

    public void doActionBeforeLoadURL() {
        hasDoActionBeforeLoadURL = true;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public NativeWebView(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
        initBaseWebView();
    }

    public NativeWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBaseWebView();
    }

    public void initBaseWebView() {
        this.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        initWebViewSettings();

        this.getRootView().setClickable(true);
    }

    public interface OnScrollChangedCallback {

        void onScroll(int l, int t);
    }

    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangedCallback != null) {
            mOnScrollChangedCallback.onScroll(l, t);
        }
    }

    public void setOnScrollChangedCallback(final OnScrollChangedCallback onScrollChangedCallback) {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }

    private void initWebViewSettings() {
        //防webview远程代码执行漏洞
        if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.HONEYCOMB) {
            removeJavascriptInterface("searchBoxJavaBridge_");
            removeJavascriptInterface("accessibility");
            removeJavascriptInterface("accessibilityTraversal");
            // Hide the zoom controls for HONEYCOMB+
            getSettings().setDisplayZoomControls(false);
        }

        setHTMLSupport(getSettings());
        resetCacheType(getSettings());
    }

    private void setHTMLSupport(WebSettings webSetting) {
        //设置WebView属性，能够执行Javascript脚本
        webSetting.setJavaScriptEnabled(true);
        webSetting.setAllowUniversalAccessFromFileURLs(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setSupportMultipleWindows(true);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSetting.setUseWideViewPort(true);  //将图片调整到适合webview的大小
        webSetting.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        //支持HTTP和HTTPS混合模式
        if (BuildUtils.INSTANCE.getSDK_INT() > Build.VERSION_CODES.LOLLIPOP) {
            //由于X5没有定义对应的常量，因此直接使用实际值，对应官方webkit的WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            webSetting.setMixedContentMode(0);
        }

        //支持缩放，默认为true。是下面那个的前提。
        webSetting.setSupportZoom(true);
        //设置内置的缩放控件。
        webSetting.setBuiltInZoomControls(true);

        //若上面是false，则该WebView不可缩放，这个不管设置什么都不能缩放。
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); //支持内容重新布局
        webSetting.supportMultipleWindows();  //多窗口
        webSetting.setNeedInitialFocus(true); //当webview调用requestFocus时为webview设置节点
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSetting.setLoadsImagesAutomatically(true);  //支持自动加载图片
        webSetting.setDefaultTextEncodingName("utf-8");//设置编码格式
    }

    private void resetCacheType(WebSettings webSetting) {
        //设置缓存位置
        String cacheDirPath =
                this.getContext().getApplicationContext().getFilesDir().getAbsolutePath() + APP_CACAHE_DIRNAME;
        //设置缓存类型
        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
        // 开启 DOM storage API 功能
        webSetting.setDomStorageEnabled(true);
        //开启 database storage API 功能
        webSetting.setDatabaseEnabled(true);
        //设置数据库缓存路径
        webSetting.setDatabasePath(cacheDirPath);
        //设置  Application Caches 缓存目录
        webSetting.setDatabasePath(cacheDirPath);
        //开启 Application Caches 功能
        webSetting.setDatabaseEnabled(true);
    }
}
