package com.bihe0832.android.common.data.center

import com.bihe0832.android.common.coroutines.Coroutines_ERROR_DATA_NULL
import com.bihe0832.android.common.coroutines.ZixieCoroutinesException
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.log.ZLog
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 *
 * @author zixie code@bihe0832.com Created on 2022/3/25.
 *
 */
abstract class InfoCacheManager<T> {

    val TAG = "InfoCacheManager"

    abstract fun getRemoteData(key: String, listener: AAFDataCallback<T>)

    private val mDataList: ConcurrentHashMap<String, InfoItem<T>> = ConcurrentHashMap()

    inner class InfoItem<T>(
            var key: String = "",
            var updateTime: Long = 0L,
            var dataItem: T? = null
    )

    inner class ContinuationCallbackForFetchDataListener<T>(private var continuation: Continuation<T>) :
            AAFDataCallback<T>() {

        override fun onSuccess(result: T?) {
            if (result != null) {
                continuation.resume(result)
            } else {
                continuation.resumeWithException(
                        ZixieCoroutinesException(
                                Coroutines_ERROR_DATA_NULL,
                                ""
                        )
                )
            }
        }

        override fun onError(code: Int, msg: String) {
            continuation.resumeWithException(ZixieCoroutinesException(code, msg))
        }
    }


    suspend fun getData(key: String): T? = getData(key, -1)

    suspend fun getData(key: String, duration: Long): T? =
            suspendCoroutine { cont ->
                getData(key, duration, ContinuationCallbackForFetchDataListener(cont))
            }

    fun getData(key: String, listener: AAFDataCallback<T>) {
        getData(key, -1, listener)
    }

    fun getData(key: String, duration: Long, listener: AAFDataCallback<T>) {

        val mFetchDataListener = object : AAFDataCallback<T>() {
            override fun onSuccess(data: T?) {
                data?.let {
                    mDataList.put(key, InfoItem(key, System.currentTimeMillis(), it))
                }
                ZLog.d(TAG, "read $key data from server")
                listener.onSuccess(data)
            }

            override fun onError(errorCode: Int, msg: String) {
                listener.onError(errorCode, msg)
            }

        }

        if (mDataList.containsKey(key)) {
            mDataList[key].let {
                if (null != it) {
                    if (null != it.dataItem && duration > 0 && System.currentTimeMillis() - duration < it.updateTime) {
                        ZLog.d(TAG, "read $key data from cache")
                        listener.onSuccess(it.dataItem)
                    } else {
                        getRemoteData(key, mFetchDataListener)
                    }
                } else {
                    getRemoteData(key, mFetchDataListener)
                }
            }
        } else {
            getRemoteData(key, mFetchDataListener)
        }
    }
}