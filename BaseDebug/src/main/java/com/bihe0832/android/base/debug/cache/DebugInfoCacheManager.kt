package com.bihe0832.android.base.debug.cache

import com.bihe0832.android.common.data.center.InfoCacheManager
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.ConvertUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author zixie code@bihe0832.com Created on 2022/3/25.
 */

object DebugInfoCacheManager {
    val LOG_TAG = "DebugInfoCacheManager"

    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default

    private val mTestInfoCacheManagerImpl = object : InfoCacheManager<DebugCacheData>() {
        override fun getRemoteData(key: String, listener: AAFDataCallback<DebugCacheData>) {
            if (ConvertUtils.parseInt(key, 0) % 2 == 0) {
                DebugCacheData().apply {
                    this.key = key
                }.let {
                    listener.onSuccess(it)
                }
            } else {
                listener.onError(1, "onError $key")
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
                    ZLog.d(LOG_TAG, it.toString())
                }

                it.onError { errCode, msg, exception ->
                    ZLog.d(LOG_TAG, "Error: $errCode, $msg, $exception")
                }

            }

            mTestInfoCacheManagerImpl.getData(key.toString(), 15000L).onSuccess {
                ZLog.d(LOG_TAG, it.toString())
            }.onError { errCode, msg, exception ->
                ZLog.d(LOG_TAG, "Error: $errCode, $msg, $exception")
            }

            mTestInfoCacheManagerImpl.getData(key.toString(), 15000L).onSuccess()?.let {
                ZLog.d(LOG_TAG, it.toString())
            }

            mTestInfoCacheManagerImpl.getData(key.toString(), 15000L).onError()?.let {
                ZLog.d(LOG_TAG, it.toString())
            }
        }
    }
}
