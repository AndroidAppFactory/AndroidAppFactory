package com.bihe0832.android.test;

import android.support.v4.app.Fragment;
import com.bihe0832.android.base.test.TestDebugTempFragment;
import com.bihe0832.android.base.test.tts.TestTTSFragment;
import com.bihe0832.android.common.test.module.TestDebugCommonFragment;
import com.bihe0832.android.test.module.TestBasicFragment;
import com.bihe0832.android.test.module.TestRouterFragment;


/**
 * Created by hardyshi on 16/6/30.
 */
public class TestMainFragment extends com.bihe0832.android.common.test.TestMainFragment {

    private static final String TAB_FOR_DEV_COMMON = "通用调试";
    private static final String TAB_FOR_DEV_TEMP = "临时调试";
    private static final String TAB_FOR_ROUTER = "路由测试";
    private static final String TAB_FOR_DEV = "开发测试";

    public TestMainFragment() {
        super(new String[]{
                TAB_FOR_DEV_COMMON, TAB_FOR_DEV_TEMP, TAB_FOR_ROUTER, TAB_FOR_DEV
        });
    }

    protected Fragment getFragmentByIndex(String title) {
        if (title.equals(TAB_FOR_DEV)) {
            return new TestTTSFragment();
        } else if (title.equals(TAB_FOR_DEV_TEMP)) {
            return new TestDebugTempFragment();
        } else if (title.equals(TAB_FOR_DEV_COMMON)) {
            return new TestDebugCommonFragment();
        } else if (title.equals(TAB_FOR_ROUTER)) {
            return new TestRouterFragment();
        } else {
            return new TestDebugCommonFragment();
        }
    }

    @Override
    protected int getDefaultTabIndex() {
        return 0;
    }
}
