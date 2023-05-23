package com.bihe0832.android.app.ui.navigation

import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import com.bihe0832.android.common.main.CommonNavigationDrawerFragment

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2023/4/10.
 * Description: Description
 *
 */
class AAFNavigationDrawerFragment : CommonNavigationDrawerFragment() {

    override fun loadFragment(containerId: Int) {
        loadRootFragment(containerId, AAFNavigationContentFragment())
    }

    override fun setUp(fragmentContainerView: View?, drawerLayout: DrawerLayout?) {
        super.setUp(fragmentContainerView, drawerLayout)
//        setDrawerLeftEdgeSize()
    }
}