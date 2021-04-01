package com.bihe0832.android.common.feedback;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.framework.ui.main.CommonActivity;
import com.bihe0832.android.common.webview.BaseWebviewFragment;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.router.annotation.Module;

import java.net.URLDecoder;


@Module(FeedbackActivity.MODULE_NAME_FEEDBACK)
public class FeedbackActivity extends CommonActivity {

    public static final String MODULE_NAME_FEEDBACK = "feedback";

    private String mURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initToolbar("建议反馈", true);
        handleIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (findFragment(FeedbackFragment.class) == null) {
            loadRootFragment(R.id.common_fragment_content, FeedbackFragment.newInstance(mURL));
        } else {
            finish();
        }
    }

    private void handleIntent(Intent intent) {
        if (intent.hasExtra(BaseWebviewFragment.INTENT_KEY_URL)) {
            mURL = URLDecoder.decode(intent.getStringExtra(BaseWebviewFragment.INTENT_KEY_URL));
        } else {
            ZLog.e("handle intent, but extra is bad");
            ZixieContext.INSTANCE.showToast("请输入正确的反馈地址");
            finish();
            return;
        }
    }
}
