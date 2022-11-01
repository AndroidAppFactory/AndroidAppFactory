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

    override fun getFragments(): ArrayList<BaseFragment> {
        return ArrayList<BaseFragment>().apply {
            add(CommonEmptyFragment.newInstance("Test1", Color.GREEN))
            add(CommonEmptyFragment.newInstance("Test2", Color.BLUE))
            add(CommonEmptyFragment.newInstance("Test3", Color.YELLOW))
            add(CommonEmptyFragment.newInstance("Test4", Color.RED))
            add(CommonEmptyFragment.newInstance("Test5", Color.MAGENTA))

        }
    }

    override fun getBottomBarTabs(): ArrayList<BaseBottomBarTab> {
        ArrayList<BaseBottomBarTab>().apply {
            add(
                DebugSvgaBottomBarTab(
                    context,
                    R.mipmap.icon_camera,
                    R.mipmap.icon_author,
                    "ic_voice.svga",
                    "弹框"
                )
            )
            add(
                DebugSvgaBottomBarTab(
                    context,
                    R.mipmap.icon_author,
                    R.mipmap.icon_camera,
                    "ic_voice.svga",
                    "下载"
                )
            )
            add(
                DebugSvgaBottomBarTab(
                    context,
                    R.mipmap.icon_feedback,
                    R.mipmap.icon_cloud,
                    "ic_voice.svga",
                    "权限"
                )
            )
            add(
                DebugSvgaBottomBarTab(
                    context,
                    R.mipmap.icon_cloud,
                    R.mipmap.icon_feedback,
                    "ic_voice.svga",
                    "文件"
                )
            )
            add(
                DebugSvgaBottomBarTab(
                    context,
                    R.mipmap.icon_cloud,
                    R.mipmap.icon_feedback,
                    "ic_voice.svga",
                    "文件"
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