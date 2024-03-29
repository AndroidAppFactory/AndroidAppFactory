package com.bihe0832.android.common.webview;


import android.os.Bundle;
import com.bihe0832.android.common.webview.nativeimpl.NativeWebViewFragment;
import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.framework.constant.Constants;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.request.URLUtils;
import java.util.HashMap;

public class CommonWebviewFragment extends NativeWebViewFragment {


    public static final String VERSION_NAME = "1.0.0";
    public static final int VERSION_CODE = 1;

    private static final String URL_PARAM_VERSION_NAME = Constants.URL_USER_AGENT_VERSION + "Name";
    private static final String URL_PARAM_VERSION_CODE = Constants.URL_USER_AGENT_VERSION + "Code";
    private static final String URL_PARAM_CLIENT_TIME = "ClientTime";
    private static final String URL_PARAM_PLATFORM = "OSVersion";

    public static CommonWebviewFragment newInstance(String url) {
        CommonWebviewFragment fragment = new CommonWebviewFragment();
        Bundle bundle = getWebviewDataBundle(url, "");
        fragment.setArguments(bundle);
        return fragment;
    }

    public static CommonWebviewFragment newInstance(String url, boolean refreshable) {
        CommonWebviewFragment fragment = new CommonWebviewFragment();
        Bundle bundle = getWebviewDataBundle(url, "");
        bundle.putBoolean(INTENT_KEY_REFRESH, refreshable);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    protected String getFinalURL(String url) {
        ZLog.d(TAG + url);
        HashMap<String, String> para = new HashMap<>();
        para.put(URL_PARAM_VERSION_NAME, ZixieContext.INSTANCE.getVersionName());
        para.put(URL_PARAM_VERSION_CODE, String.valueOf(ZixieContext.INSTANCE.getVersionCode()));
        para.put(URL_PARAM_CLIENT_TIME, String.valueOf(System.currentTimeMillis()));
        para.put(URL_PARAM_PLATFORM, Constants.SYSTEM_CONSTANT);
        String result = URLUtils.marge(url, para);
        ZLog.d(TAG + result);
        return result;
    }

    @Override
    protected String getUserAgentString() {
        return " " + Constants.URL_USER_AGENT_VERSION
                + "/" + ZixieContext.INSTANCE.getVersionName()
                + "/" + ZixieContext.INSTANCE.getVersionCode() + "/" + Constants.SYSTEM_CONSTANT + "/" +
                Constants.URL_USER_AGENT_JS_BRIDGE_VERSION + "/" + VERSION_NAME + "/" + VERSION_CODE;
    }

    @Override
    protected boolean loadUseIntent(String url) {
        return false;
    }

    @Override
    protected void actionBeforeLoadURL(String url) {
        setCookie(url, URL_PARAM_VERSION_NAME, ZixieContext.INSTANCE.getVersionName());
        setCookie(url, URL_PARAM_VERSION_CODE, String.valueOf(ZixieContext.INSTANCE.getVersionCode()));
        setCookie(url, URL_PARAM_PLATFORM, Constants.SYSTEM_CONSTANT);
        syncCookie();
    }
}
