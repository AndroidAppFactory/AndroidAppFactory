package com.bihe0832.android.base.compose.debug.cache;

import com.bihe0832.android.common.coroutines.Coroutines_ERROR_DATA_NULL
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.data.repository.InfoCacheManager
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.utils.ConvertUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @author zixie code@bihe0832.com Created on 2022/3/25.
 */

object DebugInfoCacheManager {
    val LOG_TAG = this.javaClass.simpleName

    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default

    private class AAFInfoCacheManager : InfoCacheManager<DebugCacheData>() {
        override fun getDefaultDuration(key: String): Long {
            return 10
        }

        override fun getBestLength(): Int {
            return 180
        }

        override fun getRemoteData(key: String, listener: AAFDataCallback<DebugCacheData>) {
            when (ConvertUtils.parseInt(key, 0) % 3) {
                0 -> {
                    DebugCacheData().apply {
                        this.key = key
                    }.let {
                        listener.onSuccess(it)
                    }
                }
                1 -> {
                    listener.onError(1, "onError $key")

                }

                2 -> {
                    listener.onError(2, "onError $key")

                }

            }
        }

        override suspend fun getData(
                key: String,
                duration: Long
        ): DebugCoroutinesData<DebugCacheData> =
            suspendCoroutine { cont ->
                getData(key, duration, ContinuationCallbackForFetchDataListener(cont))
            }


        inner class ContinuationCallbackForFetchDataListener(private var continuation: Continuation<DebugCoroutinesData<DebugCacheData>>) :
                AAFDataCallback<DebugCacheData>() {

            override fun onSuccess(result: DebugCacheData?) {
                if (result != null) {
                    continuation.resume(DebugCoroutinesData(result))
                } else {
                    continuation.resume(
                            DebugCoroutinesData(
                                    0,
                                    Coroutines_ERROR_DATA_NULL,
                                    ""
                            )
                    )
                }
            }

            override fun onError(code: Int, msg: String) {
                continuation.resume(
                        DebugCoroutinesData(
                                code,
                                code,
                                msg
                        )
                )
            }
        }

    }

    private val mTestInfoCacheManagerImpl = AAFInfoCacheManager()

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
        ZLog.d(LOG_TAG, "key:$key,add:$add")
        CoroutineScope(defaultDispatcher).launch {

            addData("TestCache", DebugCacheData().apply {
                this.key = "TestCache" + System.currentTimeMillis()
            })
            mTestInfoCacheManagerImpl.getData("TestCache", 500L * key).let {
                it.onSuccess {
                    ZLog.d(LOG_TAG, it.toString())
                }

                it.onAAFError { errCode, exception ->
                    ZLog.d(LOG_TAG, "onZixieError: $errCode,  $exception")

                }

                it.onAAFLoginError { errCode, exception ->
                    ZLog.d(LOG_TAG, "onZixieLoginError: $errCode,  $exception")
                }
            }
            //需要同时处理各种异常的情况
            mTestInfoCacheManagerImpl.getData(key.toString(), 15000L).let {
                it.onSuccess {
                    ZLog.d(LOG_TAG, it.toString())
                }

                it.onAAFError { errCode, exception ->
                    ZLog.d(LOG_TAG, "onZixieError: $errCode,  $exception")

                }

                it.onAAFLoginError { errCode, exception ->
                    ZLog.d(LOG_TAG, "onZixieLoginError: $errCode,  $exception")
                }
            }


            //需要同时处理各种异常的情况，链式调用
            mTestInfoCacheManagerImpl.getData(key.toString(), 15000L)
                    .onAAFError { errCode, exception ->
                        ZLog.d(LOG_TAG, "onZixieError: $errCode,  $exception")

                    }.onAAFLoginError { errCode, exception ->
                        ZLog.d(LOG_TAG, "onZixieLoginError: $errCode,  $exception")

                    }.onSuccess {
                        ZLog.d(LOG_TAG, it.toString())
                    }.data()

            //请求成功，直接获取到数据
            mTestInfoCacheManagerImpl.getData(key.toString(), 15000L).data()?.let {
                ZLog.d(LOG_TAG, it.toString())
            }

            //请求异常，对应的错误信息
            mTestInfoCacheManagerImpl.getData(key.toString(), 15000L).error()?.let {
                ZLog.d(LOG_TAG, it.toString())
            }

            mTestInfoCacheManagerImpl.getCachedData(key.toString()).let {
                ZLog.d(LOG_TAG, it.toString())
            }
        }
    }

    fun addData(key: String, cacheData: DebugCacheData) {
        mTestInfoCacheManagerImpl.addData(key, cacheData)
    }
}
