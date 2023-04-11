package com.bihe0832.android.common.main

import com.bihe0832.android.common.about.AboutFragment
import com.bihe0832.android.common.about.card.SettingsData
import com.bihe0832.android.common.settings.SettingsItem
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.update.UpdateDataFromCloud
import com.bihe0832.android.lib.adapter.CardBaseHolder
import com.bihe0832.android.lib.adapter.CardBaseModule

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/10/8.
 * Description: Description
 *
 */
open class CommonNavigationContentFragment : AboutFragment() {

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(SettingsItem.getFeedback())
            add(SettingsItem.getShareAPP())
            add(SettingsItem.getVersionList())
            add(SettingsItem.getZixie())
            if (!ZixieContext.isOfficial()) {
                add(SettingsItem.getDebug())
            }
        }
    }

    override fun updateRedPoint(cloud: UpdateDataFromCloud?) {
        var position = getSettingsDataByTitle(SettingsItem.getAboutTitle(ZixieContext.applicationContext!!))
        updateRedPoint(position, cloud)
    }

    override fun updateRedPoint(position: Int, cloud: UpdateDataFromCloud?) {
        if (position >= 0) {
            (mAdapter.data[position] as? SettingsData)?.apply {
                mItemIsNew = cloud?.canShowNew() ?: false
            }?.let { newData ->
                mRecyclerView?.findViewHolderForAdapterPosition(position).let { viewHolder ->
                    (viewHolder as? CardBaseHolder)?.initData(newData)
                }
            }
        }
    }

}