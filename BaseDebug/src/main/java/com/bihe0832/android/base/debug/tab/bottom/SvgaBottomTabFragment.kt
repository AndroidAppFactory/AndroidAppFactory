package com.bihe0832.android.base.debug.tab.bottom

import android.graphics.Color
import android.view.View
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.base.debug.download.DebugDownloadFragment
import com.bihe0832.android.base.debug.permission.DebugPermissionFragment
import com.bihe0832.android.base.debug.ui.DebugUIFragment
import com.bihe0832.android.common.ui.bottom.bar.CommonMainFragment
import com.bihe0832.android.common.ui.bottom.bar.SvgaBottomBarTab
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.framework.ui.main.CommonEmptyFragment
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.bottom.bar.BaseBottomBarTab
import java.util.Locale

class SvgaBottomTabFragment : CommonMainFragment() {

    override fun getDefaultTabID(): Int {
        return 1
    }

    override fun initFragments(): ArrayList<BaseFragment> {
        return ArrayList<BaseFragment>().apply {
            add(DebugDownloadFragment())
            add(DebugDownloadFragment())
            add(CommonEmptyFragment.newInstance("Test", Color.GREEN))
            add(DebugPermissionFragment())
            add(DebugUIFragment())
        };
    }

    override fun initBottomBarTabs(): ArrayList<BaseBottomBarTab> {
        return ArrayList<BaseBottomBarTab>().apply {
            add(
                SvgaBottomBarTab(
                    context,
                    R.drawable.icon_camera,
                    R.drawable.icon_author,
                    "ic_voice.svga",
                    R.string.tab_1
                )
            )
            add(
                SvgaBottomBarTab(
                    context,
                    R.drawable.icon_author,
                    R.drawable.icon_camera,
                    "ic_voice.svga",
                    R.string.tab_2
                )
            )
            add(
                SvgaBottomBarTab(
                    context,
                    R.drawable.icon_feedback,
                    R.drawable.icon_cloud,
                    "ic_voice.svga",
                    R.string.tab_3
                )
            )
            add(
                SvgaBottomBarTab(
                    context,
                    R.drawable.icon_cloud,
                    R.drawable.icon_feedback,
                    "ic_voice.svga",
                    R.string.tab_4
                )
            )
            add(
                SvgaBottomBarTab(
                    context,
                    R.drawable.icon_cloud,
                    R.drawable.icon_feedback,
                    "ic_voice.svga",
                    R.string.tab_5
                )
            )
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
        }, 3)

    }

    override fun onLocaleChanged(lastLocale: Locale, toLanguageTag: Locale) {
        if (isRootViewCreated()) {
            bottomBarTabs.forEach {
                (it as? SvgaBottomBarTab)?.updateTabText()
            }
        }
        super.onLocaleChanged(lastLocale, toLanguageTag)
    }
}