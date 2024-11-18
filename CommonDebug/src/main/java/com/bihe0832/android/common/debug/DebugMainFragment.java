package com.bihe0832.android.common.debug;

import android.util.DisplayMetrics;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.bihe0832.android.common.debug.module.DebugCommonFragment;
import com.bihe0832.android.framework.ui.BaseFragment;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.ConvertUtils;
import com.flyco.tablayout.SlidingTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;


/**
 * Created by zixie on 16/6/30.
 */
public class DebugMainFragment extends BaseFragment {

    public static final String TAB_FOR_DEV_COMMON = "通用调试";

    private ViewPager mViewPager;
    private SlidingTabLayout mTabBar;
    private int lastTab = 0;

    protected String[] mTabString = null;

    public DebugMainFragment() {
        this(new String[]{TAB_FOR_DEV_COMMON});
    }

    public DebugMainFragment(String[] tabString) {
        super();
        mTabString = tabString;
        lastTab = getDefaultTabIndex();
    }

    protected Fragment getFragmentByIndex(String title) {
        return new DebugCommonFragment();
    }

    protected int getDefaultTabIndex() {
        return 0;
    }

    @Override
    protected int getLayoutID() {
        return R.layout.com_bihe0832_fragment_debug_main;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
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
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mTabBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mTabBar.hideMsg(position);
//                mTabBar.getItemView(lastTab).setBackground(null);
//                mTabBar.getItemView(position).setBackground(getContext().getDrawable(R.mipmap.btn_pause));
                lastTab = position;
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
        mTabBar.setCurrentTab(getDefaultTabIndex());
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser, boolean hasCreateView) {
        super.setUserVisibleHint(isVisibleToUser, hasCreateView);
        getFragmentByIndex(ConvertUtils.getSafeValueFromArray(mTabString, lastTab, "")).setUserVisibleHint(isVisibleToUser);
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
