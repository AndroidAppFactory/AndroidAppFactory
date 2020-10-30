package com.bihe0832.android.lib.lifecycle

import android.app.ActivityManager
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.os.Process
import com.bihe0832.android.lib.log.ZLog

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2020/5/19.
 * Description: Description
 */
object ApplicationObserver : LifecycleObserver {

    private const val TAG = "ApplicationObserver"
    private var mLastPauseTime = 0L
    private var mLastResumeTime = 0L
    private var mIsAPPBackground = true

    interface APPStatusChangeListener{
        fun onForeground()
        fun onBackground()
    }

    private val mAPPStatusChangeListenerList = mutableListOf<APPStatusChangeListener>()

    fun addStatusChangeListener(listener: APPStatusChangeListener){
        mAPPStatusChangeListenerList.add(listener)
    }

    fun removeStatusChangeListener(listener: APPStatusChangeListener){
        mAPPStatusChangeListenerList.remove(listener)
    }

    fun isAPPBackground(): Boolean {
        return mIsAPPBackground
    }

    fun getLastPauseTime(): Long {
        (LifecycleHelper.applicationContext!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).runningAppProcesses?.find { it.pid ==  Process.myPid()}.let {
            if(it?.processName.equals(LifecycleHelper.applicationContext!!.packageName)){
                return mLastPauseTime
            }
        }
        return System.currentTimeMillis()
    }

    fun getLastResumeTime(): Long {
        return mLastResumeTime
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
}