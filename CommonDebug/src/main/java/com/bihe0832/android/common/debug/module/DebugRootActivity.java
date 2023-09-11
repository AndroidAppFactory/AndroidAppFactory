package com.bihe0832.android.common.debug.module;

import android.content.Context;
import com.bihe0832.android.framework.ui.main.CommonRootActivity;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2023/9/6.
 *         Description:
 */
public class DebugRootActivity extends CommonRootActivity {

    public static final void startDebugRootActivity(Context context, Class cls, String titleName) {
        startRootActivity(context, DebugRootActivity.class, cls, titleName);
    }

}
