package com.bihe0832.android.app.router;


import com.bihe0832.android.app.about.AboutActivity;
import com.bihe0832.android.common.feedback.FeedbackActivity;
import com.bihe0832.android.common.webview.BaseWebviewFragment;
import com.bihe0832.android.common.webview.WebPageActivity;

/**
 * Created by hardyshi on 2017/10/25.
 */

public class RouterConstants {
    //用户反馈
    public static final String MODULE_NAME_FEEDBACK = FeedbackActivity.MODULE_NAME_FEEDBACK;
    public static final String MODULE_NAME_WEB_PAGE = WebPageActivity.MODULE_NAME_WEB_PAGE;
    public static final String INTENT_EXTRA_KEY_WEB_URL = BaseWebviewFragment.INTENT_KEY_URL;
    public static final String MODULE_NAME_BASE_ABOUT = AboutActivity.MODULE_NAME_BASE_ABOUT;
}
