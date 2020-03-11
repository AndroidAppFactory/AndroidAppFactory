package com.bihe0832.android.lib.router;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2017-07-18.
 * Description: Description
 */

public class RouterActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RouterContext.RouterCallback callback = RouterContext.INSTANCE.getGlobalRouterCallback();

        Uri uri = getIntent().getData();
        if (uri != null) {
            Routers.open(this, uri, callback);
        }
        finish();
    }
}
