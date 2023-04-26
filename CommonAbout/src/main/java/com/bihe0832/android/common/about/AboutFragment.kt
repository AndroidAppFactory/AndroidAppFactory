package com.bihe0832.android.common.about

import android.view.View
import com.bihe0832.android.common.settings.SettingsFragment
import com.bihe0832.android.common.settings.SettingsItem
import com.bihe0832.android.common.settings.card.SettingsData
import com.bihe0832.android.framework.update.UpdateDataFromCloud
import com.bihe0832.android.framework.update.UpdateInfoLiveData
import com.bihe0832.android.lib.adapter.CardBaseHolder
import com.bihe0832.android.lib.adapter.CardBaseModule


open class AboutFragment : SettingsFragment() {

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(SettingsItem.getVersionList())
            add(SettingsItem.getFeedback())
            add(SettingsItem.getQQService(activity))
        }.apply {
            processLastItemDriver()
        }
    }

    override fun initView(view: View) {
        super.initView(view)
        updateRedPoint(UpdateInfoLiveData.value)
        UpdateInfoLiveData.observe(this) { data ->
            updateRedPoint(data)
        }
    }

    open fun updateRedPoint(cloud: UpdateDataFromCloud?) {
        var position = getSettingsDataPositionByTitle(context?.resources?.getString(R.string.settings_update_title))
        updateRedPoint(position, cloud)
    }

    open fun updateRedPoint(position: Int, cloud: UpdateDataFromCloud?) {
        if (position >= 0) {
            (mAdapter.data[position] as? SettingsData)?.apply {
                if (null != cloud && cloud.canShowNew()) {
                    mTipsText = context?.resources?.getString(R.string.settings_update_tips) ?: ""
                    mItemIsNew = true
                } else {
                    mTipsText = ""
                    mItemIsNew = false
                }
            }?.let { newData ->
                mRecyclerView?.findViewHolderForAdapterPosition(position).let { viewHolder ->
                    (viewHolder as? CardBaseHolder)?.initData(newData)
                }
            }
        }
    }
}