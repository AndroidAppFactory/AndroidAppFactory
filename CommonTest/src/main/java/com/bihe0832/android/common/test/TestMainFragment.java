package com.bihe0832.android.common.test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bihe0832.android.common.test.module.TestDebugCommonFragment;
import com.bihe0832.android.framework.ui.BaseFragment;
import com.bihe0832.android.lib.utils.ConvertUtils;
import com.flyco.tablayout.SlidingTabLayout;


/**
 * Created by hardyshi on 16/6/30.
 */
public class TestMainFragment extends BaseFragment {

    public static final String TAB_FOR_DEV_COMMON = "通用调试";

    private ViewPager mViewPager;
    private SlidingTabLayout mTabBar;

    private String[] mTabString = null;

    public TestMainFragment() {
        this(new String[]{
                TAB_FOR_DEV_COMMON
        });
    }

    public TestMainFragment(String[] tabString) {
        super();
        mTabString = tabString;
    }

    protected Fragment getFragmentByIndex(String title) {
        return new TestDebugCommonFragment();
    }


    protected int getDefaultTabIndex() {
        return 0;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test_main, container, false);
        initView(view);
        mTabBar.setCurrentTab(getDefaultTabIndex());
        return view;
    }

    private void initView(View view) {
        mViewPager = view.findViewById(R.id.framework_viewPager);
        mViewPager.setAdapter(new MyTaskPagerFragmentAdapter(getChildFragmentManager()));
        mViewPager.setOffscreenPageLimit(mTabString.length);

        mTabBar = view.findViewById(R.id.framework_tab);
        mTabBar.setViewPager(mViewPager);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        float tabWidth = dm.widthPixels / dm.density / mTabString.length;
        mTabBar.setTabWidth(tabWidth);
        mTabBar.setTabPadding(0);//默认20dp,去除尽量防止出现文字省略号的情况
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mTabBar.hideMsg(position);
            }

            @Override
            public void onPageSelected(int position) {
                mTabBar.hideMsg(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private class MyTaskPagerFragmentAdapter extends FragmentPagerAdapter {

        public MyTaskPagerFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return getFragmentByIndex(ConvertUtils.getSafeValueFromArray(mTabString, position, ""));
        }

        @Override
        public int getCount() {
            return mTabString.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabString[position];
        }
    }
}
