package com.bihe0832.android.common.tbsfeedback;

import com.bihe0832.android.common.feedback.FeedbackActivity;
import com.bihe0832.android.common.webview.base.BaseWebviewFragment;
import com.bihe0832.android.framework.router.RouterConstants;
import com.bihe0832.android.lib.router.annotation.Module;


@Module(RouterConstants.MODULE_NAME_FEEDBACK_TBS)
public final class TBSFeedbackActivity extends FeedbackActivity {

    @Override
    protected BaseWebviewFragment getWebViewFragment() {
        return TBSFeedbackFragment.newInstance(getURL());
    }

    @Override
    protected Class getWebViewFragmentClass() {
        return TBSFeedbackFragment.class;
    }

}
