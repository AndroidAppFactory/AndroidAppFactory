package com.bihe0832.android.test;

import androidx.fragment.app.Fragment;
import com.bihe0832.android.base.compose.debug.DebugComposeRootFragment;
import com.bihe0832.android.base.debug.AAFDebugModuleFragment;
import com.bihe0832.android.base.debug.google.DebugGoogleFragment;
import com.bihe0832.android.common.debug.DebugMainFragment;
import com.bihe0832.android.test.module.AAFDebugCommonFragment;
import com.bihe0832.android.test.module.AAFDebugRouterFragment;


/**
 * Created by zixie on 16/6/30.
 */
public class TestMainFragment extends DebugMainFragment {

    private static final String TAB_FOR_DEV_COMMON = "通用调试";
    private static final String TAB_FOR_DEV_MODULE = "模块调试";
    private static final String TAB_FOR_COMPOSE = "Compose";
    private static final String TAB_FOR_DEV = "开发测试";

    public TestMainFragment() {
        super(new String[]{
                TAB_FOR_DEV_COMMON, TAB_FOR_DEV_MODULE, TAB_FOR_COMPOSE, TAB_FOR_DEV
        });
    }

    // 当前是否是开发模式
    private final boolean isDev = false;

    protected Fragment getFragmentByIndex(String title) {
        switch (title) {
            case TAB_FOR_DEV:
                return new DebugGoogleFragment();
            case TAB_FOR_DEV_MODULE:
                return new AAFDebugModuleFragment();
            case TAB_FOR_DEV_COMMON:
                return new AAFDebugCommonFragment();
            case TAB_FOR_COMPOSE:
                return new DebugComposeRootFragment();
            default:
                return new AAFDebugCommonFragment();
        }
    }

    @Override
    protected int getDefaultTabIndex() {
        if (isDev) {
            return mTabString.length - 1;
        } else {
            return 1;
        }
    }
}
