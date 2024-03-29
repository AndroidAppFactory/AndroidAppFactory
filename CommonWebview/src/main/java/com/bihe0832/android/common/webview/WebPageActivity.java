package com.bihe0832.android.common.webview;

import com.bihe0832.android.common.webview.base.BaseWebviewActivity;
import com.bihe0832.android.common.webview.base.BaseWebViewFragment;
import com.bihe0832.android.common.webview.nativeimpl.NativeCookieManager;
import com.bihe0832.android.framework.router.RouterConstants;
import com.bihe0832.android.lib.router.annotation.Module;

@Module(RouterConstants.MODULE_NAME_WEB_PAGE)
public class WebPageActivity extends BaseWebviewActivity {

    protected BaseWebViewFragment getWebViewFragment() {
        return CommonWebviewFragment.newInstance(getURL());
    }

    @Override
    protected Class getWebViewFragmentClass() {
        return CommonWebviewFragment.class;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NativeCookieManager.INSTANCE.removeCookiesForDomain(getURL());
    }
}

