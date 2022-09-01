package com.bihe0832.android.common.debug;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.framework.ui.BaseFragment;
import com.bihe0832.android.framework.ui.main.CommonActivity;
import com.bihe0832.android.lib.adapter.CardInfoHelper;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.ReflecterHelper;
import com.bihe0832.android.lib.utils.os.BuildUtils;

import me.yokeyword.fragmentation.ISupportFragment;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2022/8/26.
 * Description: Description
 */
public class DebugMainActivity extends CommonActivity {

    private static final String TAG = "DebugMainActivity";

    public static final String DEBUG_MODULE_CLASS_NAME = "com.bihe0832.android.common.debug.module.class.name";
    public static final String DEBUG_MODULE_TITLE_NAME = "com.bihe0832.android.common.debug.module.title.name";

    private String rootFragmentClassName = "";
    private String rootFragmentTitleName = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootFragmentClassName = getIntent().getStringExtra(DEBUG_MODULE_CLASS_NAME);
        rootFragmentTitleName = getIntent().getStringExtra(DEBUG_MODULE_TITLE_NAME);

        ZLog.d(TAG, "rootFragmentClassName: " + rootFragmentClassName);
        ZLog.d(TAG, "rootFragmentTitleName: " + rootFragmentTitleName);

        initToolbar(rootFragmentTitleName, true);
        if (BuildUtils.INSTANCE.getSDK_INT() > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        }
        CardInfoHelper.getInstance().setAutoAddItem(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFragment();
    }

    protected void loadFragment() {
        try {
            if (TextUtils.isEmpty(rootFragmentClassName)) {
                ZixieContext.INSTANCE.showDebug("类名错误，请检查后重试");
                finish();
            }

            Class rootFragmentClass = Class.forName(rootFragmentClassName);
            if (rootFragmentClass.getClass().isAssignableFrom(BaseFragment.class.getClass())) {
                if (findFragment(rootFragmentClass) == null) {
                    loadRootFragment((ISupportFragment) ReflecterHelper.newInstance(this.rootFragmentClassName, null));
                }
            } else {
                ZixieContext.INSTANCE.showDebug(rootFragmentClassName + "不是继承 BaseFragment");
                finish();
            }
        } catch (ClassNotFoundException e) {
            ZixieContext.INSTANCE.showDebug("没有找到" + rootFragmentClassName);
            finish();
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
