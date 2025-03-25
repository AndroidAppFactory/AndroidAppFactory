package com.bihe0832.android.base.debug.tab.bottom

import android.graphics.Color
import android.view.View
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.ui.bottom.bar.CommonMainFragment
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.framework.ui.main.CommonEmptyFragment
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.bottom.bar.BaseBottomBarTab

class DebugSvgaBottomTabFragment : CommonMainFragment() {

    override fun getDefaultTabID(): Int {
        return 1
    }

    override fun initFragments(): ArrayList<BaseFragment> {
        return ArrayList<BaseFragment>().apply {
            add(CommonEmptyFragment.newInstance("Test1", Color.GREEN))
            add(CommonEmptyFragment.newInstance("Test2", Color.BLUE))
            add(CommonEmptyFragment.newInstance("Test3", Color.YELLOW))
            add(CommonEmptyFragment.newInstance("Test4", Color.RED))
            add(CommonEmptyFragment.newInstance("Test5", Color.MAGENTA))

        }
    }

    override fun initBottomBarTabs(): ArrayList<BaseBottomBarTab> {
        ArrayList<BaseBottomBarTab>().apply {
            add(
                DebugSvgaBottomBarTab(
                    context,
                        R.drawable.icon_camera,
                    R.drawable.icon_author,
                    "ic_voice.svga",
                    R.string.tab_1
                )
            )
            add(
                DebugSvgaBottomBarTab(
                    context,
                        R.drawable.icon_author,
                        R.drawable.icon_camera,
                    "ic_voice.svga",
                    R.string.tab_2
                )
            )
            add(
                DebugSvgaBottomBarTab(
                    context,
                        R.drawable.icon_feedback,
                        R.drawable.icon_cloud,
                    "ic_voice.svga",
                    R.string.tab_3
                )
            )
            add(
                DebugSvgaBottomBarTab(
                    context,
                        R.drawable.icon_cloud,
                        R.drawable.icon_feedback,
                    "ic_voice.svga",
                    R.string.tab_4
                )
            )
            add(
                DebugSvgaBottomBarTab(
                    context,
                        R.drawable.icon_cloud,
                        R.drawable.icon_feedback,
                    "ic_voice.svga",
                    R.string.tab_5
                )
            )
        }.let {
            return it
        }
    }

    override fun initView(view: View) {
        super.initView(view)
        ThreadManager.getInstance().start({
            ThreadManager.getInstance().runOnUIThread {
                getBottomBar().getItem(0).showUnreadMsg(0)
                getBottomBar().getItem(1).showUnreadMsg(2)
                getBottomBar().getItem(2).showUnreadMsg(22)
                getBottomBar().getItem(3).showUnreadMsg(200)
                getBottomBar().getItem(4).showUnreadMsg(-1)
            }
            changeTab(2)
        }, 3)

    }

    override fun onBottomBarTabUnselected(position: Int) {
        getBottomBar().getItem(position).showUnreadMsg()
        getBottomBar().getItem(position)
            .setUnreadMsgNum(getBottomBar().getItem(position).getUnreadMsgNum() - 1)
    }

    override fun onBottomBarTabSelected(position: Int, prePosition: Int) {
        onBottomBarTabReselected(position)
    }

    override fun onBottomBarTabReselected(position: Int) {
        getBottomBar().getItem(position).hideUnreadMsg()
    }

}