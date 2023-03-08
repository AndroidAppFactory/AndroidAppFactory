package com.bihe0832.android.app.message;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.bihe0832.android.app.router.RouterConstants;
import com.bihe0832.android.framework.ui.main.CommonActivity;
import com.bihe0832.android.lib.router.annotation.Module;


@Module(RouterConstants.MODULE_NAME_MESSAGE)
public final class AAFMessageActivity extends CommonActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initToolbar("消息中心", true);
        if (findFragment(AAFMessageListFragment.class) == null) {
            loadRootFragment(new AAFMessageListFragment());
        }
    }
}
