package com.bihe0832.android.common.about

import android.view.View
import com.bihe0832.android.common.settings.SettingsFragment
import com.bihe0832.android.common.settings.card.SettingsDataGo
import com.bihe0832.android.framework.update.UpdateDataFromCloud
import com.bihe0832.android.framework.update.UpdateInfoLiveData
import com.bihe0832.android.lib.adapter.CardBaseHolder
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.model.res.R as ModelResR

open class AboutFragment : SettingsFragment() {

    override fun getDataList(processLast: Boolean): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>()
    }

    override fun initView(view: View) {
        super.initView(view)
        changeUpdateRedDot(UpdateInfoLiveData.value, true)
        UpdateInfoLiveData.observe(this) { data ->
            changeUpdateRedDot(data, true)
        }
    }

    open fun changeUpdateRedDot(cloud: UpdateDataFromCloud?, showTips: Boolean) {
        changeUpdateRedDot(ThemeResourcesManager.getString(ModelResR.string.settings_update_title), cloud, showTips)
    }

    open fun changeUpdateRedDot(title: String?, cloud: UpdateDataFromCloud?, showTips: Boolean) {
        val position = getSettingsDataPositionByTitle(title)
        if (cloud?.canShowNew() == true) {
            if (showTips) {
                updateItemRedDot(
                    position,
                    0,
                    ThemeResourcesManager.getString(ModelResR.string.settings_update_tips)
                        ?: "",
                )
            } else {
                updateItemRedDot(position, 0, "")
            }
        } else {
            updateItemRedDot(position, -1, "")
        }
    }

    open fun changeMessageRedDot(title: String?, num: Int) {
        val position = getSettingsDataPositionByTitle(title)
        if (num > 0) {
            updateItemRedDot(position, num, "")
        } else {
            updateItemRedDot(position, -1, "")
        }
    }

    open fun updateItemRedDot(position: Int, newNum: Int, tips: String) {
        if (position >= 0) {
            (mAdapter.data[position] as? SettingsDataGo)?.apply {
                mTipsText = tips
                mItemNewNum = newNum
            }?.let { newData ->
                mRecyclerView?.findViewHolderForAdapterPosition(position).let { viewHolder ->
                    (viewHolder as? CardBaseHolder)?.initData(newData)
                }
            }
        }
    }
}
