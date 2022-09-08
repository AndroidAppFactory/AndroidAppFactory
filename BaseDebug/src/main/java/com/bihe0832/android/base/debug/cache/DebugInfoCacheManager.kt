package com.bihe0832.android.base.debug.cache

import com.bihe0832.android.common.data.center.InfoCacheManager
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.log.ZLog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author zixie code@bihe0832.com Created on 2022/3/25.
 */

object DebugInfoCacheManager {
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default

    private val mTestInfoCacheManagerImpl = object : InfoCacheManager<DebugCacheData>() {
        override fun getRemoteData(key: String, listener: AAFDataCallback<DebugCacheData>) {
            DebugCacheData().apply {
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
        CoroutineScope(defaultDispatcher).launch {
            mTestInfoCacheManagerImpl.getData(key.toString(), 15000L).let {
                it.onSuccess {
                    ZLog.d("zixie", it.toString())
                }

                it.onError { errCode, msg, exception ->
                    ZLog.d("zixie", "$errCode, $msg, $exception")
                }
            }
        }
    }
}
