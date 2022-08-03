package com.bihe0832.android.common.about

import android.arch.lifecycle.Observer
import android.view.View
import com.bihe0832.android.common.about.card.SettingsData
import com.bihe0832.android.common.list.CardItemForCommonList
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.common.list.swiperefresh.CommonListFragment
import com.bihe0832.android.common.settings.SettingsItem
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.update.UpdateDataFromCloud
import com.bihe0832.android.framework.update.UpdateInfoLiveData
import com.bihe0832.android.lib.adapter.CardBaseModule

/**
 * 如果有更新，第一个Item一定要是更新，否则会导致UI显示异常
 */
open class AboutFragment : CommonListFragment() {


    open fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(SettingsItem.getVersionList())
            add(SettingsItem.getFeedback())
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
        if (mDataList.size > 0) {
            (mDataList[0] as SettingsData).apply {
                if (null != cloud && cloud.updateType > UpdateDataFromCloud.UPDATE_TYPE_HAS_NEW_JUMP) {
                    mTipsText = "发现新版本"
                    mItemIsNew = true
                } else {
                    mTipsText = ""
                    mItemIsNew = false
                }
            }
            getAdapter().notifyDataSetChanged()
        }
    }


    override fun getDataLiveData(): CommonListLiveData {
        return object : CommonListLiveData() {
            override fun fetchData() {
                postValue(mDataList)
            }

            override fun clearData() {

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