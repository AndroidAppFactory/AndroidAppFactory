package com.bihe0832.android.base.debug.tab

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.framework.ui.main.CommonEmptyFragment
import com.bihe0832.android.lib.ui.custom.view.background.TextViewWithBackground
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import kotlinx.android.synthetic.main.activity_common_tab.*
import java.util.*

class DebugCommonTabActivity : BaseActivity() {
    private val mContext: Context = this
    private val mFragments = ArrayList<Fragment>()
    private val mFragments2 = ArrayList<Fragment>()
    private val mTitles = arrayOf("首页", "消息", "联系人", "更多")
    private val mIconUnselectIds = intArrayOf(
            R.drawable.icon_author, R.drawable.icon_camera,
            R.drawable.icon_cloud, R.drawable.icon_cycle)
    private val mIconSelectIds = intArrayOf(
            R.drawable.icon_author, R.drawable.icon_camera,
            R.drawable.icon_cloud, R.drawable.icon_cycle)
    private val mTabEntities: ArrayList<CustomTabEntity> = ArrayList<CustomTabEntity>()
    private var mAdapter: MyPagerAdapter? = null

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_tab)
        for (title in mTitles) {
            mFragments.add(CommonEmptyFragment.newInstance("Switch ViewPager $title"))
            mFragments2.add(CommonEmptyFragment.newInstance("Switch Fragment $title"))
        }
        for (i in mTitles.indices) {
            mTabEntities.add(TabEntity(mTitles[i], mIconSelectIds[i], mIconUnselectIds[i]))
        }
        vp_2.setAdapter(MyPagerAdapter(getSupportFragmentManager()))
        /** with nothing  */
        val mTabLayout_1 = tl_1

        /** with ViewPager  */
        val mTabLayout_2 = tl_2

        /** with Fragments  */
        val mTabLayout_3 = tl_3

        /** indicator固定宽度  */
        val mTabLayout_4 = tl_4

        /** indicator固定宽度  */
        val mTabLayout_5 = tl_5

        /** indicator矩形圆角  */
        val mTabLayout_6 = tl_6

        /** indicator三角形  */
        val mTabLayout_7 = tl_7

        /** indicator圆角色块  */
        val mTabLayout_8 = tl_8
        mTabLayout_1.setTabData(mTabEntities)
        tl_2()
        mTabLayout_3.setTabData(mTabEntities, this, R.id.fl_change, mFragments2)
        mTabLayout_4.setTabData(mTabEntities)
        mTabLayout_5.setTabData(mTabEntities)
        mTabLayout_6.setTabData(mTabEntities)
        mTabLayout_7.setTabData(mTabEntities)
        mTabLayout_8.setTabData(mTabEntities)
        mTabLayout_3.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(position: Int) {
                mTabLayout_1.setCurrentTab(position)
                mTabLayout_2.setCurrentTab(position)
                mTabLayout_4.setCurrentTab(position)
                mTabLayout_5.setCurrentTab(position)
                mTabLayout_6.setCurrentTab(position)
                mTabLayout_7.setCurrentTab(position)
                mTabLayout_8.setCurrentTab(position)
            }

            override fun onTabReselect(position: Int) {}
        })
        mTabLayout_8.setCurrentTab(2)
        mTabLayout_3.setCurrentTab(1)

        //显示未读红点
        mTabLayout_1.showDot(2)
        mTabLayout_3.showDot(1)
        mTabLayout_4.showDot(1)

        //两位数
        mTabLayout_2.showMsg(0, "测试")
        mTabLayout_2.setMsgMargin(0, -5f, 5f)

        //三位数
        mTabLayout_2.showMsg(1, 100)
        mTabLayout_2.setMsgMargin(1, -5f, 5f)

        //设置未读消息红点
        mTabLayout_2.showDot(2)
        val rtv_2_2: TextViewWithBackground = mTabLayout_2.getMsgView(2)
        if (rtv_2_2 != null) {
            rtv_2_2.width = dp2px(7.5f)
            rtv_2_2.height = dp2px(7.5f)
        }

        //设置未读消息背景
        mTabLayout_2.showMsg(3, 5)
        mTabLayout_2.setMsgMargin(3, 0f, 5f)
        val rtv_2_3: TextViewWithBackground = mTabLayout_2.getMsgView(3)
        if (rtv_2_3 != null) {
            rtv_2_3.setBackgroundColor(Color.parseColor("#6D8FB0"))
        }
    }

    var mRandom = Random()
    private fun tl_2() {
        tl_2.setTabData(mTabEntities)
        tl_2.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(position: Int) {
                vp_2.setCurrentItem(position)
            }

            override fun onTabReselect(position: Int) {
                if (position == 0) {
                    tl_2.showMsg(0, mRandom.nextInt(100) + 1)
                    //                    UnreadMsgUtils.show(mTabLayout_2.getMsgView(0), mRandom.nextInt(100) + 1);
                }
            }
        })
        vp_2.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                tl_2.setCurrentTab(position)
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
            return mTitles[position]
        }

        override fun getItem(position: Int): Fragment {
            return mFragments[position]
        }
    }

    protected fun dp2px(dp: Float): Int {
        val scale = mContext.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}