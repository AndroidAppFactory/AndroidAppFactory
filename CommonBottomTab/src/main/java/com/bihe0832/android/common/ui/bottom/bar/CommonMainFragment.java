package com.bihe0832.android.common.ui.bottom.bar;

import android.view.View;

import com.bihe0832.android.common.bottom.bar.R;
import com.bihe0832.android.framework.ui.BaseFragment;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.ui.bottom.bar.BaseBottomBarTab;
import com.bihe0832.android.lib.ui.bottom.bar.BottomBar;

import java.util.ArrayList;

/**
 * @author zixie code@bihe0832.com
 * Created on 2020/8/3.
 * Description: Description
 */
public abstract class CommonMainFragment extends BaseFragment {

    protected BottomBar mBottomBar = null;
    private ArrayList<BaseFragment> mFragments = new ArrayList<>();
    private ArrayList<BaseBottomBarTab> mBottomBarTabs = new ArrayList<>();

    protected abstract int getDefaultTabID();

    protected abstract ArrayList<BaseFragment> getFragments();

    protected abstract ArrayList<BaseBottomBarTab> getBottomBarTabs();

    @Override
    protected int getLayoutID() {
        return R.layout.common_fragment_with_bottom;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);

        mFragments = getFragments();
        BaseFragment[] list = mFragments.toArray(new BaseFragment[0]);
        loadMultipleRootFragment(R.id.fragment_content, getDefaultTabID(), list);

        mBottomBarTabs = getBottomBarTabs();
        mBottomBar = view.findViewById(R.id.main_fragment_bottomBar);
        mBottomBar.setDefaultPosition(getDefaultTabID());
        mBottomBar.setOnTabSelectedListener(new BottomBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, int prePosition) {
                onBottomBarTabSelected(position, prePosition);
            }

            @Override
            public void onTabUnselected(int position) {
                onBottomBarTabUnselected(position);
            }

            @Override
            public void onTabReselected(int position) {
                onBottomBarTabReselected(position);
            }
        });
        for (int i = 0; i < mBottomBarTabs.size(); i++) {
            mBottomBar.addItem(mBottomBarTabs.get(i));
        }
    }

    public void onBottomBarTabSelected(int position, int prePosition) {
        showHideFragment(mFragments.get(position), mFragments.get(prePosition));
        for (int i = 0; i < mFragments.size(); i++) {
            if (i == mBottomBar.getCurrentItemPosition()) {
                mFragments.get(i).setUserVisibleHint(true);
            } else {
                mFragments.get(i).setUserVisibleHint(false);
            }
        }
    }


    public void onBottomBarTabUnselected(int position) {

    }


    public void onBottomBarTabReselected(int position) {

    }

    protected void changeTab(final int position) {
        if (position < mBottomBar.getChildCount()) {
            mBottomBar.setCurrentItem(position);
        }
    }

    protected BottomBar getBottomBar() {
        return mBottomBar;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser, boolean hasCreateView) {
        ZLog.d("setUserVisibleHint:$isVisibleToUser");
        super.setUserVisibleHint(isVisibleToUser, hasCreateView);
        if (mFragments.size() > mBottomBar.getCurrentItemPosition() && null != mFragments.get(mBottomBar.getCurrentItemPosition())) {
            mFragments.get(mBottomBar.getCurrentItemPosition()).setUserVisibleHint(isVisibleToUser);
        }
    }
}
