package com.bihe0832.android.lib.data.repository

import com.bihe0832.android.common.coroutines.AAFCoroutinesData
import com.bihe0832.android.common.coroutines.AAFDataCallbackToAAFCoroutinesData
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.log.ZLog
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.suspendCoroutine

/**
 *
 * @author zixie code@bihe0832.com Created on 2022/3/25.
 *
 */
abstract class InfoCacheManager<T> {

    val TAG = "InfoCacheManager"

    // 默认的cache 数据缓存时间
    val DEFAULT_DURATION = 1000 * 60 * 30L

    // cache 满了以后，单次清除的内容量
    val DEFAULT_PART = 20

    abstract fun getRemoteData(key: String, listener: AAFDataCallback<T>)

    open fun getDefaultDuration(key: String): Long {
        return DEFAULT_DURATION
    }

    open fun getBestLength(): Int {
        return 3000
    }

    private val mDataMap: ConcurrentHashMap<String, InfoItem<T>> = ConcurrentHashMap()

    inner class InfoItem<T>(
            //数据key
            var key: String = "",
            //数据插入或者更新时间
            var initTime: Long = 0L,
            // 数据内容
            var dataItem: T? = null)


    private fun getRealBestLength(): Int {
        getBestLength().let {
            if (it > DEFAULT_PART) {
                return it
            } else {
                return DEFAULT_PART
            }
        }
    }

    fun addDataToRepository(key: String, data: T) {
        ZLog.d(TAG, "add data：$key ")
        mDataMap[key] = InfoItem(key, System.currentTimeMillis(), data)
        if (mDataMap.size > getRealBestLength()) {
            var dataIterator = mDataMap.entries.iterator()
            var num = 0
            var startCheckTime = System.currentTimeMillis()
            while (dataIterator.hasNext() && num < getRealBestLength() / DEFAULT_PART) {
                dataIterator.next().let {
                    var time = it.value.initTime
                    if (time < startCheckTime - getDefaultDuration(key)) {
                        ZLog.d(TAG, "remove ${it.key} data by length")
                        dataIterator.remove()
                        num++
                    }
                }
            }
            ZLog.d(TAG, "after remove data length is：${mDataMap.size} ")
        }
    }

    open fun addData(key: String, data: T) {
        addDataToRepository(key, data)
    }

    fun forceUpdate(key: String, forceResetWhenFailed: Boolean) {
        getData(key, -1, object : AAFDataCallback<T>() {
            override fun onSuccess(result: T?) {

            }

            override fun onError(errorCode: Int, msg: String) {
                super.onError(errorCode, msg)
                if (forceResetWhenFailed) {
                    removeData(key)
                }
            }
        })
    }

    fun removeData(key: String) {
        mDataMap.remove(key)
    }


    open suspend fun getNewData(key: String): AAFCoroutinesData<T> = getData(key, -1)

    open suspend fun getData(key: String, duration: Long): AAFCoroutinesData<T> = suspendCoroutine { cont ->
        getData(key, duration, AAFDataCallbackToAAFCoroutinesData(cont))
    }

    open suspend fun getCachedData(key: String): AAFCoroutinesData<T> {
        return getData(key, getDefaultDuration(key))
    }

    fun getNewData(key: String, listener: AAFDataCallback<T>) {
        getData(key, -1, listener)
    }

    fun getData(key: String, duration: Long, listener: AAFDataCallback<T>?) {

        val mFetchDataListener = object : AAFDataCallback<T>() {
            override fun onSuccess(data: T?) {
                data?.let {
                    addData(key, data)
                }
                ZLog.d(TAG, "read $key data from server")
                listener?.onSuccess(data)
            }

            override fun onError(errorCode: Int, msg: String) {
                listener?.onError(errorCode, msg)
            }

        }

        if (duration > 0 && mDataMap.containsKey(key)) {
            mDataMap[key].let {
                if (null != it) {
                    if (null != it.dataItem && System.currentTimeMillis() - duration < it.initTime) {
                        ZLog.d(TAG, "read $key data from cache")
                        listener?.onSuccess(it.dataItem)
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

    fun getCachedData(key: String, listener: AAFDataCallback<T>?) {
        return getData(key, getDefaultDuration(key), listener)
    }

    /**
     * 彻底不看过期时间获取数据
     */
    fun getSourceData(key: String): InfoItem<T>? {
        return mDataMap.get(key)
    }


}