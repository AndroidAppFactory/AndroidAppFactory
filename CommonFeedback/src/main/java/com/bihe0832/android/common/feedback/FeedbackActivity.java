package com.bihe0832.android.common.feedback;

import android.content.Intent;
import android.text.TextUtils;

import com.bihe0832.android.common.webview.base.BaseWebviewActivity;
import com.bihe0832.android.common.webview.base.BaseWebviewFragment;
import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.framework.router.RouterConstants;
import com.bihe0832.android.lib.router.annotation.Module;


@Module(RouterConstants.MODULE_NAME_FEEDBACK)
public final class FeedbackActivity extends BaseWebviewActivity {


    @Override
    protected BaseWebviewFragment getWebViewFragment() {
        return FeedbackFragment.newInstance(getURL());
    }

    @Override
    protected Class getWebViewFragmentClass() {
        return FeedbackFragment.class;
    }

    @Override
    protected void initToolbar() {
        initToolbar("建议反馈", true);
    }

    @Override
    protected void handleIntent(Intent intent) {
        super.handleIntent(intent);
        if (TextUtils.isEmpty(getURL())) {
            ZixieContext.INSTANCE.showToast("请输入正确的反馈地址");
            finish();
            return;
        }
    }
}
