package com.bihe0832.android.base.debug.webview;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bihe0832.android.base.debug.R;
import com.bihe0832.android.common.webview.CommonWebviewFragment;
import com.bihe0832.android.common.webview.base.BaseWebviewFragment;
import com.bihe0832.android.common.webview.log.MyBaseJsBridgeProxy;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.utils.os.DisplayUtil;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2023/5/30.
 * Description: Description
 */
public class DebugWebviewFragment extends CommonWebviewFragment {
    public static int HEIGHT = 50;
    private TextView textView = null;
    private DebugJsBridgeProxy mBaseJsBridgeProxy = null;

    @Override
    protected void parseBundle(Bundle bundle, boolean isOnCreate) {
        super.parseBundle(bundle, isOnCreate);
        mIntentUrl = "file:///android_asset/webview_test.html";
    }

    protected DebugJsBridgeProxy getJsBridgeProxy() {
        if (mBaseJsBridgeProxy == null) {
            mBaseJsBridgeProxy = new DebugJsBridgeProxy(mWebView, getActivity());
        }
        return mBaseJsBridgeProxy;
    }

    @Override
    protected WebViewClient getWebViewClient() {
        return new DebugWebViewClient(getJsBridgeProxy());
    }

    private class DebugWebViewClient extends BaseWebviewFragment.MyWebViewClient {

        public DebugWebViewClient(MyBaseJsBridgeProxy jsBridge) {
            super(jsBridge);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            ThreadManager.getInstance().runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    textView = getTextView(getContext());
                    mWebView.addView(textView);
                    mBaseJsBridgeProxy.setNativeView(textView);
                }
            });
        }
    }


    public static final TextView getTextView(Context context) {
        TextView textView = new TextView(context);
        textView.setBackgroundColor(context.getResources().getColor(R.color.bihe0832_common_toast_background_color));
        textView.setText((CharSequence) "Zixie AAF Webview Debug TextView2 ");
        textView.setGravity(Gravity.CENTER);
        textView.setVisibility(View.GONE);
        textView.setLayoutParams(new ViewGroup.MarginLayoutParams(-1, DisplayUtil.dip2px(context, (float) HEIGHT)));
        return textView;
    }
}
