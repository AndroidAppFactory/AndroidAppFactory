package com.bihe0832.android.common.about

import android.view.View
import androidx.lifecycle.Observer
import com.bihe0832.android.common.about.card.SettingsData
import com.bihe0832.android.common.list.CardItemForCommonList
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.swiperefresh.CommonListFragment
import com.bihe0832.android.common.settings.SettingsItem
import com.bihe0832.android.framework.update.UpdateDataFromCloud
import com.bihe0832.android.framework.update.UpdateInfoLiveData
import com.bihe0832.android.lib.adapter.CardBaseHolder
import com.bihe0832.android.lib.adapter.CardBaseModule


open class AboutFragment : CommonListFragment() {

    open fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(SettingsItem.getVersionList())
            add(SettingsItem.getFeedback())
            add(SettingsItem.getQQService(activity))
        }
    }

    override fun initView(view: View) {
        super.initView(view)
        updateRedPoint(UpdateInfoLiveData.value)
        UpdateInfoLiveData.observe(this, Observer<UpdateDataFromCloud> { data ->
            updateRedPoint(data)
        })
    }

    fun getSettingsDataByTitle(title: String?): Int {
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

    open fun updateRedPoint(cloud: UpdateDataFromCloud?) {
        var position = getSettingsDataByTitle(context?.resources?.getString(R.string.settings_update_title))
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

    override fun getDataLiveData(): CommonListLiveData {
        return object : CommonListLiveData() {
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
    }


    override fun getCardList(): List<CardItemForCommonList>? {
        return mutableListOf(
                CardItemForCommonList(SettingsData::class.java)
        )
    }
}