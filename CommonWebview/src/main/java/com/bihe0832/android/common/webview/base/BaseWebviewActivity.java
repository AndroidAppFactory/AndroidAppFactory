package com.bihe0832.android.common.webview.base;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
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
    protected String mURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWebViewViewModel = ViewModelProviders.of(this).get(WebViewViewModel.class);
        WebviewLoggerFile.INSTANCE.log("BaseWebviewActivity mWebViewViewModel: " + mWebViewViewModel.hashCode());
        ZLog.d(TAG + QbSdk.getTbsVersion(this));
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        try {
            handleIntent(intent);
            ((BaseWebviewFragment) findFragment(getWebViewFragmentClass())).loadUrl(mURL, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handleIntent(Intent intent) {

        if (intent.hasExtra(BaseWebviewFragment.INTENT_KEY_URL)) {
            mURL = URLDecoder.decode(intent.getStringExtra(RouterConstants.INTENT_EXTRA_KEY_WEB_URL));
        } else {
            if (intent.hasExtra(RouterConstants.INTENT_EXTRA_KEY_WEB_REDIRECT_URL)) {
                String redirectURL = URLDecoder
                        .decode(intent.getStringExtra(RouterConstants.INTENT_EXTRA_KEY_WEB_REDIRECT_URL));
                IntentUtils.openWebPage(redirectURL, this);
                finish();
            } else if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
                mURL = intent.getData().toString();
            } else {
                WebviewLoggerFile.INSTANCE.log("handle intent, but extra is bad");
            }
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
            public void onChanged(@Nullable final String s) {
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
            WebviewLoggerFile.INSTANCE.log(TAG + "onResume, extra is good, but value is bad");
            finish();
            return;
        } else {
            WebviewLoggerFile.INSTANCE.log(TAG + "onResume:" + mURL);
            if (findFragment(getWebViewFragmentClass()) == null) {
                loadRootFragment(R.id.common_fragment_content, getWebViewFragment());
            }
        }
    }
}

