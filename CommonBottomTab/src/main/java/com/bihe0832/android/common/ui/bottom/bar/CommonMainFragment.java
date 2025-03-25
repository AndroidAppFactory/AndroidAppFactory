package com.bihe0832.android.common.ui.bottom.bar;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.bihe0832.android.common.bottom.bar.R;
import com.bihe0832.android.framework.ui.BaseFragment;
import com.bihe0832.android.lib.ui.bottom.bar.BaseBottomBarTab;
import com.bihe0832.android.lib.ui.bottom.bar.BottomBar;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @author zixie code@bihe0832.com Created on 2020/8/3. Description: Description
 */
public abstract class CommonMainFragment extends BaseFragment {

    protected BottomBar mBottomBar = null;
    private ArrayList<BaseFragment> mFragments = new ArrayList<>();
    private ArrayList<BaseBottomBarTab> mBottomBarTabs = new ArrayList<>();

    protected abstract int getDefaultTabID();

    protected abstract ArrayList<BaseFragment> initFragments();

    protected abstract ArrayList<BaseBottomBarTab> initBottomBarTabs();

    public ArrayList<BaseBottomBarTab> getBottomBarTabs() {
        return mBottomBarTabs;
    }

    public ArrayList<BaseFragment> getFragments() {
        return mFragments;
    }

    @Override
    protected int getLayoutID() {
        return R.layout.common_fragment_with_bottom;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        mBottomBarTabs = initBottomBarTabs();
        mBottomBar = view.findViewById(R.id.main_fragment_bottomBar);
        mBottomBar.setOnTabSelectedListener(new BottomBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, int prePosition) {
                showHideFragment(getFragments().get(position), getFragments().get(prePosition));

                if (position < getFragments().size()) {
                    getFragments().get(position).setUserVisibleHint(true);
                }

                if (position != prePosition && prePosition < getFragments().size()) {
                    getFragments().get(prePosition).setUserVisibleHint(false);
                }

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
        for (int i = 0; i < getBottomBarTabs().size(); i++) {
            mBottomBar.addItem(getBottomBarTabs().get(i));
        }
        loadFragments();
    }

    public void onBottomBarTabSelected(int position, int prePosition) {

    }


    public void onBottomBarTabUnselected(int position) {

    }


    public void onBottomBarTabReselected(int position) {

    }

    public void loadFragments() {
        mFragments = initFragments();
        BaseFragment[] list = getFragments().toArray(new BaseFragment[0]);
        loadMultipleRootFragment(R.id.fragment_content, mBottomBar.getCurrentItemPosition(), list);
    }

    @Override
    public void onLocaleChanged(@NonNull Locale lastLocale, @NonNull Locale toLanguageTag) {
        loadFragments();
    }

    protected void changeTab(final int position) {
        if (position < ((ViewGroup) mBottomBar.getChildAt(0)).getChildCount()) {
            mBottomBar.setCurrentItem(position);
        }
    }

    protected BottomBar getBottomBar() {
        return mBottomBar;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser, boolean hasCreateView) {
        super.setUserVisibleHint(isVisibleToUser, hasCreateView);
        if (getFragments().size() > mBottomBar.getCurrentItemPosition() && null != getFragments().get(
                mBottomBar.getCurrentItemPosition())) {
            getFragments().get(mBottomBar.getCurrentItemPosition()).setUserVisibleHint(isVisibleToUser);
        }
    }
}
