package com.bihe0832.android.common.webview.base;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bihe0832.android.common.webview.R;
import com.bihe0832.android.common.webview.log.WebviewLoggerFile;
import com.bihe0832.android.framework.router.RouterConstants;
import com.bihe0832.android.framework.ui.main.CommonActivity;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.ConvertUtils;
import com.bihe0832.android.lib.utils.intent.IntentUtils;
import com.tencent.smtt.sdk.QbSdk;

import java.net.URLDecoder;

public abstract class BaseWebviewActivity extends CommonActivity {


    protected abstract BaseWebviewFragment getWebViewFragment();

    protected abstract Class getWebViewFragmentClass();

    //标题栏
    private static final String TAG = "WebPageActivity";
    private WebViewViewModel mWebViewViewModel;
    private String mURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWebViewViewModel = new ViewModelProvider(this).get(WebViewViewModel.class);
        WebviewLoggerFile.INSTANCE.log("BaseWebviewActivity mWebViewViewModel: " + mWebViewViewModel.hashCode());
        ZLog.d(TAG + QbSdk.getTbsVersion(this));
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
                handleIntent(intent);
                ((BaseWebviewFragment) findFragment(getWebViewFragmentClass())).loadUrl(mURL, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handleIntent(Intent intent) {


        if (intent.hasExtra(RouterConstants.INTENT_EXTRA_KEY_WEB_REDIRECT_URL)) {
            String redirectURL = URLDecoder.decode(intent.getStringExtra(RouterConstants.INTENT_EXTRA_KEY_WEB_REDIRECT_URL));
            IntentUtils.openWebPage(redirectURL, this);
            finish();
        } else {
            mURL = parseURL(intent);
            WebviewLoggerFile.INSTANCE.log("handle intent:" + mURL);
        }
        if (intent.hasExtra(RouterConstants.INTENT_EXTRA_KEY_WEB_TITLE_STATUS)
                && ConvertUtils.parseInt(intent.getStringExtra(RouterConstants.INTENT_EXTRA_KEY_WEB_TITLE_STATUS),
                RouterConstants.INTENT_EXTRA_VALUE_WEB_TITLE_SHOW)
                == RouterConstants.INTENT_EXTRA_VALUE_WEB_TITLE_HIDE) {
            findViewById(R.id.common_toolbar).setVisibility(View.GONE);
        } else {
            initToolbar();
        }
    }

    protected String parseURL(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            return intent.getData().toString();
        } else if (intent.hasExtra(BaseWebviewFragment.INTENT_KEY_URL)) {
            return URLDecoder.decode(intent.getStringExtra(RouterConstants.INTENT_EXTRA_KEY_WEB_URL));
        } else {
            return "";
        }
    }

    protected String getURL() {
        return mURL;
    }

    protected void initToolbar() {
        initToolbar(R.id.common_toolbar, "", true);
        getMToolbar().setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mWebViewViewModel.getTitleLiveData().observe(BaseWebviewActivity.this, new Observer<String>() {
            @Override
            public void onChanged(final String s) {
                if (!s.equals("about:blank")) {
                    updateTitle(s);
                }
                WebviewLoggerFile.INSTANCE.log("mWebViewViewModel: " + hashCode() + "  title: " + s);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (TextUtils.isEmpty(mURL)) {
            ZLog.e(
                    TAG,
                    "  \n !!!========================================  \n \n \n !!! Webview: onResume, extra is good, but value is bad \n \n \n !!!========================================"
            );
            WebviewLoggerFile.INSTANCE.log(TAG + "  \n !!!========================================  \n \n \n !!! Webview: onResume, extra is good, but value is bad \n \n \n !!!========================================");
            finish();
            return;
        } else {
            WebviewLoggerFile.INSTANCE.log(TAG + " onResume:" + mURL);
            if (findFragment(getWebViewFragmentClass()) == null) {
                loadRootFragment(getWebViewFragment());
            }
        }
    }
}

