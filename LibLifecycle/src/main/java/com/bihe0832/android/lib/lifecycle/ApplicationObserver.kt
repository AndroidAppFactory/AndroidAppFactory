package com.bihe0832.android.lib.lifecycle

import androidx.lifecycle.*
import com.bihe0832.android.lib.log.ZLog
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author zixie code@bihe0832.com
 * Created on 2020/5/19.
 * Description: Description
 */
object ApplicationObserver : DefaultLifecycleObserver {

    private const val TAG = "ApplicationObserver"
    private var mAPPStartTime = 0L
    private var mLastPauseTime = 0L
    private var mLastResumeTime = 0L
    private var mIsAPPBackground = true
    private val mAPPStatusChangeListenerList = CopyOnWriteArrayList<APPStatusChangeListener>()
    private val mAPPDestroyChangeListenerList = CopyOnWriteArrayList<APPDestroyListener>()


    interface APPStatusChangeListener {
        fun onForeground()
        fun onBackground()
    }

    interface APPDestroyListener {
        fun onAllActivityDestroyed()
    }

    init {
        mAPPStartTime = System.currentTimeMillis()
    }

    fun addStatusChangeListener(listener: APPStatusChangeListener) {
        if (!mAPPStatusChangeListenerList.contains(listener)) {
            mAPPStatusChangeListenerList.add(listener)
        }
    }

    fun removeStatusChangeListener(listener: APPStatusChangeListener) {
        if (mAPPStatusChangeListenerList.contains(listener)) {
            mAPPStatusChangeListenerList.remove(listener)
        }
    }

    fun addDestoryListener(listener: APPDestroyListener) {
        if (!mAPPDestroyChangeListenerList.contains(listener)) {
            mAPPDestroyChangeListenerList.add(listener)
        }

    }

    fun removeDestoryListener(listener: APPDestroyListener) {
        if (mAPPDestroyChangeListenerList.contains(listener)) {
            mAPPDestroyChangeListenerList.remove(listener)
        }
    }

    fun isAPPBackground(): Boolean {
        return mIsAPPBackground
    }

    fun getLastPauseTime(): Long {
        return mLastPauseTime
    }

    fun getLastResumedTime(): Long {
        return mLastResumeTime
    }

    fun getAPPStartTime(): Long {
        return mAPPStartTime
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        ZLog.d(TAG, "onForeground!")
        mIsAPPBackground = false
        mLastResumeTime = System.currentTimeMillis()
        mAPPStatusChangeListenerList.forEach {
            it.onForeground()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        ZLog.d(TAG, "onBackground!")
        mLastPauseTime = System.currentTimeMillis()
        mIsAPPBackground = true
        mAPPStatusChangeListenerList.forEach {
            it.onBackground()
        }
    }


    fun onAllActivityDestroyed() {
        ZLog.d(TAG, "onAllActivityDestroy!")
        mAPPDestroyChangeListenerList.forEach {
            it.onAllActivityDestroyed()
        }
    }
}