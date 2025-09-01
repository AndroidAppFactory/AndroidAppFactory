package com.bihe0832.android.base.debug.webview;

import android.os.Bundle;
import android.widget.TextView;

import com.bihe0832.android.common.webview.CommonWebviewFragment;

/**
 * @author zixie code@bihe0832.com
 * Created on 2023/5/30.
 * Description: Description
 */
public class DebugWebViewWithNativeFragment extends CommonWebviewFragment {
    public static int HEIGHT = 50;
    private final TextView textView = null;
    private DebugJsBridgeProxy mBaseJsBridgeProxy = null;

    @Override
    protected void parseBundle(Bundle bundle, boolean isOnCreate) {
        super.parseBundle(bundle, isOnCreate);
        mIntentUrl = "file:///android_asset/webview_test.html";
    }

    @Override
    protected void actionBeforeLoadURL(String url) {
        super.actionBeforeLoadURL(url);
    }

    protected DebugJsBridgeProxy getJsBridgeProxy() {
        if (mBaseJsBridgeProxy == null) {
            mBaseJsBridgeProxy = new DebugJsBridgeProxy(getActivity(), mWebView);
        }
        return mBaseJsBridgeProxy;
    }
}
