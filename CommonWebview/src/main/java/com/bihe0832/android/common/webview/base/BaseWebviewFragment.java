package com.bihe0832.android.common.webview.base;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bihe0832.android.common.webview.R;
import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode;
import com.bihe0832.android.framework.router.RouterConstants;
import com.bihe0832.android.framework.ui.BaseFragment;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.request.URLUtils;
import com.bihe0832.android.lib.utils.intent.IntentUtils;
import com.bihe0832.android.lib.webview.BaseWebView;
import com.bihe0832.android.lib.webview.jsbridge.BaseJsBridgeProxy;
import com.bihe0832.android.lib.webview.jsbridge.JsBridge;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.MimeTypeMap;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseWebviewFragment extends BaseFragment implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String INTENT_KEY_URL = RouterConstants.INTENT_EXTRA_KEY_WEB_URL;
    public static final String INTENT_KEY_REFRESH = "refresh";
    public static final String INTENT_KEY_DATA = "WebviewFragment.data";
    public static final String INTENT_KEY_THIRD_PART = "http://localhost";
    public static final String KEY_WX_PAY_PART = "https://wx.tenpay.com";

    private HashMap<String, String> globalLocalRes = new HashMap<String, String>() {{
        put("https://cdn.bihe0832.com/js/jsbridge.js", "web/js/jsbridge.min.js");
    }};

    // 追加业务参数
    protected abstract String getFinalURL(String url);

    //在最后loadURL之前执行操作
    protected abstract void actionBeforeLoadURL(String url);

    //是否通过intent的方式打开当前的URL
    protected abstract boolean loadUseIntent(String url);

    //获取业务内置浏览器的agent头
    protected abstract String getUserAgentString();

    protected abstract BaseJsBridgeProxy getJsBridgeProxy();

    protected static final String TAG = "WebPageFragment -> :";

    private View mCustomView;
    public BaseWebView mWebView;
    private IX5WebChromeClient.CustomViewCallback mCustomViewCallback;
    private ConstraintLayout mErrorPage;
    private SwipeRefreshLayout mNormalPage;
    private TextView mRetry;
    private TextView mRedirect;
    private ViewGroup mViewParent;
    protected ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeLayout;
    private Boolean isLoadSuccess = true;
    private TextView mErrorUrl;

    private WebViewViewModel mWebViewViewModel;
    protected BaseJsBridgeProxy mJSBridgeProxy = null;
    private ValueCallback<Uri[]> mPicUploadCallback;

    protected String mIntentUrl;
    protected String mPostData;
    protected boolean mRefreshable;
    private int mOriginalSystemUiVisibility;
    private int mOriginalOrientation;

    private MutableLiveData<Integer> _webViewScrollTopLiveData = new MutableLiveData<>();
    private LiveData<Integer> mWebViewScrollTopLiveData = _webViewScrollTopLiveData;

    private long lastResumeTime = 0L;
    private long lastPauseTime = 0L;


    public LiveData<Integer> getWebViewScrollTopLiveData() {
        return mWebViewScrollTopLiveData;
    }

    private WebViewRefreshCallback mRefreshCallback = null;

    public void setOnWebViewRefreshCallback(WebViewRefreshCallback callback) {
        mRefreshCallback = callback;
    }

    public HashMap<String, String> getGlobalLocalRes() {
        return globalLocalRes;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mIntentUrl = URLDecoder.decode(bundle.getString(INTENT_KEY_URL));
            mRefreshable = bundle.getBoolean(INTENT_KEY_REFRESH, false);
            mPostData = bundle.getString(INTENT_KEY_DATA);
        }
        mWebViewViewModel = ViewModelProviders.of(getActivity()).get(WebViewViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.common_zixie_fragment_webview, container, false);
        mViewParent = (ViewGroup) view.findViewById(R.id.app_webview_x5webView);
        mRetry = (TextView) view.findViewById(R.id.web_retry);
        mRedirect = (TextView) view.findViewById(R.id.web_native_browser);
        mErrorUrl = (TextView) view.findViewById(R.id.web_error_url);
        mRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl(mIntentUrl, mPostData);
            }
        });
        mRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtils.openWebPage(mIntentUrl, getContext());
            }
        });
        getActivity().getWindow().setFormat(PixelFormat.TRANSLUCENT);
        initWebview(view);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ZixieActivityRequestCode.FILE_CHOOSER) {
            if (data != null || resultCode == RESULT_OK) {
                Uri[] uris = new Uri[1];
                uris[0] = data.getData();
                if (mPicUploadCallback != null) {
                    mPicUploadCallback.onReceiveValue(uris);
                    mPicUploadCallback = null;
                }
            }
        }
    }

    private void initWebview(View view) {
        mWebView = new BaseWebView(getContext(), null);
        mNormalPage = (SwipeRefreshLayout) view.findViewById(R.id.app_webview_swipe_container);
        mViewParent.addView(mWebView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        mErrorPage = (ConstraintLayout) view.findViewById(R.id.error_page);
        mProgressBar = (ProgressBar) view.findViewById(R.id.app_webview_progressbar);
        mProgressBar.setMax(100);

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.app_webview_swipe_container);
        mSwipeLayout.setEnabled(mRefreshable);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                mSwipeLayout.setRefreshing(false);
                if (mJSBridgeProxy != null && mJSBridgeProxy.canPullToRefresh()) {
                    if (mRefreshCallback != null) {
                        mRefreshCallback.onRefresh(mWebView);
                    } else {
                        mWebView.reload();
                    }
                    mSwipeLayout.setEnabled(true);
                }
            }
        });

        mWebView.setOnScrollChangedCallback(new BaseWebView.OnScrollChangedCallback() {
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

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            if (!ZixieContext.INSTANCE.isOfficial()) {
                mWebView.setWebContentsDebuggingEnabled(true);
            } else {
                mWebView.setWebContentsDebuggingEnabled(false);
            }
        }

        mWebView.setWebViewClient(new BaseWebviewFragment.MyWebViewClient(getJsBridgeProxy()));
        mWebView.setWebChromeClient(new BaseWebviewFragment.MyWebChromeClient());
        setUserAgentSupport(mWebView.getSettings());
        synUserInfoInCookies();
        long time = System.currentTimeMillis();
        if (mIntentUrl == null) {
            mIntentUrl = "about:blank;";
        } else {
            loadUrl(mIntentUrl, mPostData);
        }
        ZLog.d(TAG, "time-cost cost time: " + (System.currentTimeMillis() - time));
    }

    public void loadUrl(String url, String data) {
        isLoadSuccess = true;
        mIntentUrl = url;
        mPostData = data;
        actionBeforeLoadURL(url);
        if (TextUtils.isEmpty(data)) {
            mWebView.loadUrl(getFinalURL(url));
        } else {
            mWebView.postUrl(getFinalURL(url), data.getBytes());
        }
    }

    protected WebResourceResponse interceptRequestResult(String url) {

        if (null != getGlobalLocalRes() && getGlobalLocalRes().containsKey(url)) {
            try {
                String type = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
                return new WebResourceResponse(type, "utf-8",
                        getContext().getAssets().open(getGlobalLocalRes().get(url)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //WebViewClient就是帮助WebView处理各种通知、请求事件的。
    class MyWebViewClient extends WebViewClient {

        public BaseJsBridgeProxy mJSBridgeProxy = null;

        public MyWebViewClient(BaseJsBridgeProxy jsBridge) {
            BaseWebviewFragment.this.mJSBridgeProxy = jsBridge;
            this.mJSBridgeProxy = jsBridge;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            ZLog.d(TAG + "shouldInterceptRequest url:" + url);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String url = request.getUrl().toString();
                WebResourceResponse res = interceptRequestResult(url);
                if (null != res) {
                    return res;
                }
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            ZLog.d(TAG + "shouldOverrideUrlLoading url:" + url);
            if (TextUtils.isEmpty(url)) {
                return false;
            }
            if (url.startsWith(INTENT_KEY_THIRD_PART)) {
                String value = URLUtils.getValueByName(url, "value");
                try {
                    return processOverrideUrlLoading(URLDecoder.decode(value, "UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                return processOverrideUrlLoading(url);
            }
        }

        private boolean processOverrideUrlLoading(String url) {
            if (url.startsWith("http") || url.startsWith("https")) {
                if (loadUseIntent(url)) {
                    IntentUtils.jumpToOtherApp(url, getContext());
                    return true;
                } else {
                    if (!url.startsWith(KEY_WX_PAY_PART)) {
                        loadUrl(url, "");
                    } else {
                        Map<String, String> headers = new HashMap<>();
                        mWebView.loadUrl(url, headers);
                    }
                    return true;
                }
            } else if (url.startsWith(JsBridge.JS_BRIDGE_SCHEME)) {
                if (mJSBridgeProxy != null) {
                    mJSBridgeProxy.invoke(url);
                }
                return true;
            } else if (url.equals("about:blank;") || url.equals("about:blank")) {
                // 3.0及以下的webview调用jsb时会调用同时call起的空白页面，将这个页面屏蔽掉不出来
                return Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
            } else {
                IntentUtils.jumpToOtherApp(url, getContext());
                return true;
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (isLoadSuccess) {
                mNormalPage.setVisibility(View.VISIBLE);
                mErrorPage.setVisibility(View.GONE);
            } else {
                mNormalPage.setVisibility(View.GONE);
                mErrorPage.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
            super.onPageStarted(webView, s, bitmap);
            mNormalPage.setVisibility(View.VISIBLE);
            mErrorPage.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            isLoadSuccess = false;
            mErrorUrl.setText(mIntentUrl);
            return;
        }

        @Override
        public void onReceivedSslError(WebView view,
                SslErrorHandler handler, SslError error) {
            handler.proceed();// 接受所有网站的证书
        }
    }

    //WebChromeClient是辅助WebView处理Javascript的对话框，网站图标，网站title，加载进度等
    class MyWebChromeClient extends WebChromeClient {

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                FileChooserParams fileChooserParams) {
            BaseWebviewFragment.this.mPicUploadCallback = filePathCallback;
            openImageChooserActivity();
            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            ZLog.d(TAG, "onProgressChanged " + newProgress);
            mProgressBar.setProgress(newProgress);
            if (newProgress == 100) {
                mProgressBar.setVisibility(View.GONE);
            } else {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            if (newProgress == 100) {
                //隐藏进度条
                mSwipeLayout.setRefreshing(false);
            } else {
                if (!mSwipeLayout.isRefreshing()) {
                    mSwipeLayout.setRefreshing(true);
                }
            }

            super.onProgressChanged(view, newProgress);
        }

        //获取Web页中的title用来设置自己界面中的title
        //当加载出错的时候，比如无网络，这时onReceiveTitle中获取的标题为 找不到该网页,
        //因此建议当触发onReceiveError时，不要使用获取到的title
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);

            mWebViewViewModel.setTitleString(title);
            ZLog.d(TAG, "mWebViewViewModel: " + mWebViewViewModel.hashCode());
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
            decor.addView(mCustomView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            getActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE);
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        @Override
        public void onHideCustomView() {
            FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
            decor.removeView(mCustomView);
            mCustomView = null;

            getActivity().getWindow().getDecorView()
                    .setSystemUiVisibility(mOriginalSystemUiVisibility);
            getActivity().setRequestedOrientation(mOriginalOrientation);

            mCustomViewCallback.onCustomViewHidden();
            mCustomViewCallback = null;
        }
    }

    private void openImageChooserActivity() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Image Chooser"), ZixieActivityRequestCode.FILE_CHOOSER);
    }

    private void setUserAgentSupport(WebSettings webSettings) {
        String userAgent = webSettings.getUserAgentString();
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
        webSettings.setUserAgentString(userAgent);
    }

    private void synUserInfoInCookies() {
        if (null == mWebView) {
            return;
        }
        CookieSyncManager.createInstance(mWebView.getContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(mWebView, true);
        }
        syncCookie();
    }

    public static void removeCookie(Context context) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        syncCookie();
    }

    public static void syncCookie() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager.getInstance().sync();
        }
    }

    @Override
    public void onResume() {
        ZLog.d(TAG, "onResume ");
        super.onResume();
        onJSBridgeResume();

    }

    @Override
    public void onPause() {
        ZLog.d(TAG, "onPause ");
        super.onPause();
        onJSBridgePause();
    }

    private void onJSBridgeResume() {
        if (null != mJSBridgeProxy) {
            ZLog.d(TAG, "onJSBridgeResume");
            if (System.currentTimeMillis() - lastResumeTime < 5 * 1000) {
                ZLog.d(TAG, "onJSBridgeResume to quick");
                return;
            }
            lastResumeTime = System.currentTimeMillis();
            lastPauseTime = 0;
            mJSBridgeProxy.onResume();
        }
    }

    private void onJSBridgePause() {
        if (null != mJSBridgeProxy) {
            ZLog.d(TAG, "onJSBridgePause");
            if (System.currentTimeMillis() - lastPauseTime < 5 * 1000) {
                ZLog.d(TAG, "onJSBridgePause to quick");
                return;
            }
            lastPauseTime = System.currentTimeMillis();
            lastResumeTime = 0;
            mJSBridgeProxy.onPause();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        ZLog.d(TAG, "setUserVisibleHint " + isVisibleToUser);
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            onJSBridgeResume();
        } else {
            onJSBridgePause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.stopLoading();
            mWebView.clearHistory();
            mWebView.destroy();
        }
        if (Build.VERSION.SDK_INT < 16) {
            try {
                Field field = WebView.class.getDeclaredField("mWebViewCore");
                field = field.getType().getDeclaredField("mBrowserFrame");
                field = field.getType().getDeclaredField("sConfigCallback");
                field.setAccessible(true);
                field.set(null, null);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Field sConfigCallback = Class.forName("android.webkit.BrowserFrame")
                        .getDeclaredField("sConfigCallback");
                if (sConfigCallback != null) {
                    sConfigCallback.setAccessible(true);
                    sConfigCallback.set(null, null);
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onBackPressedSupport() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        } else {
            return false;
        }
    }

    public interface WebViewRefreshCallback {

        void onRefresh(WebView webView);
    }

    public static String getCookieString(String name, String value, String domain) {
        return getCookieString(name, value, "/", domain);
    }

    public static String getCookieString(String name, String value, String path, String domain) {
        String v = name + "=" + value;
        if (domain != null) {
            v += "; path=" + path;
            v += "; domain=" + domain;
        }
        return v;
    }
}
