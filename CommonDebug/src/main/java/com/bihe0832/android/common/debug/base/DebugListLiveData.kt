package com.bihe0832.android.common.debug.base

import com.bihe0832.android.common.list.CommonListLiveData

/**
 *
 * @author zixie code@bihe0832.com Created on 2020/12/1.
 *
 */
abstract class DebugListLiveData : CommonListLiveData() {

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