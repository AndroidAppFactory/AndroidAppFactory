package com.bihe0832.android.common.feedback;

import android.content.Intent;
import android.text.TextUtils;
import com.bihe0832.android.common.webview.base.BaseWebViewFragment;
import com.bihe0832.android.common.webview.base.BaseWebviewActivity;
import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.framework.router.RouterConstants;
import com.bihe0832.android.lib.router.annotation.Module;


@Module(RouterConstants.MODULE_NAME_FEEDBACK)
public class FeedbackActivity extends BaseWebviewActivity {


    @Override
    protected BaseWebViewFragment getWebViewFragment() {
        return FeedbackFragment.newInstance(getURL());
    }

    @Override
    protected Class getWebViewFragmentClass() {
        return FeedbackFragment.class;
    }

    @Override
    protected void initToolbar() {
        initToolbar(getResources().getString(com.bihe0832.android.model.res.R.string.feedback_title), true);
    }

    @Override
    protected void handleIntent(Intent intent) {
        super.handleIntent(intent);
        if (TextUtils.isEmpty(getURL())) {
            ZixieContext.INSTANCE.showToast(getResources().getString(com.bihe0832.android.model.res.R.string.feedback_bad_tips));
            finish();
        }
    }
}
