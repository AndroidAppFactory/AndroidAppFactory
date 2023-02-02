package com.bihe0832.android.base.debug.navigation

import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.navigation.drawer.NavigationDrawerFragment

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/10/8.
 * Description: Description
 *
 */
class DebugNavigationDrawerFragment : NavigationDrawerFragment() {


    override fun getLayoutID(): Int {
        return R.layout.debug_fragment_navigation_drawer
    }

    override fun setUp(fragmentContainerView: View, drawerLayout: DrawerLayout) {
        super.setUp(fragmentContainerView, drawerLayout)
    }

    private val listHeadFragment by lazy {
        DebugNavigationItemList()
    }

    private val listBodyFragment by lazy {
        DebugNavigationItemList()
    }

    override fun initView(view: View) {
        super.initView(view)
        loadRootFragment(R.id.test_head, listHeadFragment)
        loadRootFragment(R.id.test_body, listBodyFragment)
    }

}