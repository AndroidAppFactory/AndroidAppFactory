package com.bihe0832.android.base.debug.tab

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.framework.ui.main.CommonEmptyFragment.Companion.newInstance
import com.bihe0832.android.lib.media.image.loadCircleCropImage
import com.bihe0832.android.lib.ui.textview.ext.setDrawableLeft
import com.bihe0832.android.lib.utils.os.DisplayUtil
import com.flyco.tablayout.SlidingTabLayout
import com.flyco.tablayout.listener.OnTabSelectListener
import kotlinx.android.synthetic.main.activity_sliding_tab.tl_1
import kotlinx.android.synthetic.main.activity_sliding_tab.tl_10
import kotlinx.android.synthetic.main.activity_sliding_tab.tl_11
import kotlinx.android.synthetic.main.activity_sliding_tab.tl_2
import kotlinx.android.synthetic.main.activity_sliding_tab.tl_3
import kotlinx.android.synthetic.main.activity_sliding_tab.tl_4
import kotlinx.android.synthetic.main.activity_sliding_tab.tl_5
import kotlinx.android.synthetic.main.activity_sliding_tab.tl_6
import kotlinx.android.synthetic.main.activity_sliding_tab.tl_7
import kotlinx.android.synthetic.main.activity_sliding_tab.tl_8
import kotlinx.android.synthetic.main.activity_sliding_tab.tl_9
import kotlinx.android.synthetic.main.activity_sliding_tab.vp

class DebugSlidingTabActivity : BaseActivity(), OnTabSelectListener {
    private val mContext: Context = this
    private val mFragments = ArrayList<Fragment>()

    private val mTitles = arrayOf(
        "热门",
        "iOS",
        "Android",
        "前端",
        "后端",
        "设计",
        "工具资源",
    )

    //    private val mTitles = arrayOf(
//        "https://cdn.bihe0832.com/images/zixie_32.ico",
//        "https://cdn.bihe0832.com/images/head.jpg",
//        "https://cdn.bihe0832.com/images/head.jpg",
//        "https://cdn.bihe0832.com/images/zixie_32.ico",
//        "https://cdn.bihe0832.com/images/head.jpg",
//        "https://cdn.bihe0832.com/images/zixie_32.ico",
//        "https://cdn.bihe0832.com/images/android_favicon.png"
//    )
    private var mAdapter: MyPagerAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sliding_tab)
        for (title in mTitles) {
            mFragments.add(newInstance(title))
        }
        mAdapter = MyPagerAdapter(supportFragmentManager)
        vp.adapter = mAdapter
        /** 默认  */
        val tabLayout_1: SlidingTabLayout = tl_1

        /**自定义部分属性 */
        val tabLayout_2: SlidingTabLayout = tl_2

        /** 字体加粗,大写  */
        val tabLayout_3: SlidingTabLayout = tl_3

        /** tab固定宽度  */
        val tabLayout_4: SlidingTabLayout = tl_4

        /** indicator固定宽度  */
        val tabLayout_5: SlidingTabLayout = tl_5

        /** indicator圆  */
        val tabLayout_6: SlidingTabLayout = tl_6

        /** indicator矩形圆角  */
        val tabLayout_7: SlidingTabLayout = tl_7

        /** indicator三角形  */
        val tabLayout_8: SlidingTabLayout = tl_8

        /** indicator圆角色块  */
        val tabLayout_9: SlidingTabLayout = tl_9

        /** indicator圆角色块  */
        val tabLayout_10: SlidingTabLayout = tl_10
        tabLayout_1.setViewPager(vp)
        tabLayout_2.setViewPager(vp)
        tabLayout_2.setOnTabSelectListener(this)
        tabLayout_3.setViewPager(vp)
        tabLayout_4.setViewPager(vp)
        tabLayout_5.setViewPager(vp)
        tabLayout_6.setViewPager(vp)
        tabLayout_7.setViewPager(vp, mTitles)
        tabLayout_8.setViewPager(vp, mTitles, this, mFragments)
        tabLayout_9.setViewPager(vp)
        tabLayout_10.setViewPager(vp)
        tl_11.setViewPager(vp)
        vp.currentItem = 4
        tabLayout_1.showDot(4)
        tabLayout_3.showDot(4)
        tabLayout_2.showDot(4)
        tabLayout_2.showMsg(3, 5)
        tabLayout_2.setMsgMargin(3, 0f, 10f)
        val rtv_2_3 = tabLayout_2.getMsgView(3)
        if (rtv_2_3 != null) {
            rtv_2_3.backgroundColor = Color.parseColor("#6D8FB0")
        }
        tabLayout_2.showMsg(5, 5)
        tabLayout_2.setMsgMargin(5, 0f, 10f)

//        tabLayout_7.setOnTabSelectListener(new OnTabSelectListener() {
//            @Override
//            public void onTabSelect(int position) {
//                Toast.makeText(mContext, "onTabSelect&position--->" + position, Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onTabReselect(int position) {
//                mFragments.add(SimpleCardFragment.getInstance("后端"));
//                mAdapter.notifyDataSetChanged();
//                tabLayout_7.addNewTab("后端");
//            }
//        });
    }

    override fun onTabSelect(position: Int) {
        Toast.makeText(mContext, "onTabSelect&position--->$position", Toast.LENGTH_SHORT).show()
        tl_11.backgroundImageView.loadCircleCropImage(
            if (position == 0) {
                "https://alifei01.cfp.cn/creative/vcg/nowater800/new/VCG21ba1a56cfe.jpg"
            } else {
                "https://alifei01.cfp.cn/creative/vcg/nowarter800/new/VCG211154718095.jpg"
            },
        )

        (tl_10.getChildAt(0) as ViewGroup).let { rootView ->
            ((rootView.getChildAt(1) as ViewGroup).getChildAt(0) as AppCompatTextView).apply {
                compoundDrawablePadding = DisplayUtil.dip2px(context!!, 4f)
                setDrawableLeft(
                    R.mipmap.icon,
                    DisplayUtil.dip2px(context!!, 20f),
                    DisplayUtil.dip2px(context!!, 20f),
                )
            }
        }
    }

    override fun onTabReselect(position: Int) {
        Toast.makeText(mContext, "onTabReselect&position--->$position", Toast.LENGTH_SHORT).show()
    }

    private inner class MyPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int {
            return mFragments.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mTitles[position]
        }

        override fun getItem(position: Int): Fragment {
            return mFragments[position]
        }
    }
}
