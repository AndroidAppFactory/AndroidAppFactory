package com.bihe0832.android.lib.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.bihe0832.android.lib.log.ZLog
import java.lang.ref.SoftReference

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2020/5/19.
 * Description: Description
 */
object ActivityObserver : Application.ActivityLifecycleCallbacks {
    private const val TAG = "ActivityObserver"

    private val mActivityList = mutableListOf<Activity>()
    private var mSoftReferenceOfCurrentActivity: SoftReference<Activity>? = null
    private var mSoftReferenceOfResumedActivity: SoftReference<Activity>? = null


    /**
     * 返回最后一次创建的Activity
     */
    fun getCurrentActivity(): Activity? {
        return mSoftReferenceOfCurrentActivity?.get()
    }

    /**
     * 返回当前在前台的Activity
     *
     * 他与 getCurrentActivity 的区别在于：如果当前Activity 为 A, 新创建一个Activity为 B，在
     *  B onResume 之前，getCurrentActivity 为 B，getCurrentResumedActivity 为 A
     *  B onResume 之后，getCurrentActivity 为 B，getCurrentResumedActivity 为 B
     *
     *  在具体的使用中要特别注意
     */
    fun getCurrentResumedActivity(): Activity? {
        return mSoftReferenceOfResumedActivity?.get()
    }

    fun getActivityList(): List<Activity> {
        return mActivityList
    }

    fun hasActivity(cmpName: String): Boolean {
        mActivityList.forEach {
            if (it.javaClass.name == cmpName) {
                return true
            }
        }
        return false
    }

    override fun onActivityPaused(activity: Activity?) {
        ZLog.d(TAG, "onActivityPaused: ${activity?.javaClass?.simpleName}(${System.identityHashCode(activity)})")
    }

    override fun onActivityResumed(activity: Activity?) {
        ZLog.d(TAG, "onActivityResumed: ${activity?.javaClass?.simpleName}(${System.identityHashCode(activity)})")
        activity?.let {
            mSoftReferenceOfCurrentActivity = SoftReference(activity)
            mSoftReferenceOfResumedActivity = SoftReference(activity)
        }
        ZLog.d(TAG, "Current Activitty: ${activity?.javaClass?.simpleName}(${System.identityHashCode(activity)})")
    }

    override fun onActivityStarted(activity: Activity?) {
        ZLog.d(TAG, "onActivityStarted: ${activity?.javaClass?.simpleName}(${System.identityHashCode(activity)})")
    }

    override fun onActivityDestroyed(activity: Activity?) {
        ZLog.d(TAG, "onActivityDestroyed: ${activity?.javaClass?.simpleName}(${System.identityHashCode(activity)})")
        try {
            activity?.let {
                mActivityList.remove(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
        ZLog.d(TAG, "onActivitySaveInstanceState: ${activity?.javaClass?.simpleName}(${System.identityHashCode(activity)})")
    }

    override fun onActivityStopped(activity: Activity?) {
        ZLog.d(TAG, "onActivityStopped: ${activity?.javaClass?.simpleName}(${System.identityHashCode(activity)})")
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        ZLog.d(TAG, "onActivityCreated: ${activity?.javaClass?.simpleName}(${System.identityHashCode(activity)})")
        activity?.let {
            mActivityList.add(activity)
            mSoftReferenceOfCurrentActivity = SoftReference(activity)
        }
    }
}