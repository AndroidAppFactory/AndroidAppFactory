package com.bihe0832.android.common.webview.tbsimpl;


import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.bihe0832.android.common.webview.R;
import com.bihe0832.android.common.webview.base.BaseWebViewFragment;
import com.bihe0832.android.common.webview.core.WebViewLoggerFile;
import com.bihe0832.android.common.webview.tbs.TBSWebView;
import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.http.common.core.BaseConnection;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener;
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.MimeTypeMap;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class TBSWebViewFragment extends BaseWebViewFragment {

    public TBSWebView mWebView;
    protected WebViewRefreshCallback mRefreshCallback = null;
    private View mCustomView;
    private int mOriginalSystemUiVisibility;
    private int mOriginalOrientation;
    private IX5WebChromeClient.CustomViewCallback mCustomViewCallback;

    public void setOnWebViewRefreshCallback(WebViewRefreshCallback callback) {
        mRefreshCallback = callback;
    }

    protected WebViewClient getWebViewClient() {
        return new TBSWebViewFragment.MyWebViewClient();
    }

    protected WebChromeClient getWebChromeClient() {
        return new TBSWebViewFragment.MyWebChromeClient();
    }

    protected void createWebView() {
        mWebView = new TBSWebView(getContext(), null);
        mWebView.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void addWebViewToLayout(ViewGroup mViewParent) {
        mViewParent.addView(mWebView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected int getLayoutID() {
        return R.layout.common_zixie_fragment_webview;
    }

    @Override
    protected void initRefreshAndScrollChangedCallback() {
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                mSwipeLayout.setRefreshing(false);
                if (mJSBridgeProxy != null && mJSBridgeProxy.canPullToRefresh()) {
                    mJSBridgeProxy.onRefresh();
                    if (mRefreshCallback != null) {
                        mRefreshCallback.onRefresh(mWebView);
                    } else {
                        // ⚠️ 此处的reload 只会走webview的内部逻辑，不会走 loadURL的完整逻辑，如果还有外部逻辑要处理，建议用mRefreshCallback
                        mWebView.reload();
                    }
                    mSwipeLayout.setEnabled(true);
                }
            }
        });
        mWebView.setOnScrollChangedCallback(new TBSWebView.OnScrollChangedCallback() {
            public void onScroll(int l, int t) {
                ZLog.d(TAG, "We Scrolled etc..." + l + " t =" + t);
                _webViewScrollTopLiveData.postValue(t);
                if (t > 0) {
                    //webView不是顶部
                    mSwipeLayout.setEnabled(false);
                } else {
                    //webView在顶部
                    if (mJSBridgeProxy != null && mJSBridgeProxy.canPullToRefresh()) {
                        mSwipeLayout.setEnabled(true);
                    } else {
                        mSwipeLayout.setEnabled(false);
                    }
                }
            }
        });
    }

    @Override
    protected void initWebContentsDebuggingEnabled() {
        if (BuildUtils.INSTANCE.getSDK_INT() > VERSION_CODES.KITKAT) {
            if (!ZixieContext.INSTANCE.isOfficial()) {
                mWebView.setWebContentsDebuggingEnabled(true);
            } else {
                mWebView.setWebContentsDebuggingEnabled(false);
            }
        }
    }

    @Override
    protected void initWebChromeClient() {
        mWebView.setWebViewClient(getWebViewClient());
    }

    @Override
    protected void initWebViewClient() {
        mWebView.setWebChromeClient(getWebChromeClient());
    }

    @Override
    protected void initUserAgentSupport() {
        String userAgent = mWebView.getSettings().getUserAgentString();
        String myAgent = " " + getUserAgentString();
        if (TextUtils.isEmpty(userAgent)) {
            userAgent = myAgent;
        } else {
            if (userAgent.endsWith("/")) {
                userAgent = userAgent + myAgent;
            } else {
                userAgent = userAgent + "/" + myAgent;
            }
        }
        mWebView.getSettings().setUserAgentString(userAgent);
    }

    @Override
    protected void initCookieSupport() {
        TBSCookieManager.INSTANCE.init(mWebView);
    }

    @Override
    protected void loadFinalURL(String finalURL, String data) {
        if (null != mWebView) {
            mWebView.doActionBeforeLoadURL();
            if (TextUtils.isEmpty(data)) {
                mWebView.loadUrl(finalURL);
            } else {
                mWebView.postUrl(finalURL, data.getBytes());
            }
        }
    }

    @Override
    protected void onJSBridgeResume() {
        super.onJSBridgeResume();
    }

    @Override
    protected void onJSBridgePause() {
        super.onJSBridgePause();
    }

    protected WebResourceResponse interceptRequestResult(String url) {
        if (null != mWebView && !mWebView.hasDoActionBeforeLoadURL()) {
            //主要用于解决页面通过webview的reload刷新时没有走 actionBeforeLoadURL 导致一些前置逻辑被跳过
            actionBeforeLoadURL(url);
            mWebView.doActionBeforeLoadURL();
        }
        if (null != getGlobalLocalRes() && getGlobalLocalRes().containsKey(url)) {
            try {
                String type = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(FileUtils.INSTANCE.getExtensionName(url));
                return new WebResourceResponse(type, BaseConnection.HTTP_REQ_VALUE_CHARSET_UTF8,
                        getContext().getAssets().open(getGlobalLocalRes().get(url)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected boolean processRefererString(String url, String mRefererString) {
        if (!TextUtils.isEmpty(mRefererString)) {
            HashMap<String, String> headerHashMap = new HashMap<>();
            headerHashMap.put(HEADER_NAME_REFERER, mRefererString);
            mWebView.loadUrl(url, headerHashMap);
        } else {
            mWebView.loadUrl(url);
        }
        return true;
    }

    protected TBSJsBridgeProxy getJsBridgeProxy() {
        return new TBSJsBridgeProxy(getActivity(), mWebView);
    }

    @Override
    protected void destroyWebView() {
        if (mWebView != null) {
            mWebView.setOnScrollChangedCallback(null);
            mWebView.stopLoading();
            mWebView.clearHistory();
            mWebView.destroy();
            mWebView = null;
        }
    }

    @Override
    public void setCookie(String url, String name, String value) {
        TBSCookieManager.INSTANCE.setCookie(url, name, value);
    }

    @Override
    public void syncCookie() {
        TBSCookieManager.INSTANCE.syncCookie();
    }

    @Override
    public void removeCookiesForDomain(String url) {
        TBSCookieManager.INSTANCE.removeCookiesForDomain(url);
    }

    public interface WebViewRefreshCallback {

        void onRefresh(WebView webView);
    }

    //WebViewClient就是帮助WebView处理各种通知、请求事件的。
    protected class MyWebViewClient extends WebViewClient {

        private String mRefererString = "";

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            ZLog.d(TAG + "shouldInterceptRequest url:" + url);
            if (BuildUtils.INSTANCE.getSDK_INT() < VERSION_CODES.LOLLIPOP) {
                WebResourceResponse res = interceptRequestResult(url);
                if (null != res) {
                    return res;
                }
            }
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            ZLog.d(TAG + "shouldInterceptRequest url:" + request.getUrl().toString());
            if (BuildUtils.INSTANCE.getSDK_INT() >= VERSION_CODES.LOLLIPOP) {
                String url = request.getUrl().toString();

                ///获取RequestHeader中的所有 key value
                Map<String, String> headerHashMap = request.getRequestHeaders();
                if (headerHashMap.containsKey(HEADER_NAME_REFERER)) {
                    mRefererString = headerHashMap.get(HEADER_NAME_REFERER);
                }

                WebResourceResponse res = interceptRequestResult(url);
                if (null != res) {
                    return res;
                }
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (shouldWebClientOverrideUrlLoading(url, mRefererString)) {
                return true;
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            onWebClientPageFinished();
        }

        @Override
        public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
            super.onPageStarted(webView, s, bitmap);
            onWebClientPageStarted();
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            ZLog.e(TAG, "onReceivedError: errorCode=" + errorCode + " description=" + description + " failingUrl="
                    + failingUrl);
            onWebClientReceivedError(errorCode);
            return;
        }

        @Override
        public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest,
                WebResourceError webResourceError) {
            ZLog.e(TAG, "onReceivedError: errorCode=" + webResourceError.getErrorCode() + " description="
                    + webResourceError.getDescription() + " failingUrl="
                    + webResourceRequest.getUrl().toString());
            onWebClientReceivedError(webResourceError.getErrorCode());
            return;
        }

        @Override
        public void onReceivedHttpError(WebView webView, WebResourceRequest webResourceRequest,
                WebResourceResponse webResourceResponse) {
            super.onReceivedHttpError(webView, webResourceRequest, webResourceResponse);
        }

        @Override
        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
            DialogUtils.INSTANCE.showConfirmDialog(
                    getContext(),
                    getResources().getString(com.bihe0832.android.model.res.R.string.dialog_title),
                    getResources().getString(com.bihe0832.android.model.res.R.string.com_bihe0832_web_ssl_error_message),
                    getResources().getString(com.bihe0832.android.model.res.R.string.dialog_button_ok),
                    getResources().getString(com.bihe0832.android.model.res.R.string.dialog_button_cancel),
                    new OnDialogListener() {
                        @Override
                        public void onPositiveClick() {
                            sslErrorHandler.proceed();
                        }

                        @Override
                        public void onNegativeClick() {
                            sslErrorHandler.cancel();
                        }

                        @Override
                        public void onCancel() {
                            onNegativeClick();
                        }
                    }

            );
        }
    }

    //WebChromeClient是辅助WebView处理Javascript的对话框，网站图标，网站title，加载进度等
    protected class MyWebChromeClient extends WebChromeClient {

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                FileChooserParams fileChooserParams) {
            mPicUploadCallback = filePathCallback;
            String[] fileTypes = new String[]{"*/*"};
            if (BuildUtils.INSTANCE.getSDK_INT() >= VERSION_CODES.LOLLIPOP) {
                fileTypes = fileChooserParams.getAcceptTypes();
            }
            openChooserActivity(fileTypes);
            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            ZLog.d(TAG, "onProgressChanged " + newProgress);
            mProgressBar.setProgress(newProgress);
            super.onProgressChanged(view, newProgress);
        }

        //获取Web页中的title用来设置自己界面中的title
        //当加载出错的时候，比如无网络，这时onReceiveTitle中获取的标题为 找不到该网页,
        //因此建议当触发onReceiveError时，不要使用获取到的title
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);

            mWebViewViewModel.setTitleString(title);
            WebViewLoggerFile.INSTANCE.log("BaseWebviewFragment title：" + title);
            WebViewLoggerFile.INSTANCE.log("BaseWebviewFragment mWebViewViewModel: " + mWebViewViewModel.hashCode());
        }


        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            return true;
        }

        //处理confirm弹出框
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
                JsPromptResult result) {
            ZLog.d(TAG, "onJsPrompt " + url);
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

        //处理prompt弹出框
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            ZLog.d(TAG, "onJsConfirm " + message);
            return super.onJsConfirm(view, url, message, result);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            ZLog.d(TAG, "onJsAlert " + message);
            result.confirm();
            return true;
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            ZLog.d(TAG,
                    "onConsoleMessage  From line " + consoleMessage.lineNumber() + " of " + consoleMessage.sourceId()
                            + " \n\t" + consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback callback) {
            if (mCustomView != null) {
                onHideCustomView();
                return;
            }

            mCustomView = view;
            mOriginalSystemUiVisibility = getActivity().getWindow().getDecorView().getSystemUiVisibility();
            mOriginalOrientation = getActivity().getRequestedOrientation();

            mCustomViewCallback = callback;

            FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
            decor.addView(mCustomView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            getActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE);
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        @Override
        public void onHideCustomView() {
            FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
            decor.removeView(mCustomView);
            mCustomView = null;

            getActivity().getWindow().getDecorView().setSystemUiVisibility(mOriginalSystemUiVisibility);
            getActivity().setRequestedOrientation(mOriginalOrientation);

            mCustomViewCallback.onCustomViewHidden();
            mCustomViewCallback = null;
        }
    }
}
