package com.bihe0832.android.base.debug.webview;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bihe0832.android.base.debug.R;
import com.bihe0832.android.common.webview.CommonWebviewFragment;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.utils.ConvertUtils;
import com.bihe0832.android.lib.utils.os.DisplayUtil;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
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

    @Override
    protected void parseBundle(Bundle bundle, boolean isOnCreate) {
        super.parseBundle(bundle, isOnCreate);
        mIntentUrl = "file:///android_asset/webview_test.html";
    }

    @Override
    protected void actionOnPageFinished(WebView view, String url) {
        super.actionOnPageFinished(view, url);
        mWebView.evaluateJavascript("javaScript:getNativeViewPosition()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                ThreadManager.getInstance().runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        AbsoluteLayout.LayoutParams params = (AbsoluteLayout.LayoutParams) textView.getLayoutParams();
                        params.y = DisplayUtil.dip2pxWithDefaultDensity(getContext(), ConvertUtils.parseFloat(value, -1));
                        textView.setLayoutParams(params);
                        mWebView.loadUrl("javaScript:setNativeViewHeight(" + HEIGHT + ")");
                    }
                });
            }
        });
    }

    @Override
    protected void initView(@NonNull View view) {
        super.initView(view);
        this.mRefreshable = false;
    }

    @Override
    protected void initWebview(View view, WebViewClient webViewClient, WebChromeClient webChromeClient) {
        super.initWebview(view, webViewClient, webChromeClient);
        textView = getTextView(getContext());
        mWebView.addView(textView);
    }

    public static final TextView getTextView(Context context) {
        TextView textView = new TextView(context);
        textView.setBackgroundColor(context.getResources().getColor(R.color.bihe0832_common_toast_background_color));
        textView.setText((CharSequence) "Zixie AAF Webview Debug TextView2 ");
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new ViewGroup.MarginLayoutParams(-1, DisplayUtil.dip2px(context, (float) HEIGHT)));
        return textView;
    }
}
