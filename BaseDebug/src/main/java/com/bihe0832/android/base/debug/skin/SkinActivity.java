package com.bihe0832.android.base.debug.skin;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.bihe0832.android.base.debug.R;
import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.theme.ThemeManager;

public class SkinActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin);
    }

    public void apply(View view) {
        ThemeManager.INSTANCE.applyTheme(ZixieContext.INSTANCE.getZixieFolder() + "skin.apk");
    }

    public void restore(View view) {
        ThemeManager.INSTANCE.applyTheme("");
    }

    public void load(View view) {
        FileUtils.INSTANCE.copyAssetsFileToPath(this, "skin.apk", ZixieContext.INSTANCE.getZixieFolder() + "skin.apk");
    }
}
