package com.bihe0832.android.base.debug.tab

import android.view.View
import com.bihe0832.android.base.debug.tab.bottom.DebugSimpleBottomTabFragment
import com.bihe0832.android.base.debug.tab.bottom.DebugSvgaBottomTabFragment
import com.bihe0832.android.base.debug.tab.bottom.SvgaBottomTabFragment
import com.bihe0832.android.common.debug.base.BaseDebugListFragment
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.lib.adapter.CardBaseModule

class DebugTabFragment : BaseDebugListFragment() {

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("DebugCommonTabActivity", View.OnClickListener { startActivityWithException(DebugCommonTabActivity::class.java) }))
            add(DebugItemData("DebugSegmentTabActivity", View.OnClickListener { startActivityWithException(DebugSegmentTabActivity::class.java) }))
            add(DebugItemData("DebugSlidingTabActivity", View.OnClickListener { startActivityWithException(DebugSlidingTabActivity::class.java) }))
            add(getDebugFragmentItemData("首页SimpleTAB", DebugSimpleBottomTabFragment::class.java))
            add(getDebugFragmentItemData("首页SVGA TAB", SvgaBottomTabFragment::class.java))
            add(getDebugFragmentItemData("首页自定义SVGA TAB", DebugSvgaBottomTabFragment::class.java))
            add(getDebugFragmentItemData("常见多TAB页面", DebugSlidingFragment::class.java))


        }
    }
}