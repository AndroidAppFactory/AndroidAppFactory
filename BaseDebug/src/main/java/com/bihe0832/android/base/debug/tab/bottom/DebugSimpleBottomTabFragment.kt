package com.bihe0832.android.base.debug.tab.bottom

import android.view.View
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.base.debug.download.DebugDownloadFragment
import com.bihe0832.android.base.debug.file.DebugFileFragment
import com.bihe0832.android.base.debug.permission.DebugPermissionFragment
import com.bihe0832.android.common.ui.bottom.bar.CommonMainFragment
import com.bihe0832.android.common.ui.bottom.bar.SimpleBottomBarTab
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.ui.bottom.bar.BaseBottomBarTab

class DebugSimpleBottomTabFragment : CommonMainFragment() {

    override fun getDefaultTabID(): Int {
        return 1
    }

    override fun initFragments(): ArrayList<BaseFragment> {
        return ArrayList<BaseFragment>().apply {
            add(DebugDownloadFragment())
            add(DebugDownloadFragment())
            add(DebugPermissionFragment())
            add(DebugFileFragment())
        }
    }

    override fun initBottomBarTabs(): ArrayList<BaseBottomBarTab> {
        ArrayList<BaseBottomBarTab>().apply {
            add(SimpleBottomBarTab(context, R.drawable.icon_camera, "弹框"))
            add(SimpleBottomBarTab(context, R.drawable.icon_author, "下载"))
            add(SimpleBottomBarTab(context, R.drawable.icon_feedback, "权限"))
            add(SimpleBottomBarTab(context, R.drawable.icon_cloud, "文件"))
        }.let {
            return it
        }
    }

    override fun initView(view: View) {
        super.initView(view)
//        getBottomBar().getItem(0).showUnreadMsg(0)
//        getBottomBar().getItem(1).showUnreadMsg(2)
//        getBottomBar().getItem(2).showUnreadMsg(22)
//        getBottomBar().getItem(3).showUnreadMsg(200)
    }

}