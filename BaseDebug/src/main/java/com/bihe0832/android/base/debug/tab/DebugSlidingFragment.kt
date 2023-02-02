package com.bihe0832.android.base.debug.tab

import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.framework.ui.main.CommonEmptyFragment
import com.flyco.tablayout.listener.OnTabSelectListener
import kotlinx.android.synthetic.main.debug_tab_sliding_fragment.*

class DebugSlidingFragment : BaseFragment() {

    private val TAB_1 = "首页"
    private val TAB_2 = "消息"
    private var mTabList = arrayOf(TAB_1, TAB_2)

    private val mFragments = mutableListOf<Fragment>().apply {
        add(CommonEmptyFragment.newInstance(TAB_1))
        add(CommonEmptyFragment.newInstance(TAB_2))
    }

    override fun initView(view: View) {
        framework_viewPager.apply {
            offscreenPageLimit = mFragments.size - 1
            isScrollable = true
            adapter = object : FragmentPagerAdapter(childFragmentManager) {
                override fun getItem(position: Int): Fragment {
                    return mFragments[position]
                }

                override fun getCount(): Int {
                    return mFragments.size
                }

                override fun getPageTitle(position: Int): CharSequence? {
                    return mTabList[position]
                }
            }
        }?.let {
            framework_tab.apply {
                setViewPager(it)
                setOnTabSelectListener(object : OnTabSelectListener {
                    override fun onTabSelect(position: Int) {
                        resetTabUIByCurrentTab(position)
                    }

                    override fun onTabReselect(position: Int) {
                        onTabSelect(position)
                    }

                })
            }
        }

        showUnreadMsg(1, 100)
        resetTabUIByCurrentTab(0)
    }

    open fun resetTabUIByCurrentTab(currentTab: Int) {
        showUnreadMsg(currentTab, -1)
        var index = currentTab
        try {
            framework_tab?.apply {
                setCurrentTab(index)
                (getChildAt(0) as ViewGroup).let { rootView ->
                    for (i in 0 until tabCount) {
                        ((rootView.getChildAt(i) as ViewGroup).getChildAt(0) as AppCompatTextView).apply {
                            if (i == index) {
                                textSize = 14f
                                typeface = Typeface.DEFAULT_BOLD
                            } else {
                                textSize = 16f
                                typeface = Typeface.DEFAULT
                            }
                        }
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun showUnreadMsg(position: Int, p0: Int) {
        if (p0 > 0) {
            framework_tab.showMsg(position, p0)
//            framework_tab.getMsgView(position)?.run {
//                this.textSize = 14f
//            }
        } else {
            framework_tab.hideMsg(position)
        }
    }

    override fun getLayoutID(): Int {
        return R.layout.debug_tab_sliding_fragment
    }

}