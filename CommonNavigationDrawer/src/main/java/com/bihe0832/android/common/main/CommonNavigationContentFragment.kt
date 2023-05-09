package com.bihe0832.android.common.main

import android.view.View
import com.bihe0832.android.common.about.AboutFragment
import com.bihe0832.android.common.settings.SettingsItem
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.theme.ThemeChangedLiveData

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/10/8.
 * Description: Description
 *
 */
open class CommonNavigationContentFragment : AboutFragment() {

    override fun initView(view: View) {
        super.initView(view)
        ThemeChangedLiveData.observe(this) {
            mDataLiveData.refresh()
        }
    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(SettingsItem.getFeedback())
            add(SettingsItem.getShareAPP())
            add(SettingsItem.getVersionList())
            add(SettingsItem.getZixie())

            if (!ZixieContext.isOfficial()) {
                add(SettingsItem.getDebug())
            }
        }.apply {
            processLastItemDriver()
        }
    }
}