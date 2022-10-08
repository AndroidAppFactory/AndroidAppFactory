package com.bihe0832.android.base.debug.navigation

import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import com.bihe0832.android.app.about.AboutFragment
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.navigation.drawer.NavigationDrawerFragment

/**
 *
 * @author hardyshi code@bihe0832.com
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

    private val listFragment by lazy {
        AboutFragment()
    }

    override fun initView(view: View) {
        super.initView(view)
        if (findFragment(AboutFragment::class.java) == null) {
            loadRootFragment(R.id.test_id, listFragment)
        }
    }

}