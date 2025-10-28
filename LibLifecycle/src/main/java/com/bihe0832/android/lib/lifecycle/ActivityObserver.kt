package com.bihe0832.android.lib.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.bihe0832.android.lib.log.ZLog
import java.lang.ref.SoftReference
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author zixie code@bihe0832.com
 * Created on 2020/5/19.
 * Description: Description
 */
object ActivityObserver : Application.ActivityLifecycleCallbacks {

    const val TAG = "ActivityObserver"
    private val mActivityList = CopyOnWriteArrayList<Activity>()
    private var mSoftReferenceOfCurrentActivity: SoftReference<Activity>? = null
    private var mSoftReferenceOfResumedActivity: SoftReference<Activity>? = null
    private var mAAFActivityLifecycleChangedListener: AAFActivityLifecycleChangedListener? = null

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

    fun setActivityLifecycleChangedListener(changedListener: AAFActivityLifecycleChangedListener?) {
        mAAFActivityLifecycleChangedListener = changedListener
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        ZLog.d(TAG, "onActivityCreated: ${activity.javaClass.simpleName}(${System.identityHashCode(activity)}) - taskid:${activity.taskId}")
        mActivityList.add(activity)
        mSoftReferenceOfCurrentActivity = SoftReference(activity)
        mAAFActivityLifecycleChangedListener?.onActivityCreated(activity, savedInstanceState)
    }

    override fun onActivityResumed(activity: Activity) {
        ZLog.d(TAG, "onActivityResumed: ${activity.javaClass.simpleName}(${System.identityHashCode(activity)}) - taskid:${activity.taskId}")
        mSoftReferenceOfCurrentActivity = SoftReference(activity)
        mSoftReferenceOfResumedActivity = SoftReference(activity)
        mAAFActivityLifecycleChangedListener?.onActivityResumed(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        ZLog.d(TAG, "onActivityPaused: ${activity.javaClass.simpleName}(${System.identityHashCode(activity)}) - taskid:${activity.taskId}")
        mAAFActivityLifecycleChangedListener?.onActivityPaused(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        ZLog.d(TAG, "onActivityStarted: ${activity.javaClass.simpleName}(${System.identityHashCode(activity)}) - taskid:${activity.taskId}")
        mAAFActivityLifecycleChangedListener?.onActivityStarted(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
        ZLog.d(TAG, "onActivityDestroyed: ${activity.javaClass.simpleName}(${System.identityHashCode(activity)}) - taskid:${activity.taskId}")
        try {
            mActivityList.remove(activity)
            if (mActivityList.size == 0) {
                ZLog.d(TAG, "app has no activity after onActivityDestroyed: ${activity.javaClass.simpleName}(${System.identityHashCode(activity)})")
                ApplicationObserver.onAllActivityDestroyed()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mAAFActivityLifecycleChangedListener?.onActivityDestroyed(activity)

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        ZLog.d(TAG, "onActivitySaveInstanceState: ${activity.javaClass.simpleName}(${System.identityHashCode(activity)}) - taskid:${activity.taskId}")
        mAAFActivityLifecycleChangedListener?.onActivitySaveInstanceState(activity, outState)

    }

    override fun onActivityStopped(activity: Activity) {
        ZLog.d(TAG, "onActivityStopped: ${activity.javaClass.simpleName}(${System.identityHashCode(activity)}) - taskid:${activity.taskId}")
        mAAFActivityLifecycleChangedListener?.onActivityStopped(activity)
    }
}