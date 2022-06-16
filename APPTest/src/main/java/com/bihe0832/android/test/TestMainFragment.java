package com.bihe0832.android.test;

import android.support.v4.app.Fragment;

import com.bihe0832.android.base.debug.DebugTempFragment;
import com.bihe0832.android.base.debug.dialog.DebugDialogFragment;
import com.bihe0832.android.base.debug.download.DebugDownloadFragment;
import com.bihe0832.android.base.debug.temp.DebugBasicFragment;
import com.bihe0832.android.base.debug.view.DebugTextViewFragment;
import com.bihe0832.android.common.debug.DebugMainFragment;
import com.bihe0832.android.common.debug.module.DebugCommonFragment;
import com.bihe0832.android.test.module.DebugRouterFragment;


/**
 * Created by hardyshi on 16/6/30.
 */
public class TestMainFragment extends DebugMainFragment {

    private static final String TAB_FOR_DEV_COMMON = "通用调试";
    private static final String TAB_FOR_DEV_TEMP = "临时调试";
    private static final String TAB_FOR_ROUTER = "路由测试";
    private static final String TAB_FOR_DEV = "开发测试";

    public TestMainFragment() {
        super(new String[]{
                TAB_FOR_DEV_COMMON, TAB_FOR_DEV_TEMP, TAB_FOR_ROUTER, TAB_FOR_DEV
        });
    }

    // 当前是否是开发模式
    private final boolean isDev = true;

    protected Fragment getFragmentByIndex(String title) {
        if (title.equals(TAB_FOR_DEV)) {
            return new DebugDownloadFragment();
        } else if (title.equals(TAB_FOR_DEV_TEMP)) {
            return new DebugTempFragment();
        } else if (title.equals(TAB_FOR_DEV_COMMON)) {
            return new DebugCommonFragment();
        } else if (title.equals(TAB_FOR_ROUTER)) {
            return new DebugRouterFragment();
        } else {
            return new DebugCommonFragment();
        }
    }

    @Override
    protected int getDefaultTabIndex() {
        if (isDev) {
            return mTabString.length - 1;
        } else {
            return 0;
        }
    }
}
