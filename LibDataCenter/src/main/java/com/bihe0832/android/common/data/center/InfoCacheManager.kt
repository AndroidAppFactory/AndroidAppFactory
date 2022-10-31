package com.bihe0832.android.common.data.center

import com.bihe0832.android.common.coroutines.Coroutines_ERROR_DATA_NULL
import com.bihe0832.android.common.coroutines.ZixieCoroutinesData
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.log.ZLog
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
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

    inner class ContinuationCallbackForFetchDataListener<T>(private var continuation: Continuation<ZixieCoroutinesData<T>>) :
        AAFDataCallback<T>() {

        override fun onSuccess(result: T?) {
            if (result != null) {
                continuation.resume(ZixieCoroutinesData(result))
            } else {
                continuation.resume(
                    ZixieCoroutinesData(
                        -1,
                        Coroutines_ERROR_DATA_NULL,
                        ""
                    )
                )
            }
        }

        override fun onError(code: Int, msg: String) {
            continuation.resume(
                ZixieCoroutinesData(
                    code,
                    code,
                    msg
                )
            )
        }
    }


    open suspend fun getData(key: String): ZixieCoroutinesData<T> = getData(key, -1)

    open suspend fun getData(key: String, duration: Long): ZixieCoroutinesData<T> =
        suspendCoroutine { cont ->
            getData(key, duration, ContinuationCallbackForFetchDataListener(cont))
        }

    fun getData(key: String, listener: AAFDataCallback<T>) {
        getData(key, -1, listener)
    }

    fun addData(key: String, data: T) {
        mDataList.put(key, InfoItem(key, System.currentTimeMillis(), data))
    }

    fun getData(key: String, duration: Long, listener: AAFDataCallback<T>) {

        val mFetchDataListener = object : AAFDataCallback<T>() {
            override fun onSuccess(data: T?) {
                data?.let {
                    addData(key, data)
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

    fun getCachedData(key: String): InfoItem<T>? {
        return mDataList.get(key)
    }
}