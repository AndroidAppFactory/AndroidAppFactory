package com.bihe0832.android.app.about

import android.view.View
import com.bihe0832.android.app.update.UpdateManager
import com.bihe0832.android.common.settings.SettingsItem
import com.bihe0832.android.lib.adapter.CardBaseModule

open class AboutFragment : com.bihe0832.android.common.about.AboutFragment() {

    open override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(SettingsItem.getUpdate(activity, View.OnClickListener {
                activity?.let {
                    UpdateManager.checkUpdateAndShowDialog(it, true)
                }
            }))
            addAll(super.getDataList())


        }
    }
}