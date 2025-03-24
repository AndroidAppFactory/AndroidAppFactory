package com.bihe0832.android.common.debug.module;

import android.content.Context;
import androidx.annotation.NonNull;
import com.bihe0832.android.framework.ui.BaseFragment;
import com.bihe0832.android.framework.ui.main.CommonRootActivity;
import java.util.Locale;
import java.util.Map;

/**
 * Summary
 *
 * @author code@bihe0832.com Created on 2023/9/6. Description:
 */
public class DebugRootActivity extends CommonRootActivity {

    public static final void startDebugRootActivity(Context context, Class cls, String titleName) {
        startRootActivity(context, DebugRootActivity.class, cls, titleName);
    }

    public static final void startDebugRootActivity(Context context, Class cls, String titleName,
            Map<String, String> data) {
        startRootActivity(context, DebugRootActivity.class, cls, titleName, data);
    }

    @Override
    public boolean supportMultiLanguage() {
        return true;
    }

    @Override
    public void onLocaleChanged(@NonNull Locale lastLocale, @NonNull Locale toLanguageTag) {
        super.onLocaleChanged(lastLocale, toLanguageTag);
        try {
            Class rootFragmentClass = Class.forName(getRootFragmentClassName());
            if (findFragment(rootFragmentClass) != null && findFragment(rootFragmentClass) instanceof BaseFragment) {
                ((BaseFragment) findFragment(rootFragmentClass)).onLocaleChanged(lastLocale, toLanguageTag);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
