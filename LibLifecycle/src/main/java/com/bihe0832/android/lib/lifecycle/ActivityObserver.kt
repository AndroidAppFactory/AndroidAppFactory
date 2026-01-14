package com.bihe0832.android.lib.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.bihe0832.android.lib.log.ZLog
import java.lang.ref.SoftReference
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Activity 生命周期观察者
 *
 * 实现 Application.ActivityLifecycleCallbacks 接口，提供全局 Activity 生命周期监听功能：
 * - 维护当前 Activity 栈列表
 * - 提供获取当前 Activity、最近创建的 Activity 的方法
 * - 支持自定义 Activity 生命周期监听器
 * - 检测所有 Activity 销毁并通知 ApplicationObserver
 *
 * 使用方式：
 * ```kotlin
 * // 获取当前Activity
 * val currentActivity = ActivityObserver.getCurrentActivity()
 *
 * // 获取当前在前台的Activity
 * val resumedActivity = ActivityObserver.getCurrentResumedActivity()
 *
 * // 获取Activity列表
 * val activityList = ActivityObserver.getActivityList()
 *
 * // 检查是否存在某个Activity
 * val hasMain = ActivityObserver.hasActivity("com.example.MainActivity")
 * ```
 *
 * @author zixie code@bihe0832.com
 * Created on 2020/5/19.
 */
object ActivityObserver : Application.ActivityLifecycleCallbacks {

    /** 日志标签 */
    const val TAG = "ActivityObserver"

    /** Activity 列表，使用线程安全的 CopyOnWriteArrayList 存储所有已创建未销毁的 Activity */
    private val mActivityList = CopyOnWriteArrayList<Activity>()

    /** 最后一次创建的 Activity 的软引用，避免内存泄漏 */
    private var mSoftReferenceOfCurrentActivity: SoftReference<Activity>? = null

    /** 当前处于 Resumed 状态的 Activity 的软引用，避免内存泄漏 */
    private var mSoftReferenceOfResumedActivity: SoftReference<Activity>? = null

    /** 自定义 Activity 生命周期变化监听器 */
    private var mAAFActivityLifecycleChangedListener: AAFActivityLifecycleChangedListener? = null

    /**
     * 返回最后一次创建的 Activity
     *
     * 注意：返回的是最后调用 onCreate 的 Activity，不一定是当前可见的 Activity
     *
     * @return 最后创建的 Activity，如果不存在则返回 null
     */
    fun getCurrentActivity(): Activity? {
        return mSoftReferenceOfCurrentActivity?.get()
    }

    /**
     * 返回当前在前台的 Activity
     *
     * 与 getCurrentActivity 的区别：
     * - 如果当前 Activity 为 A，新创建一个 Activity 为 B
     * - 在 B onResume 之前：getCurrentActivity 为 B，getCurrentResumedActivity 为 A
     * - 在 B onResume 之后：getCurrentActivity 为 B，getCurrentResumedActivity 为 B
     *
     * 在具体使用中要特别注意这个区别
     *
     * @return 当前处于 Resumed 状态的 Activity，如果不存在则返回 null
     */
    fun getCurrentResumedActivity(): Activity? {
        return mSoftReferenceOfResumedActivity?.get()
    }

    /**
     * 获取当前所有已创建未销毁的 Activity 列表
     *
     * @return Activity 列表，按创建顺序排列
     */
    fun getActivityList(): List<Activity> {
        return mActivityList
    }

    /**
     * 检查是否存在指定类名的 Activity
     *
     * @param cmpName Activity 的完整类名，如 "com.example.MainActivity"
     * @return true 表示存在该 Activity，false 表示不存在
     */
    fun hasActivity(cmpName: String): Boolean {
        mActivityList.forEach {
            if (it.javaClass.name == cmpName) {
                return true
            }
        }
        return false
    }

    /**
     * 设置自定义 Activity 生命周期变化监听器
     *
     * 设置后，所有 Activity 的生命周期回调都会同时通知到该监听器
     *
     * @param changedListener 生命周期变化监听器，传 null 可移除监听
     */
    fun setActivityLifecycleChangedListener(changedListener: AAFActivityLifecycleChangedListener?) {
        mAAFActivityLifecycleChangedListener = changedListener
    }

    /**
     * Activity 创建时的回调
     *
     * 将 Activity 添加到列表，并更新当前 Activity 引用
     *
     * @param activity 被创建的 Activity
     * @param savedInstanceState 保存的状态数据
     */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        ZLog.d(TAG, "onActivityCreated: ${activity.javaClass.simpleName}(${System.identityHashCode(activity)}) - taskid:${activity.taskId}")
        mActivityList.add(activity)
        mSoftReferenceOfCurrentActivity = SoftReference(activity)
        mAAFActivityLifecycleChangedListener?.onActivityCreated(activity, savedInstanceState)
    }

    /**
     * Activity 恢复到前台时的回调
     *
     * 更新当前 Activity 和 Resumed Activity 的引用
     *
     * @param activity 恢复到前台的 Activity
     */
    override fun onActivityResumed(activity: Activity) {
        ZLog.d(TAG, "onActivityResumed: ${activity.javaClass.simpleName}(${System.identityHashCode(activity)}) - taskid:${activity.taskId}")
        mSoftReferenceOfCurrentActivity = SoftReference(activity)
        mSoftReferenceOfResumedActivity = SoftReference(activity)
        mAAFActivityLifecycleChangedListener?.onActivityResumed(activity)
    }

    /**
     * Activity 暂停时的回调
     *
     * @param activity 被暂停的 Activity
     */
    override fun onActivityPaused(activity: Activity) {
        ZLog.d(TAG, "onActivityPaused: ${activity.javaClass.simpleName}(${System.identityHashCode(activity)}) - taskid:${activity.taskId}")
        mAAFActivityLifecycleChangedListener?.onActivityPaused(activity)
    }

    /**
     * Activity 启动时的回调
     *
     * @param activity 被启动的 Activity
     */
    override fun onActivityStarted(activity: Activity) {
        ZLog.d(TAG, "onActivityStarted: ${activity.javaClass.simpleName}(${System.identityHashCode(activity)}) - taskid:${activity.taskId}")
        mAAFActivityLifecycleChangedListener?.onActivityStarted(activity)
    }

    /**
     * Activity 销毁时的回调
     *
     * 从列表中移除 Activity，如果是最后一个 Activity 则通知 ApplicationObserver
     *
     * @param activity 被销毁的 Activity
     */
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

    /**
     * Activity 保存实例状态时的回调
     *
     * @param activity 正在保存状态的 Activity
     * @param outState 用于保存状态的 Bundle
     */
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        ZLog.d(TAG, "onActivitySaveInstanceState: ${activity.javaClass.simpleName}(${System.identityHashCode(activity)}) - taskid:${activity.taskId}")
        mAAFActivityLifecycleChangedListener?.onActivitySaveInstanceState(activity, outState)

    }

    /**
     * Activity 停止时的回调
     *
     * @param activity 被停止的 Activity
     */
    override fun onActivityStopped(activity: Activity) {
        ZLog.d(TAG, "onActivityStopped: ${activity.javaClass.simpleName}(${System.identityHashCode(activity)}) - taskid:${activity.taskId}")
        mAAFActivityLifecycleChangedListener?.onActivityStopped(activity)
    }
}