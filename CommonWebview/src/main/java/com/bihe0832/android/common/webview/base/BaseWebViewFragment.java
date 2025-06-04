package com.bihe0832.android.common.webview.base;


import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.bihe0832.android.common.webview.R;
import com.bihe0832.android.common.webview.core.WebViewLoggerFile;
import com.bihe0832.android.common.webview.core.WebViewViewModel;
import com.bihe0832.android.framework.constant.ZixieActivityRequestCode;
import com.bihe0832.android.framework.router.RouterAction;
import com.bihe0832.android.framework.router.RouterConstants;
import com.bihe0832.android.framework.ui.BaseFragment;
import com.bihe0832.android.lib.file.mimetype.FileMimeTypes;
import com.bihe0832.android.lib.http.common.core.BaseConnection;
import com.bihe0832.android.lib.jsbridge.BaseJsBridge;
import com.bihe0832.android.lib.jsbridge.BaseJsBridgeProxy;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.request.URLUtils;
import com.bihe0832.android.lib.utils.intent.IntentUtils;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.HashMap;

public abstract class BaseWebViewFragment extends BaseFragment implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String INTENT_KEY_URL = RouterConstants.INTENT_EXTRA_KEY_WEB_URL;
    public static final String INTENT_KEY_REFRESH = "refresh";
    public static final String INTENT_KEY_DATA = "WebviewFragment.data";
    public static final String INTENT_KEY_THIRD_PART_HTTP = "http://localhost";
    public static final String INTENT_KEY_THIRD_PART_HTTPS = "https://localhost";
    public static final String TAG = "WebPageFragment -> :";
    protected static final String HEADER_NAME_REFERER = "Referer";
    protected ConstraintLayout mErrorPage;
    protected SwipeRefreshLayout mNormalPage;
    protected ProgressBar mProgressBar;
    protected SwipeRefreshLayout mSwipeLayout;
    protected Boolean isLoadSuccess = true;
    protected TextView mErrorUrl;
    protected WebViewViewModel mWebViewViewModel;
    protected BaseJsBridgeProxy mJSBridgeProxy = null;
    protected ValueCallback<Uri[]> mPicUploadCallback;
    protected String mIntentUrl;
    protected String mPostData;
    protected boolean mRefreshable = false;
    protected MutableLiveData<Integer> _webViewScrollTopLiveData = new MutableLiveData<>();
    private HashMap<String, String> globalLocalRes = new HashMap<String, String>() {{
        put("https://cdn.bihe0832.com/js/jsbridge.js", "web/js/jsbridge.min.new.js");
    }};
    private TextView mRetry;
    private TextView mRedirect;
    private ViewGroup mViewParent;
    private LiveData<Integer> mWebViewScrollTopLiveData = _webViewScrollTopLiveData;


    public static Bundle getWebviewDataBundle(String url, String data) {
        Bundle bundle = new Bundle();
        bundle.putString(INTENT_KEY_URL, url);
        if (!TextUtils.isEmpty(data)) {
            bundle.putString(INTENT_KEY_DATA, data);
        }
        return bundle;
    }

    // 追加业务参数
    protected abstract String getFinalURL(String url);

    //在最后loadURL之前执行操作
    protected abstract void actionBeforeLoadURL(String url);

    //是否通过intent的方式打开当前的URL
    protected abstract boolean loadUseIntent(String url);

    //获取业务内置浏览器的agent头
    protected abstract String getUserAgentString();

    protected abstract void createWebView();

    protected abstract void destroyWebView();

    protected abstract void loadFinalURL(String url, String data);

    protected abstract void addWebViewToLayout(ViewGroup mViewParent);

    protected abstract void initRefreshAndScrollChangedCallback();

    protected abstract void initWebContentsDebuggingEnabled();

    protected abstract void initWebViewClient();

    protected abstract void initWebChromeClient();

    protected abstract void initUserAgentSupport();

    protected abstract void initCookieSupport();

    protected abstract boolean processRefererString(String url, String mRefererString);

    protected abstract BaseJsBridgeProxy getJsBridgeProxy();

    public LiveData<Integer> getWebViewScrollTopLiveData() {
        return mWebViewScrollTopLiveData;
    }

    public HashMap<String, String> getGlobalLocalRes() {
        return globalLocalRes;
    }

    //在页面加载结束之后执行操作
    protected void onWebClientPageFinished() {
        if (isLoadSuccess) {
            mNormalPage.setVisibility(View.VISIBLE);
            mErrorPage.setVisibility(View.GONE);
        } else {
            mNormalPage.setVisibility(View.GONE);
            mErrorPage.setVisibility(View.VISIBLE);
        }
        mProgressBar.setVisibility(View.GONE);
        mSwipeLayout.setRefreshing(false);
    }

    protected void onWebClientPageStarted() {
        mProgressBar.setVisibility(View.VISIBLE);
        mNormalPage.setVisibility(View.VISIBLE);
        mErrorPage.setVisibility(View.GONE);
    }

    protected void onWebClientReceivedError(int errorCode) {
        ZLog.e(TAG, "onWebClientReceivedError");
        if (errorCode == WebViewClient.ERROR_CONNECT) {
            // 处理网络连接错误
            isLoadSuccess = false;
            mErrorUrl.setText(mIntentUrl);
        } else {
            // 处理其他类型的错误
        }
    }

    @Override
    protected void initView(@NonNull View view) {
        super.initView(view);
        mWebViewViewModel = new ViewModelProvider(getActivity()).get(WebViewViewModel.class);
        createWebView();
        mViewParent = (ViewGroup) view.findViewById(R.id.app_webview);
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
                IntentUtils.openWebPage(getContext(), mIntentUrl);
            }
        });
        getActivity().getWindow().setFormat(PixelFormat.TRANSLUCENT);
        initWebview(view);
    }


    @Override
    protected void parseBundle(Bundle bundle, boolean isOnCreate) {
        mIntentUrl = bundle.getString(INTENT_KEY_URL);
        mRefreshable = bundle.getBoolean(INTENT_KEY_REFRESH, false);
        mPostData = bundle.getString(INTENT_KEY_DATA);
        if (!isOnCreate) {
            loadUrl(mIntentUrl, mPostData);
        }
    }

    @Override
    protected int getLayoutID() {
        return R.layout.common_zixie_fragment_webview;
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
            } else {
                if (mPicUploadCallback != null) {
                    mPicUploadCallback.onReceiveValue(null);
                }
            }
        }
    }

    public boolean shouldWebClientOverrideUrlLoading(String url, String mRefererString) {
        WebViewLoggerFile.INSTANCE.log(TAG + "shouldOverrideUrlLoading url:" + url);
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        if (url.startsWith(INTENT_KEY_THIRD_PART_HTTP) || url.startsWith(INTENT_KEY_THIRD_PART_HTTPS)) {
            String value = URLUtils.getValueByName(url, "value");
            try {
                return processWebClientOverrideUrlLoading(
                        URLDecoder.decode(value, BaseConnection.HTTP_REQ_VALUE_CHARSET_UTF8), "");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return processWebClientOverrideUrlLoading(url, mRefererString);
        }
    }


    protected boolean processWebClientOverrideUrlLoading(String url, String mRefererString) {
        if (url.startsWith(BaseJsBridge.JS_BRIDGE_SCHEME)) {
            if (mJSBridgeProxy != null) {
                mJSBridgeProxy.invoke(url);
                ZLog.e(TAG, "new JSBridge invoke:" + url);
            } else {
                ZLog.e(TAG, "new mJSBridgeProxy is null");
            }
            return true;
        } else if (url.startsWith(RouterAction.INSTANCE.getSCHEME())) {
            RouterAction.INSTANCE.openFinalURL(url, Intent.FLAG_ACTIVITY_NEW_TASK);
            return true;
        } else if (url.equals("about:blank;") || url.equals("about:blank")) {
            // 3.0及以下的webview调用jsb时会调用同时call起的空白页面，将这个页面屏蔽掉不出来
            return BuildUtils.INSTANCE.getSDK_INT() < VERSION_CODES.HONEYCOMB;
        } else if (url.startsWith("http") || url.startsWith("https")) {
            if (FileMimeTypes.INSTANCE.isApkFile(url) || FileMimeTypes.INSTANCE.isArchive(url)) {
                return jumpToOtherApp(url, getActivity());
            } else if (loadUseIntent(url)) {
                return jumpToOtherApp(url, getActivity());
            } else {
                return processRefererString(url, mRefererString);
            }
        } else {
            return jumpToOtherApp(url, getActivity());
        }
    }

    protected boolean jumpToOtherApp(String url, Context context) {
        ZLog.e(TAG, "jumpToOtherApp url:" + url);
        // TODO 兼容主流的应用schema打开方式，一般是call schema的同时，会跳转到下载页面，如果返回false就会报错
        return IntentUtils.startIntent(context, new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    protected void initWebview(View view) {
        mNormalPage = (SwipeRefreshLayout) view.findViewById(R.id.app_webview_swipe_container);
        addWebViewToLayout(mViewParent);
        mErrorPage = (ConstraintLayout) view.findViewById(R.id.error_page);
        mProgressBar = (ProgressBar) view.findViewById(R.id.app_webview_progressbar);
        mProgressBar.setMax(100);
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.app_webview_swipe_container);
        mSwipeLayout.setEnabled(mRefreshable);
        initRefreshAndScrollChangedCallback();
        initWebContentsDebuggingEnabled();
        mJSBridgeProxy = getJsBridgeProxy();
        initWebChromeClient();
        initWebViewClient();
        initUserAgentSupport();
        initCookieSupport();
        long time = System.currentTimeMillis();
        if (mIntentUrl == null) {
            mIntentUrl = "about:blank;";
        } else {
            loadUrl(mIntentUrl, mPostData);
        }
        ZLog.d(TAG, "mIntentUrl：" + mIntentUrl);
        ZLog.d(TAG, "time-cost cost time: " + (System.currentTimeMillis() - time));
    }

    public void loadUrl(String url, String data) {
        isLoadSuccess = true;
        mIntentUrl = url;
        mPostData = data;
        String finalURL = getFinalURL(url);
        actionBeforeLoadURL(finalURL);
        loadFinalURL(finalURL, data);
    }


    protected void openChooserActivity(String[] fileTypes) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(String.join(",", fileTypes));
        intent.putExtra(Intent.EXTRA_MIME_TYPES, fileTypes);
        startActivityForResult(Intent.createChooser(intent, "Image Chooser"), ZixieActivityRequestCode.FILE_CHOOSER);
    }

    protected void onJSBridgeResume() {
        if (null != mJSBridgeProxy) {
            ZLog.d(TAG, "onJSBridgeResume");
            mJSBridgeProxy.onResume();
        }
    }

    protected void onJSBridgePause() {
        if (null != mJSBridgeProxy) {
            ZLog.d(TAG, "onJSBridgePause");
            mJSBridgeProxy.onPause();
        }
    }

    @Override
    public final void setUserVisibleHint(boolean isVisibleToUser, boolean hasCreateView) {
        super.setUserVisibleHint(isVisibleToUser, hasCreateView);
        if (isVisibleToUser) {
            onJSBridgeResume();
        } else {
            onJSBridgePause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyWebView();
        if (BuildUtils.INSTANCE.getSDK_INT() < VERSION_CODES.JELLY_BEAN) {
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
        if (mJSBridgeProxy.canGoBack()) {
            mJSBridgeProxy.goBack();
            return true;
        } else {
            return false;
        }
    }

    public abstract void setCookie(String url, String name, String value);

    public abstract void syncCookie();

    public abstract void removeCookiesForDomain(String url);
}
