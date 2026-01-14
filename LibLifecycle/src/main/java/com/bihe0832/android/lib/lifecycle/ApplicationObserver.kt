package com.bihe0832.android.lib.lifecycle

import androidx.lifecycle.*
import com.bihe0832.android.lib.log.ZLog
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 应用程序生命周期观察者
 *
 * 基于 ProcessLifecycleOwner 实现的应用前后台状态监听器，提供以下功能：
 * - 监听应用进入前台/后台事件
 * - 记录应用启动时间、最后暂停时间、最后恢复时间
 * - 监听所有 Activity 销毁事件
 * - 支持多个监听器同时注册
 *
 * 使用方式：
 * ```kotlin
 * // 监听前后台切换
 * ApplicationObserver.addStatusChangeListener(object : APPStatusChangeListener {
 *     override fun onForeground() { /* 应用进入前台 */ }
 *     override fun onBackground() { /* 应用进入后台 */ }
 * })
 *
 * // 监听所有Activity销毁
 * ApplicationObserver.addDestroyListener(object : APPDestroyListener {
 *     override fun onAllActivityDestroyed() { /* 所有Activity已销毁 */ }
 * })
 * ```
 *
 * @author zixie code@bihe0832.com
 * Created on 2020/5/19.
 */
object ApplicationObserver : DefaultLifecycleObserver {

    /** 日志标签 */
    private const val TAG = "ApplicationObserver"

    /** 应用启动时间戳（毫秒） */
    private var mAPPStartTime = 0L

    /** 应用最后一次进入后台的时间戳（毫秒） */
    private var mLastPauseTime = 0L

    /** 应用最后一次进入前台的时间戳（毫秒） */
    private var mLastResumeTime = 0L

    /** 应用是否处于后台状态 */
    private var mIsAPPBackground = true

    /** 应用前后台状态变化监听器列表，使用线程安全的 CopyOnWriteArrayList */
    private val mAPPStatusChangeListenerList = CopyOnWriteArrayList<APPStatusChangeListener>()

    /** 应用销毁监听器列表，使用线程安全的 CopyOnWriteArrayList */
    private val mAPPDestroyChangeListenerList = CopyOnWriteArrayList<APPDestroyListener>()


    /**
     * 应用前后台状态变化监听器接口
     *
     * 用于监听应用在前台和后台之间的切换事件
     */
    interface APPStatusChangeListener {
        /**
         * 应用进入前台时回调
         *
         * 当应用从后台切换到前台时触发，通常用于：
         * - 恢复暂停的任务
         * - 刷新数据
         * - 恢复动画等
         */
        fun onForeground()

        /**
         * 应用进入后台时回调
         *
         * 当应用从前台切换到后台时触发，通常用于：
         * - 暂停耗资源的任务
         * - 保存状态
         * - 停止动画等
         */
        fun onBackground()
    }

    /**
     * 应用销毁监听器接口
     *
     * 用于监听应用中所有 Activity 都被销毁的事件
     */
    interface APPDestroyListener {
        /**
         * 所有 Activity 销毁时回调
         *
         * 当应用中最后一个 Activity 被销毁时触发，通常用于：
         * - 清理全局资源
         * - 保存应用状态
         * - 执行退出前的清理工作
         */
        fun onAllActivityDestroyed()
    }

    init {
        mAPPStartTime = System.currentTimeMillis()
    }

    /**
     * 添加应用前后台状态变化监听器
     *
     * @param listener 状态变化监听器，不会重复添加
     */
    fun addStatusChangeListener(listener: APPStatusChangeListener) {
        if (!mAPPStatusChangeListenerList.contains(listener)) {
            mAPPStatusChangeListenerList.add(listener)
        }
    }

    /**
     * 移除应用前后台状态变化监听器
     *
     * @param listener 要移除的状态变化监听器
     */
    fun removeStatusChangeListener(listener: APPStatusChangeListener) {
        if (mAPPStatusChangeListenerList.contains(listener)) {
            mAPPStatusChangeListenerList.remove(listener)
        }
    }

    /**
     * 添加应用销毁监听器
     *
     * @param listener 销毁监听器，不会重复添加
     */
    fun addDestroyListener(listener: APPDestroyListener) {
        if (!mAPPDestroyChangeListenerList.contains(listener)) {
            mAPPDestroyChangeListenerList.add(listener)
        }
    }

    /**
     * 移除应用销毁监听器
     *
     * @param listener 要移除的销毁监听器
     */
    fun removeDestroyListener(listener: APPDestroyListener) {
        if (mAPPDestroyChangeListenerList.contains(listener)) {
            mAPPDestroyChangeListenerList.remove(listener)
        }
    }

    /**
     * 判断应用是否处于后台状态
     *
     * @return true 表示应用在后台，false 表示应用在前台
     */
    fun isAPPBackground(): Boolean {
        return mIsAPPBackground
    }

    /**
     * 获取应用最后一次进入后台的时间
     *
     * @return 最后一次进入后台的时间戳（毫秒），如果从未进入后台则返回 0
     */
    fun getLastPauseTime(): Long {
        return mLastPauseTime
    }

    /**
     * 获取应用最后一次进入前台的时间
     *
     * @return 最后一次进入前台的时间戳（毫秒），如果从未进入前台则返回 0
     */
    fun getLastResumedTime(): Long {
        return mLastResumeTime
    }

    /**
     * 获取应用启动时间
     *
     * @return 应用启动时的时间戳（毫秒）
     */
    fun getAPPStartTime(): Long {
        return mAPPStartTime
    }

    /**
     * 应用进入前台时的生命周期回调
     *
     * 由 ProcessLifecycleOwner 自动触发，会通知所有注册的状态变化监听器
     *
     * @param owner 生命周期所有者
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        ZLog.d(TAG, "onForeground!")
        mIsAPPBackground = false
        mLastResumeTime = System.currentTimeMillis()
        mAPPStatusChangeListenerList.forEach {
            it.onForeground()
        }
    }

    /**
     * 应用进入后台时的生命周期回调
     *
     * 由 ProcessLifecycleOwner 自动触发，会通知所有注册的状态变化监听器
     *
     * @param owner 生命周期所有者
     */
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        ZLog.d(TAG, "onBackground!")
        mLastPauseTime = System.currentTimeMillis()
        mIsAPPBackground = true
        mAPPStatusChangeListenerList.forEach {
            it.onBackground()
        }
    }


    /**
     * 所有 Activity 销毁时的回调
     *
     * 由 ActivityObserver 在检测到所有 Activity 都被销毁时调用，
     * 会通知所有注册的销毁监听器
     */
    fun onAllActivityDestroyed() {
        ZLog.d(TAG, "onAllActivityDestroy!")
        mAPPDestroyChangeListenerList.forEach {
            it.onAllActivityDestroyed()
        }
    }
}