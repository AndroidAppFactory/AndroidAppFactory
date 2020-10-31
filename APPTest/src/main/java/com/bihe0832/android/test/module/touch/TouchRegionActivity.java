package com.bihe0832.android.test.module.touch;

import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.framework.base.BaseActivity;
import com.bihe0832.android.lib.ui.touchregion.ViewExtForTouchRegionKt;
import com.bihe0832.android.test.R;


/**
 * Copyright (c) 2018 Tencent. All rights reserved.
 *
 * @Project: app
 * @Date: 2018/11/12 16:08
 * @Version: V1.0
 */
public class TouchRegionActivity extends BaseActivity implements View.OnClickListener {
    private ConstraintLayout rootView = null;
    private TextView tv = null;
    private TextView tv2 = null;
    private Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        setContentView(R.layout.activity_touch_region);

        initViews();
    }

    private void initViews() {
        rootView = (ConstraintLayout) findViewById(R.id.root);
        ((Toolbar) findViewById(R.id.common_toolbar)).setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressedSupport();
            }
        });

        //rootView.setOnClickListener(this);
        tv = (TextView) findViewById(R.id.tv);
        tv.setOnClickListener(this);
        ViewExtForTouchRegionKt.expandTouchRegionWithdp(tv, 200, 200, 200, 200);

        tv2 = (TextView) findViewById(R.id.tv2);
        tv2.setOnClickListener(this);
        ViewExtForTouchRegionKt.expandTouchRegionWithdp(tv2, 200, 200, 200, 200);
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        if (viewId == R.id.root) {
            ZixieContext.INSTANCE.showToast("背景被点击");
        } else if (viewId == R.id.tv) {
            ZixieContext.INSTANCE.showToast("Hello World");
        } else if (viewId == R.id.tv2) {
            ZixieContext.INSTANCE.showToast("Test");
        } else {

        }
    }
}