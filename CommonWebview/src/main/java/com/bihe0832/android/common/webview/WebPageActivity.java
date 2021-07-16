package com.bihe0832.android.common.webview;

import com.bihe0832.android.common.webview.base.BaseWebviewActivity;
import com.bihe0832.android.common.webview.base.BaseWebviewFragment;
import com.bihe0832.android.lib.router.annotation.Module;

import static com.bihe0832.android.common.webview.WebPageActivity.MODULE_NAME_WEB_PAGE;

@Module(MODULE_NAME_WEB_PAGE)
public final class WebPageActivity extends BaseWebviewActivity {
    public static final String MODULE_NAME_WEB_PAGE = "zweb";

    protected BaseWebviewFragment getWebViewFragment() {
        return CommonWebviewFragment.newInstance(mURL);
    }

    @Override
    protected Class getWebViewFragmentClass() {
        return CommonWebviewFragment.class;
    }

}

