package com.bihe0832.android.common.webview.nativeimpl;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build.VERSION_CODES;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import androidx.annotation.RequiresApi;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.bihe0832.android.common.webview.R;
import com.bihe0832.android.common.webview.base.BaseWebViewFragment;
import com.bihe0832.android.common.webview.core.WebViewLoggerFile;
import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.http.common.core.BaseConnection;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener;
import com.bihe0832.android.lib.ui.dialog.tools.DialogUtils;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class NativeWebViewFragment extends BaseWebViewFragment {

    public NativeWebView mWebView;
    protected WebViewRefreshCallback mRefreshCallback = null;

    public void setOnWebViewRefreshCallback(WebViewRefreshCallback callback) {
        mRefreshCallback = callback;
    }

    protected WebViewClient getWebViewClient() {
        return new NativeWebViewFragment.MyWebViewClient();
    }

    protected WebChromeClient getWebChromeClient() {
        return new NativeWebViewFragment.MyWebChromeClient();
    }

    protected void createWebView() {
        mWebView = new NativeWebView(getContext(), null);
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
        mWebView.setOnScrollChangedCallback(new NativeWebView.OnScrollChangedCallback() {
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
        NativeCookieManager.INSTANCE.init(mWebView);
    }

    @Override
    protected void loadFinalURL(String finalURL, String data) {
        mWebView.doActionBeforeLoadURL();
        if (TextUtils.isEmpty(data)) {
            mWebView.loadUrl(finalURL);
        } else {
            mWebView.postUrl(finalURL, data.getBytes());
        }
    }

    @Override
    protected void onJSBridgeResume() {
        super.onJSBridgeResume();
        if (null != mWebView) {
            mWebView.resumeTimers();
        }
    }

    @Override
    protected void onJSBridgePause() {
        super.onJSBridgePause();
        if (null != mWebView) {
            mWebView.pauseTimers();
        }
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

    protected NativeJsBridgeProxy getJsBridgeProxy() {
        return new NativeJsBridgeProxy(getActivity(), mWebView);
    }

    @Override
    protected void destroyWebView() {
        if (mWebView != null) {
            mWebView.stopLoading();
            mWebView.clearHistory();
            mWebView.destroy();
        }
    }

    @Override
    public void setCookie(String url, String name, String value) {
        NativeCookieManager.INSTANCE.setCookie(url, name, value);
    }

    @Override
    public void syncCookie() {
        NativeCookieManager.INSTANCE.syncCookie();
    }

    @Override
    public void removeCookiesForDomain(String url) {
        NativeCookieManager.INSTANCE.removeCookiesForDomain(url);
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
            onWebClientReceivedError(errorCode);
        }

        @RequiresApi(api = VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (BuildUtils.INSTANCE.getSDK_INT() >= VERSION_CODES.M) {
                onWebClientReceivedError(error.getErrorCode());
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            DialogUtils.INSTANCE.showConfirmDialog(getContext(), getResources().getString(com.bihe0832.android.model.res.R.string.dialog_title),
                    getResources().getString(com.bihe0832.android.model.res.R.string.com_bihe0832_web_ssl_error_message),
                    getResources().getString(com.bihe0832.android.model.res.R.string.dialog_button_ok),
                    getResources().getString(com.bihe0832.android.model.res.R.string.dialog_button_cancel), new OnDialogListener() {
                        @Override
                        public void onPositiveClick() {
                            handler.proceed();
                        }

                        @Override
                        public void onNegativeClick() {
                            handler.cancel();
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
    }
}
