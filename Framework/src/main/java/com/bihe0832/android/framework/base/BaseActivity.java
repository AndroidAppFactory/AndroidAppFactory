package com.bihe0832.android.framework.base;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

import me.yokeyword.fragmentation.SupportActivity;

public class BaseActivity extends SupportActivity {

    protected Toolbar mToolbar;
    public final long CREATE_TIME = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (Fragment fragment:getSupportFragmentManager().getFragments()) {
            if(fragment.isAdded()){
                fragment.setUserVisibleHint(true);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (Fragment fragment:getSupportFragmentManager().getFragments()) {
            if(fragment.isAdded()){
                fragment.setUserVisibleHint(false);
            }
        }
    }

    public void onBack(){
        finish();
    }
}
