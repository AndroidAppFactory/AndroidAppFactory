package com.bihe0832.android.base.debug.navigation

import com.bihe0832.android.app.about.AboutFragment
import com.bihe0832.android.base.debug.R

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2022/10/8.
 * Description: Description
 *
 */
class DebugNavigationItemList : AboutFragment() {

    override fun getLayoutID(): Int {
        return R.layout.debug_navigation_list
    }

}