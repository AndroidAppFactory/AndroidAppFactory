package com.bihe0832.android.common.main

import android.view.View
import com.bihe0832.android.common.navigation.drawer.NavigationDrawerFragment
import com.bihe0832.android.common.navigation.drawer.R

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/10/8.
 * Description: Description
 *
 */
open class CommonNavigationDrawerFragment : NavigationDrawerFragment() {

    override fun getLayoutID(): Int {
        return R.layout.com_bihe0832_navigation_drawer
    }

    override fun initView(view: View) {
        super.initView(view)
        loadFragment(R.id.navigation_drawer_content)
    }

    open fun loadFragment(containerId: Int) {
        loadRootFragment(containerId, CommonNavigationContentFragment())
    }
}