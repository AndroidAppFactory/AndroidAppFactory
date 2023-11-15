package com.bihe0832.android.common.crop.ui

import android.content.Context
import com.bihe0832.android.common.ui.bottom.bar.SimpleBottomBarTab

const val TAB_ID_ASPECT = 1
const val TAB_ID_SCALE = 2
const val TAB_ID_ROTATE = 3

class CropBottomTab(context: Context?, icon: Int, title: CharSequence?, private val tabId: Int) :
    SimpleBottomBarTab(context, icon, title) {
    fun getTabID(): Int {
        return tabId
    }
}
