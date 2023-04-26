package com.bihe0832.android.common.settings

import com.bihe0832.android.common.list.CardItemForCommonList
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.swiperefresh.CommonListFragment
import com.bihe0832.android.common.settings.card.PlaceholderData
import com.bihe0832.android.common.settings.card.SettingsData
import com.bihe0832.android.common.settings.card.SettingsDataV2
import com.bihe0832.android.lib.adapter.CardBaseModule


open class SettingsFragment : CommonListFragment() {

    private val mSettingsLiveData = object : CommonListLiveData() {
        override fun initData() {
            postValue(getDataList())
        }

        override fun refresh() {
            postValue(getDataList())
        }

        override fun loadMore() {

        }

        override fun hasMore(): Boolean {
            return false
        }

        override fun canRefresh(): Boolean {
            return false
        }
    }

    override fun getDataLiveData(): CommonListLiveData {
        return mSettingsLiveData
    }

    override fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf<CardItemForCommonList>().apply {
            add(CardItemForCommonList(PlaceholderData::class.java))
            add(CardItemForCommonList(SettingsData::class.java))
            add(CardItemForCommonList(SettingsDataV2::class.java))
        }
    }

    fun getSettingsDataPositionByTitle(title: String?): Int {
        var position: Int = -1
        if (!title.isNullOrEmpty()) {
            for (i in mAdapter.data.indices) {
                if (mAdapter.data[i] is SettingsData && title == (mAdapter.data[i] as? SettingsData)?.mItemText) {
                    position = i
                    break
                }
            }
        }
        return position
    }

    open fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(SettingsItem.getVersionList())
            add(SettingsItem.getFeedback())
            add(SettingsItem.getQQService(activity))
        }.apply {
            processLastItemDriver()
        }
    }

    fun List<CardBaseModule>.processLastItemDriver() {
        try {
            if (last() is SettingsData) {
                (last() as SettingsData)?.mShowDriver = false
            } else if (last() is SettingsDataV2) {
                (last() as SettingsDataV2)?.mShowDriver = false
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}