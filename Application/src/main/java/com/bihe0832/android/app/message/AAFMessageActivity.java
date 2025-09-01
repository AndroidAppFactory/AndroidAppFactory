package com.bihe0832.android.app.message;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.bihe0832.android.app.R;
import com.bihe0832.android.app.router.RouterConstants;
import com.bihe0832.android.framework.ui.main.CommonActivity;
import com.bihe0832.android.lib.router.annotation.Module;
import com.bihe0832.android.lib.theme.ThemeResourcesManager;


@Module(RouterConstants.MODULE_NAME_MESSAGE)
public final class AAFMessageActivity extends CommonActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initToolbar(ThemeResourcesManager.INSTANCE.getString(R.string.settings_message_title), true);
        if (findFragment(AAFMessageListFragment.class) == null) {
            loadRootFragment(new AAFMessageListFragment());
        }
    }

    protected void onResume() {
        super.onResume();
        try {
            if (this.findFragment(AAFMessageListFragment.class) != null) {
                this.findFragment(AAFMessageListFragment.class).setUserVisibleHint(true);
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }
    }
}
