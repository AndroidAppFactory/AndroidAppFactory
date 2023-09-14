package com.bihe0832.android.common.panel.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.bihe0832.android.common.panel.R;
import com.bihe0832.android.framework.ui.BaseActivity;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2023/9/6.
 *         Description:
 */
public class PanelActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.com_bihe0832_common_activity_panel);
        if (findFragment(PanelMainFragment.class) == null) {
            loadRootFragment(R.id.about_fragment_content, new PanelMainFragment());
        }
    }
}
