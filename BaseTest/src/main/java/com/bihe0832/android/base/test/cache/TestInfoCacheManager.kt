package com.bihe0832.android.base.test.cache

import com.bihe0832.android.common.coroutines.ZixieCoroutinesException
import com.bihe0832.android.common.data.center.InfoCacheManager
import com.bihe0832.android.lib.log.ZLog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author hardyshi code@bihe0832.com Created on 2022/3/25.
 */

object TestInfoCacheManager {
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default

    private val mTestInfoCacheManagerImpl = object : InfoCacheManager<Any?>() {
        override fun getRemoteData(key: String, listener: FetchDataListener<Any?>) {
            TestCacheData().apply {
                this.key = key
            }.let {
                listener.onSuccess(it)
            }
        }
    }

    private var key = 0
    private var add = true
    fun loggerData() {
        key = if (add) {
            key + 1
        } else {
            key - 1
        }
        if (key > 10) {
            add = false
        } else if (key < 0) {
            add = true
        }

        try {
            CoroutineScope(defaultDispatcher).launch {
                mTestInfoCacheManagerImpl.getData(key.toString(), 15000L).let {
                    ZLog.d("hardy", it.toString())
                }
            }
        } catch (e: ZixieCoroutinesException) {
            e.printStackTrace()
            ZLog.d("hardy", e.toString())
        }

    }
}
