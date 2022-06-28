package com.bihe0832.android.common.debug.base

import com.bihe0832.android.common.debug.R
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.framework.ZixieContext

/**
 *
 * @author zixie code@bihe0832.com Created on 2020/12/1.
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
        return ZixieContext.applicationContext?.getString(R.string.common_debug_empty_tips) ?: ""
    }
}