package com.bihe0832.android.lib.lifecycle

import android.app.ActivityManager
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.os.Process
import com.bihe0832.android.lib.log.ZLog

/**
 * @author zixie code@bihe0832.com
 * Created on 2020/5/19.
 * Description: Description
 */
object ApplicationObserver : LifecycleObserver {

    private const val TAG = "ApplicationObserver"
    private var mAPPStartTime = 0L
    private var mLastPauseTime = 0L
    private var mLastResumeTime = 0L
    private var mIsAPPBackground = true
    private val mAPPStatusChangeListenerList = mutableListOf<APPStatusChangeListener>()
    private val mAPPDestroyChangeListenerList = mutableListOf<APPDestroyListener>()


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
        (LifecycleHelper.applicationContext!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).runningAppProcesses?.find { it.pid == Process.myPid() }
            .let {
                if (it?.processName.equals(LifecycleHelper.applicationContext!!.packageName)) {
                    return mLastPauseTime
                }
            }
        return System.currentTimeMillis()
    }

    fun getLastResumedTime(): Long {
        return mLastResumeTime
    }

    fun getAPPStartTime(): Long {
        return mAPPStartTime
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onForeground() {
        ZLog.d(TAG, "onForeground!")
        mIsAPPBackground = false
        mLastResumeTime = System.currentTimeMillis()
        mAPPStatusChangeListenerList.forEach {
            it.onForeground()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onBackground() {
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