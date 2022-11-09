package com.bihe0832.android.common.webview;

import com.bihe0832.android.common.webview.base.BaseWebviewActivity;
import com.bihe0832.android.common.webview.base.BaseWebviewFragment;
import com.bihe0832.android.common.webview.base.CookieManagerForZixie;
import com.bihe0832.android.framework.router.RouterConstants;
import com.bihe0832.android.lib.router.annotation.Module;

@Module(RouterConstants.MODULE_NAME_WEB_PAGE)
public class WebPageActivity extends BaseWebviewActivity {

    protected BaseWebviewFragment getWebViewFragment() {
        return CommonWebviewFragment.newInstance(getURL());
    }

    @Override
    protected Class getWebViewFragmentClass() {
        return CommonWebviewFragment.class;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CookieManagerForZixie.INSTANCE.removeCookiesForDomain(getURL());
    }
}

