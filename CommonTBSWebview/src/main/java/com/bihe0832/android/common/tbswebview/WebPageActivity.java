package com.bihe0832.android.common.tbswebview;

import com.bihe0832.android.common.webview.base.BaseWebviewActivity;
import com.bihe0832.android.common.webview.base.BaseWebViewFragment;
import com.bihe0832.android.common.webview.nativeimpl.NativeCookieManager;
import com.bihe0832.android.framework.router.RouterConstants;
import com.bihe0832.android.lib.router.annotation.Module;

@Module(RouterConstants.MODULE_NAME_WEB_PAGE_TBS)
public class WebPageActivity extends BaseWebviewActivity {

    protected BaseWebViewFragment getWebViewFragment() {
        return CommonWebViewFragment.newInstance(getURL());
    }

    @Override
    protected Class getWebViewFragmentClass() {
        return CommonWebViewFragment.class;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NativeCookieManager.INSTANCE.removeCookiesForDomain(getURL());
    }
}

