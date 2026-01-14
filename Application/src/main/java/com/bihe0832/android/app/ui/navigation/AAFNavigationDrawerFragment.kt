package com.bihe0832.android.app.ui.navigation

import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import com.bihe0832.android.common.main.CommonNavigationDrawerFragment

/**
 * AAF 侧边栏导航 Fragment
 *
 * 侧边栏抽屉导航的容器 Fragment，加载 AAFNavigationContentFragment 作为内容
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/4/10.
 */
class AAFNavigationDrawerFragment : CommonNavigationDrawerFragment() {

    /**
     * 加载导航内容 Fragment
     *
     * @param containerId Fragment 容器 ID
     */
    override fun loadFragment(containerId: Int) {
        loadRootFragment(containerId, AAFNavigationContentFragment())
    }

    /**
     * 设置抽屉导航
     *
     * @param fragmentContainerView Fragment 容器视图
     * @param drawerLayout 抽屉布局
     */
    override fun setUp(fragmentContainerView: View?, drawerLayout: DrawerLayout?) {
        super.setUp(fragmentContainerView, drawerLayout)
//        setDrawerLeftEdgeSize()
    }
}