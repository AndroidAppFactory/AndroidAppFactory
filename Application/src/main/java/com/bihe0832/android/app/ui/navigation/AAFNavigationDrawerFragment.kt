package com.bihe0832.android.app.ui.navigation

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
}