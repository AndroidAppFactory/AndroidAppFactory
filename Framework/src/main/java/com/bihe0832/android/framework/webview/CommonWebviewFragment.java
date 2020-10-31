package com.bihe0832.android.framework.webview;


import android.net.Uri;
import android.os.Bundle;

import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.framework.constant.Constants;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.request.URLUtils;
import com.bihe0832.android.lib.webview.jsbridge.BaseJsBridgeProxy;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;

/**
 * Created by akihuang on 2019/7/4.
 */

public class CommonWebviewFragment extends BaseWebviewFragment {

    private static final String URL_USER_AGENT_VERSION = "ZixieVersion";
    private static final String URL_USER_AGENT_JS_BRIDGE_VERSION = "JSVersion";
    public static final String VERSION_NAME = "1.0.0";
    public static final int VERSION_CODE = 1;

    private static final String URL_PARAM_VERSION_NAME = URL_USER_AGENT_VERSION + "Name";
    private static final String URL_PARAM_VERSION_CODE = URL_USER_AGENT_VERSION + "Code";
    private static final String URL_PARAM_PLATFORM = "OSVersion";

    public static CommonWebviewFragment newInstance(String url) {
        CommonWebviewFragment fragment = new CommonWebviewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(INTENT_KEY_URL, url);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static CommonWebviewFragment newInstance(String url, boolean refreshable) {
        CommonWebviewFragment fragment = new CommonWebviewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(INTENT_KEY_URL, url);
        bundle.putBoolean(INTENT_KEY_REFRESH, refreshable);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected String getFinalURL(String url) {
        ZLog.d(TAG + url);
        StringBuilder builder = new StringBuilder();
        builder.append(URL_PARAM_VERSION_NAME).append(URLUtils.HTTP_REQ_ENTITY_MERGE).append(ZixieContext.INSTANCE.getVersionName());
        builder.append(URLUtils.HTTP_REQ_ENTITY_JOIN).append(URL_PARAM_VERSION_CODE).append(URLUtils.HTTP_REQ_ENTITY_MERGE).append(ZixieContext.INSTANCE.getVersionCode());
        String result = URLUtils.marge(url, builder.toString());
        ZLog.d(TAG + result);
        return result;
    }

    @Override
    protected String getUserAgentString() {
        return " " + URL_USER_AGENT_VERSION
                + "/" + ZixieContext.INSTANCE.getVersionName()
                + "/" + ZixieContext.INSTANCE.getVersionCode() + "/" + Constants.SYSTEM_CONSTANT + "/" +
                URL_USER_AGENT_JS_BRIDGE_VERSION + "/" + VERSION_NAME + "/" + VERSION_CODE;
    }

    @Override
    protected BaseJsBridgeProxy getJsBridgeProxy() {
        return new BaseJsBridgeProxy(mWebView, getActivity()) {
        };
    }

    @Override
    protected boolean loadUseIntent(String url) {
        return false;
    }

    @Override
    protected void actionBeforeLoadURL(String url) {
        Uri uri = Uri.parse(url);
        if (uri.getHost() == null) {
            return;
        }
        String host = uri.getHost().toLowerCase();
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setCookie(url, getSetCookieString(URL_PARAM_VERSION_NAME, ZixieContext.INSTANCE.getVersionName(), host) + "; MAX-Age=" + 60 * 60 * 24);
        cookieManager.setCookie(url, getSetCookieString(URL_PARAM_VERSION_CODE, String.valueOf(ZixieContext.INSTANCE.getVersionCode()), host) + "; MAX-Age=" + 60 * 60 * 24);
        cookieManager.setCookie(url, getSetCookieString(URL_PARAM_PLATFORM, Constants.SYSTEM_CONSTANT, host) + "; MAX-Age=" + 60 * 60 * 24);
        CookieSyncManager.getInstance().sync();
    }
}
