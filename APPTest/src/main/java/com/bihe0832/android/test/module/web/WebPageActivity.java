package com.bihe0832.android.test.module.web;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.bihe0832.android.framework.base.BaseActivity;
import com.bihe0832.android.framework.webview.BaseWebviewFragment;
import com.bihe0832.android.framework.webview.WebViewViewModel;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.test.R;
import com.tencent.smtt.sdk.QbSdk;

public class WebPageActivity extends BaseActivity {

    //标题栏
    private static final String TAG = "WebPageActivity";
    private WebViewViewModel mWebViewViewModel;
    protected String mURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_activity_framelayout);
        mWebViewViewModel = ViewModelProviders.of(this).get(WebViewViewModel.class);
        ZLog.d(TAG + QbSdk.getTbsVersion(this));
        handleIntent(getIntent());
//        initToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(mURL)) {
            ZLog.d(TAG + "handle intent, extra is good, but value is bad");
            finish();
            return;
        } else {
            if (findFragment(WebviewFragment.class) == null) {
                loadRootFragment(R.id.common_fragment_content, getWebViewFragment());
            }
        }
    }


    protected BaseWebviewFragment getWebViewFragment(){
        return  WebviewFragment.newInstance(mURL);
    }

    private void handleIntent(Intent intent) {
        if (intent.hasExtra(BaseWebviewFragment.INTENT_KEY_URL)) {
            mURL = Uri.decode(intent.getStringExtra(BaseWebviewFragment.INTENT_KEY_URL));
        } else {
            ZLog.d("handle intent, but extra is bad");
            return;
        }
    }

//    private void initToolbar() {
//        mToolbar = findViewById(R.id.common_toolbar);
//        mWebViewViewModel.getTitleLiveData().observe(WebPageActivity.this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable final String s) {
//                if (!s.equals("about:blank")) {
//                    mToolbar.setTitle(s);
//                }
//                ZLog.d("mWebViewViewModel: " + hashCode() + "  title: " + s);
//            }
//        });
//        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressedSupport();
//            }
//        });
//    }

}

