package com.bihe0832.android.app.language;

import androidx.annotation.NonNull;
import com.bihe0832.android.app.router.RouterConstants;
import com.bihe0832.android.common.language.BaseLanguageActivity;
import com.bihe0832.android.common.language.card.SettingsDataLanguage;
import com.bihe0832.android.lib.router.annotation.Module;
import java.util.List;

/**
 * @author zixie code@bihe0832.com Created on 2025/3/27. Description: Description
 */

@Module(RouterConstants.MODULE_NAME_LANGUAGE)
public class LanguageActivity extends BaseLanguageActivity {

    @NonNull
    @Override
    public List<SettingsDataLanguage> getLanguageList() {
        return AAFLanguageManager.INSTANCE.getSupportLanguageList();
    }
}
