package com.bihe0832.android.test;

import android.content.Intent;
import android.support.v4.app.Fragment;
import androidx.annotation.Nullable;
import com.bihe0832.android.base.test.TestDebugTempFragment;
import com.bihe0832.android.base.test.card.TestListFragment;
import com.bihe0832.android.base.test.dialog.TestDialogFragment;
import com.bihe0832.android.base.test.download.TestDownloadFragment;
import com.bihe0832.android.base.test.image.TestImageFragment;
import com.bihe0832.android.base.test.log.TestLogFragment;
import com.bihe0832.android.base.test.notify.TestNotifyFragment;
import com.bihe0832.android.base.test.photos.TestPhotosFragment;
import com.bihe0832.android.base.test.temp.TestBasicFragment;
import com.bihe0832.android.base.test.textview.TestTextView;
import com.bihe0832.android.common.test.module.TestDebugCommonFragment;
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

    // 当前是否是开发模式
    private final boolean isDev = true;
    protected Fragment getFragmentByIndex(String title) {
        if (title.equals(TAB_FOR_DEV)) {
            return new TestPhotosFragment();
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
        if (isDev) {
            return mTabString.length - 1;
        } else {
            return 0;
        }
    }
}
