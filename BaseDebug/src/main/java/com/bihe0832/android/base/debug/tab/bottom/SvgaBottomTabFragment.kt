package com.bihe0832.android.base.debug.tab.bottom

import android.graphics.Color
import android.view.View
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.base.debug.dialog.DebugDialogFragment
import com.bihe0832.android.base.debug.download.DebugDownloadFragment
import com.bihe0832.android.base.debug.file.DebugFileFragment
import com.bihe0832.android.base.debug.permission.DebugPermissionFragment
import com.bihe0832.android.common.ui.bottom.bar.CommonMainFragment
import com.bihe0832.android.common.ui.bottom.bar.SvgaBottomBarTab
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.framework.ui.main.CommonEmptyFragment
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.bottom.bar.BaseBottomBarTab

class SvgaBottomTabFragment : CommonMainFragment() {

    override fun getDefaultTabID(): Int {
        return 1
    }

    override fun getFragments(): ArrayList<BaseFragment> {
        return ArrayList<BaseFragment>().apply {
            add(DebugDialogFragment())
            add(DebugDownloadFragment())
            add(CommonEmptyFragment.newInstance("Test", Color.GREEN))
            add(DebugPermissionFragment())
            add(DebugFileFragment())
        }
    }

    override fun getBottomBarTabs(): ArrayList<BaseBottomBarTab> {
        ArrayList<BaseBottomBarTab>().apply {
            add(SvgaBottomBarTab(context, R.drawable.icon_camera,  R.drawable.icon_author, "ic_voice.svga", "弹框"))
            add(SvgaBottomBarTab(context,  R.drawable.icon_author, R.drawable.icon_camera, "ic_voice.svga", "下载"))
            add(SvgaBottomBarTab(context,  R.drawable.icon_feedback, R.drawable.icon_cloud, "ic_voice.svga", "权限"))
            add(SvgaBottomBarTab(context, R.drawable.icon_cloud,  R.drawable.icon_feedback, "ic_voice.svga", "文件"))
            add(SvgaBottomBarTab(context, R.drawable.icon_cloud,  R.drawable.icon_feedback, "ic_voice.svga", "文件"))
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

}