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
 * @author zixie code@bihe0832.com
 * Created on 2022/8/26.
 * Description: Description
 */
public class DebugMainActivity extends CommonActivity {

    final String TAG = this.getClass().getSimpleName();

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

        initToolbar(getTitleName(), true, R.mipmap.btn_back);

        if (BuildUtils.INSTANCE.getSDK_INT() > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        }
        CardInfoHelper.getInstance().setAutoAddItem(true);
        loadFragment();
    }

    protected String getRootFragmentClassName() {
        return rootFragmentClassName;
    }

    protected String getTitleName() {
        if (TextUtils.isEmpty(rootFragmentTitleName)) {
            try {
                if (getRootFragmentClassName() != null) {
                    return getRootFragmentClassName().substring(getRootFragmentClassName().lastIndexOf(".") + 1);
                } else {
                    return this.getClass().getSimpleName();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this.getClass().getSimpleName();
        }
        return rootFragmentTitleName;
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Class rootFragmentClass = Class.forName(getRootFragmentClassName());
            if (findFragment(rootFragmentClass) != null) {
                ((BaseFragment) findFragment(rootFragmentClass)).setUserVisibleHint(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void loadFragment() {
        String rootFragmentClassName = getRootFragmentClassName();
        try {
            if (TextUtils.isEmpty(rootFragmentClassName)) {
                ZixieContext.INSTANCE.showDebug("类名错误，请检查后重试");
                finish();
            }

            Class rootFragmentClass = Class.forName(rootFragmentClassName);
            if (rootFragmentClass.getClass().isAssignableFrom(BaseFragment.class.getClass())) {
                if (findFragment(rootFragmentClass) == null) {
                    loadRootFragment((ISupportFragment) ReflecterHelper.newInstance(rootFragmentClassName, null));
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
