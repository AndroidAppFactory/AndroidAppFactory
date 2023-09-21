package com.bihe0832.android.app.about

import android.app.Activity
import android.view.View
import com.bihe0832.android.app.update.UpdateManager
import com.bihe0832.android.common.settings.SettingsItem
import com.bihe0832.android.common.settings.card.SettingsData
import com.bihe0832.android.framework.update.UpdateInfoLiveData
import com.bihe0832.android.lib.adapter.CardBaseModule

open class AboutFragment : com.bihe0832.android.common.about.AboutFragment() {

    override fun getDataList(processLast: Boolean): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(
                SettingsItem.getUpdate(
                    UpdateInfoLiveData.value,
                    View.OnClickListener {
                        activity?.let {
                            UpdateManager.checkUpdateAndShowDialog(
                                it,
                                checkUpdateByUser = true,
                                showIfNeedUpdate = true,
                            )
                        }
                    },
                ),
            )
            add(SettingsItem.getVersionList())
            add(getFeedbackItem(activity))
        }.apply {
            processLastItemDriver(processLast)
        }
    }
}

fun getFeedbackItem(activity: Activity?): SettingsData {
    return SettingsItem.getFeedbackURL()
}
