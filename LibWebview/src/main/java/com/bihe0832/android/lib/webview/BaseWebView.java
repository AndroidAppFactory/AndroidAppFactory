package com.bihe0832.android.lib.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;

import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;


public class BaseWebView extends WebView{

    private static final String APP_CACAHE_DIRNAME = "/webcache";

    private OnScrollChangedCallback mOnScrollChangedCallback;


    public BaseWebView(Context arg0) {
        super(arg0);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public BaseWebView(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
        this.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        initWebViewSettings();

        this.getRootView().setClickable(true);
    }
    public BaseWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
        if (mOnScrollChangedCallback != null) mOnScrollChangedCallback.onScroll(l, t);
    }

    public void setOnScrollChangedCallback(final OnScrollChangedCallback onScrollChangedCallback) {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }

    private void initWebViewSettings() {
        //防webview远程代码执行漏洞
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            removeJavascriptInterface("searchBoxJavaBridge_");
            removeJavascriptInterface("accessibility");
            removeJavascriptInterface("accessibilityTraversal");
            // Hide the zoom controls for HONEYCOMB+
            getSettings().setDisplayZoomControls(false);
        }

        setHTMLSupport(getSettings());
        resetCacheType(getSettings());

        if (getX5WebViewExtension() != null) {
            Bundle data = new Bundle();
            data.putBoolean("standardFullScreen", false);// true表示标准全屏，竖屏Web播放横屏视频不转为横屏，false表示X5全屏，此时竖屏Web播放横屏视频会转为横屏；不设置默认false，
            data.putBoolean("supportLiteWnd", false);// false：关闭小窗；true：开启小窗；不设置默认true，
            data.putInt("DefaultVideoScreen", 2);// 1：以页面内开始播放，2：以全屏开始播放；不设置默认：1
            getX5WebViewExtension().invokeMiscMethod("setVideoParams", data);
            //设置WebView是否通过手势触发播放媒体，默认是true，需要手势触发。
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                //设置WebView是否通过手势触发播放媒体，默认是true，需要手势触发。
                getSettings().setMediaPlaybackRequiresUserGesture(true);
            }
            //接口禁止(直接或反射)调用，避免视频画面无法显示
            setDrawingCacheEnabled(true);
        }
    }

    private void setHTMLSupport(WebSettings webSetting) {
        //设置WebView属性，能够执行Javascript脚本
        webSetting.setJavaScriptEnabled(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setSupportMultipleWindows(false);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSetting.setUseWideViewPort(true);  //将图片调整到适合webview的大小
        webSetting.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        //支持HTTP和HTTPS混合模式
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
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
        //设置缓存类型
        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
        //设置缓存位置
        String cacheDirPath = this.getContext().getApplicationContext().getFilesDir().getAbsolutePath() + APP_CACAHE_DIRNAME;
        //JYLog.d("cacheDirPath=" + cacheDirPath);
        //设置数据库缓存路径
        webSetting.setDatabasePath(cacheDirPath);
        //设置  Application Caches 缓存目录
        webSetting.setAppCachePath(cacheDirPath);
        //开启 Application Caches 功能
        webSetting.setAppCacheEnabled(true);
        // 开启 DOM storage API 功能
        webSetting.setDomStorageEnabled(true);
        //开启 database storage API 功能
        webSetting.setDatabaseEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
    }

}
