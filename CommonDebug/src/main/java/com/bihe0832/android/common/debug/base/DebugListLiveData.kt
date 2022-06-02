package com.bihe0832.android.common.debug.base

import com.bihe0832.android.common.list.CommonListLiveData

/**
 *
 * @author hardyshi code@bihe0832.com Created on 2020/12/1.
 *
 */
abstract class DebugListLiveData : CommonListLiveData() {

    override fun clearData() {
        postValue(ArrayList())
    }

    override fun loadMore() {

    }

    override fun hasMore(): Boolean {
        return false
    }

    override fun canRefresh(): Boolean {
        return false
    }

    override fun getEmptyText(): String {
        return "当前数据为空，请稍候再试"
    }
}