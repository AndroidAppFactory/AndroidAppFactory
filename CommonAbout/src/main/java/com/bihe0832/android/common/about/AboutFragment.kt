package com.bihe0832.android.common.about

import android.view.View
import androidx.lifecycle.Observer
import com.bihe0832.android.common.about.card.SettingsData
import com.bihe0832.android.common.list.CardItemForCommonList
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.swiperefresh.CommonListFragment
import com.bihe0832.android.common.settings.SettingsItem
import com.bihe0832.android.framework.ZixieContext
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
            add(SettingsItem.getZixie())
            if (!ZixieContext.isOfficial()) {
                add(SettingsItem.getDebug())
            }
        }
    }

    val mDataList by lazy {
        ArrayList<CardBaseModule>().apply {
            addAll(getDataList())
        }
    }

    override fun initView(view: View) {
        super.initView(view)
        updateRedPoint(UpdateInfoLiveData.value)
        UpdateInfoLiveData.observe(this, Observer<UpdateDataFromCloud> { data ->
            updateRedPoint(data)
        })
    }

    open fun updateRedPoint(cloud: UpdateDataFromCloud?) {
        var position: Int = -1
        val title = context?.resources?.getString(R.string.settings_update_title)
        for (i in mAdapter.data.indices) {
            if (mAdapter.data[i] is SettingsData && title == (mAdapter.data[i] as? SettingsData)?.mItemText) {
                position = i
                break
            }
        }

        if (position >= 0) {
            (mAdapter.data[position] as? SettingsData)?.apply {
                if (null != cloud && cloud.updateType > UpdateDataFromCloud.UPDATE_TYPE_HAS_NEW_JUMP) {
                    mTipsText = context?.resources?.getString(R.string.settings_update_tips)
                            ?: ""
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
                postValue(mDataList)
            }

            override fun refresh() {

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