package com.bihe0832.android.framework.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.bihe0832.android.common.deprecated.R;
import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.framework.ui.BaseFragment;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.ReflecterHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import me.yokeyword.fragmentation.ISupportFragment;

/**
 * @author zixie code@bihe0832.com Created on 2022/8/26. Description: Description
 */
public class CommonRootActivity extends CommonActivity {

    public static final String DEBUG_MODULE_CLASS_NAME = "com.bihe0832.android.common.module.class.name";
    public static final String DEBUG_MODULE_TITLE_NAME = "com.bihe0832.android.common.module.title.name";
    final String TAG = this.getClass().getSimpleName();
    private String rootFragmentClassName = "";
    private String rootFragmentTitleName = "";

    public static void startRootActivity(Context context, Class activityClass, Map<String, String> data) {
        Intent intent = new Intent(context, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (data != null) {
            for (Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }
        context.startActivity(intent);
    }

    public static void startRootActivity(Context context, Class activityClass, Class cls, String titleName) {
        HashMap<String, String> data = new HashMap<>();
        data.put(DEBUG_MODULE_CLASS_NAME, cls.getName());
        data.put(DEBUG_MODULE_TITLE_NAME, titleName);
        startRootActivity(context, activityClass, data);
    }


    public static void startRootActivity(Context context, Class activityClass, Class cls, String titleName,
            Map<String, String> data) {
        HashMap<String, String> paramData = new HashMap<>();
        if (data != null) {
            paramData.putAll(data);
        }
        paramData.put(DEBUG_MODULE_CLASS_NAME, cls.getName());
        paramData.put(DEBUG_MODULE_TITLE_NAME, titleName);
        startRootActivity(context, activityClass, paramData);
    }

    public static void startCommonRootActivity(Context context, Class cls, String titleName) {
        startRootActivity(context, CommonRootActivity.class, cls, titleName);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootFragmentClassName = getIntent().getStringExtra(DEBUG_MODULE_CLASS_NAME);
        rootFragmentTitleName = getIntent().getStringExtra(DEBUG_MODULE_TITLE_NAME);
        ZLog.d(TAG, "rootFragmentClassName: " + rootFragmentClassName);
        ZLog.d(TAG, "rootFragmentTitleName: " + rootFragmentTitleName);
        initToolbar(R.id.common_toolbar, getTitleName(), false, true, com.bihe0832.android.lib.aaf.res.R.drawable.icon_left_arrow);
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
                    ISupportFragment fragment = (ISupportFragment) ReflecterHelper.newInstance(rootFragmentClassName,
                            null);
                    loadRootFragment(fragment);
                    fragment.onNewBundle(getIntent().getExtras());
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
