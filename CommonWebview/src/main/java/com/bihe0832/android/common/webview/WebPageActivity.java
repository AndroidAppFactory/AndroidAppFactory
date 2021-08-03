package com.bihe0832.android.common.webview;

import com.bihe0832.android.common.webview.base.BaseWebviewActivity;
import com.bihe0832.android.common.webview.base.BaseWebviewFragment;
import com.bihe0832.android.framework.router.RouterConstants;
import com.bihe0832.android.lib.router.annotation.Module;

@Module(RouterConstants.MODULE_NAME_WEB_PAGE)
public final class WebPageActivity extends BaseWebviewActivity {

    protected BaseWebviewFragment getWebViewFragment() {
        return CommonWebviewFragment.newInstance(mURL);
    }

    @Override
    protected Class getWebViewFragmentClass() {
        return CommonWebviewFragment.class;
    }

}

