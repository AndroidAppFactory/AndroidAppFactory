package com.bihe0832.android.test;

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

import com.bihe0832.android.framework.ui.BaseFragment;
import com.bihe0832.android.lib.utils.ConvertUtils;
import com.bihe0832.android.test.module.TestDebugCommonFragment;
import com.bihe0832.android.test.module.TestDebugTempFragment;
import com.bihe0832.android.test.module.TestRouterFragment;
import com.bihe0832.android.test.module.card.TestListFragment;
import com.flyco.tablayout.SlidingTabLayout;


/**
 * Created by hardyshi on 16/6/30.
 */
public class TestMainFragment extends BaseFragment {
    private ViewPager mViewPager;
    private SlidingTabLayout mTabBar;
    public static final String DEFAULT_TAB = "3";
    public static final String INTENT_EXTRA_KEY_TEST_ITEM_TAB = "tab";

    private static final String TAB_FOR_DEV_COMMON = "通用调试";
    private static final String TAB_FOR_ROUTER = "路由测试";
    private static final String TAB_FOR_DEV_TEMP = "临时调试";
    private static final String TAB_FOR_NEW = "模块测试";
    private String[] mTabString = new String[]{
            TAB_FOR_DEV_COMMON, TAB_FOR_DEV_TEMP, TAB_FOR_ROUTER, TAB_FOR_NEW
    };

    public static TestMainFragment newInstance(int tab) {

        Bundle args = new Bundle();
        args.putString(INTENT_EXTRA_KEY_TEST_ITEM_TAB, String.valueOf(tab));
        TestMainFragment fragment = new TestMainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test_main, container, false);
        initView(view);
        Bundle bundle = getArguments();
        String tab = DEFAULT_TAB;
        if (bundle != null) {
            tab = bundle.getString(INTENT_EXTRA_KEY_TEST_ITEM_TAB);
        }
        mTabBar.setCurrentTab(ConvertUtils.parseInt(tab, 0));
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

            if (mTabString[position].equals(TAB_FOR_NEW)) {
                return new TestListFragment();
            } else if (mTabString[position].equals(TAB_FOR_DEV_COMMON)) {
                return new TestDebugCommonFragment();
            } else if (mTabString[position].equals(TAB_FOR_DEV_TEMP)) {
                return new TestDebugTempFragment();
            } else if (mTabString[position].equals(TAB_FOR_ROUTER)) {
                return new TestRouterFragment();
            } else {
                return new TestDebugCommonFragment();
            }
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
