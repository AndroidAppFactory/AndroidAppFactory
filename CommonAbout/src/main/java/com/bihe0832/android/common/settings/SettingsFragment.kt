package com.bihe0832.android.common.settings

import com.bihe0832.android.common.list.CardItemForCommonList
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.swiperefresh.CommonListFragment
import com.bihe0832.android.common.settings.card.PlaceholderData
import com.bihe0832.android.common.settings.card.SettingsDataGo
import com.bihe0832.android.common.settings.card.SettingsDataSwitch
import com.bihe0832.android.lib.adapter.CardBaseModule

open class SettingsFragment : CommonListFragment() {

    private val mSettingsLiveData = object : CommonListLiveData() {
        override fun initData() {
            postValue(getDataList(true))
        }

        override fun refresh() {
            postValue(getDataList(true))
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
            add(CardItemForCommonList(SettingsDataGo::class.java))
            add(CardItemForCommonList(SettingsDataSwitch::class.java))
        }
    }

    fun getSettingsDataPositionByTitle(title: String?): Int {
        var position: Int = -1
        if (!title.isNullOrEmpty()) {
            for (i in mAdapter.data.indices) {
                if (mAdapter.data[i] is SettingsDataGo && title == (mAdapter.data[i] as? SettingsDataGo)?.mItemText) {
                    position = i
                    break
                }
            }
        }
        return position
    }

    open fun getDataList(processLast: Boolean): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>()
    }

    fun List<CardBaseModule>.processLastItemDriver(processLast: Boolean) {
        try {
            if (processLast) {
                if (last() is SettingsDataGo) {
                    (last() as SettingsDataGo)?.mShowDriver = false
                } else if (last() is SettingsDataSwitch) {
                    (last() as SettingsDataSwitch)?.mShowDriver = false
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}
