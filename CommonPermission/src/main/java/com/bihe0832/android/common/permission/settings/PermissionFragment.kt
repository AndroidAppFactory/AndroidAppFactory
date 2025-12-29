package com.bihe0832.android.common.permission.settings

import android.Manifest
import com.bihe0832.android.common.settings.SettingsFragment
import com.bihe0832.android.common.settings.card.PlaceholderData
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.aaf.res.R as ResR

open class PermissionFragment : SettingsFragment() {

    override fun getDataList(processLast: Boolean): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(PermissionItem.getRecommandSetting(context!!))
            add(PlaceholderData(context!!, 4f, ResR.color.md_theme_outline))
            add(PermissionItem.getPermissionSetting(activity!!, Manifest.permission.CAMERA))
        }.apply {
            processLastItemDriver(processLast)
        }
    }
}
