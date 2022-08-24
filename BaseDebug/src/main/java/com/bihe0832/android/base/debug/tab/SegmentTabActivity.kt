package com.bihe0832.android.base.debug.tab

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.framework.ui.main.CommonEmptyFragment.Companion.newInstance
import com.flyco.tablayout.SegmentTabLayout
import com.flyco.tablayout.listener.OnTabSelectListener
import kotlinx.android.synthetic.main.activity_segment_tab.*

class SegmentTabActivity : BaseActivity() {
    private val mFragments = ArrayList<Fragment>()
    private val mFragments2 = ArrayList<Fragment>()
    private val mTitles = arrayOf("首页", "消息")
    private val mTitles_2 = arrayOf("首页", "消息", "联系人")
    private val mTitles_3 = arrayOf("首页", "消息", "联系人", "更多")
    private var mDecorView: View? = null
    private var mTabLayout_3: SegmentTabLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_segment_tab)
        for (title in mTitles_3) {
            mFragments.add(newInstance("Switch ViewPager $title"))
        }
        for (title in mTitles_2) {
            mFragments2.add(newInstance("Switch Fragment $title"))
        }
        mDecorView = window.decorView
        val tabLayout_1: SegmentTabLayout = tl_1
        val tabLayout_2: SegmentTabLayout = tl_2
        mTabLayout_3 = tl_3
        val tabLayout_4: SegmentTabLayout = tl_4
        val tabLayout_5: SegmentTabLayout = tl_5
        tabLayout_1.setTabData(mTitles)
        tabLayout_2.setTabData(mTitles_2)
        tl_3()
        tabLayout_4.setTabData(mTitles_2, this, R.id.fl_change, mFragments2)
        tabLayout_5.setTabData(mTitles_3)
        tl_6.setTabData(mTitles_3)
        //显示未读红点
        tabLayout_1.showDot(2)
        tabLayout_2.showDot(2)
        mTabLayout_3!!.showDot(1)
        tabLayout_4.showDot(1)

        //设置未读消息红点
        mTabLayout_3!!.showDot(2)
        val rtv_3_2 = mTabLayout_3!!.getMsgView(2)
        if (rtv_3_2 != null) {
            rtv_3_2.backgroundColor = Color.parseColor("#6D8FB0")
        }
    }

    private fun tl_3() {
        vp_2.setAdapter(MyPagerAdapter(supportFragmentManager))
        mTabLayout_3!!.setTabData(mTitles_3)
        mTabLayout_3!!.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(position: Int) {
                vp_2.setCurrentItem(position)
            }

            override fun onTabReselect(position: Int) {}
        })
        vp_2.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                mTabLayout_3!!.currentTab = position
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        vp_2.setCurrentItem(1)
    }

    private inner class MyPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount(): Int {
            return mFragments.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mTitles_3[position]
        }

        override fun getItem(position: Int): Fragment {
            return mFragments[position]
        }
    }
}