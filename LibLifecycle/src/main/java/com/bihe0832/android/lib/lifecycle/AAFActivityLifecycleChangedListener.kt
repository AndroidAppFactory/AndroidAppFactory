/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/23 下午5:27
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/23 下午5:27
 *
 */

package com.bihe0832.android.lib.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * AAF Activity 生命周期变化监听器抽象类
 *
 * 提供 Activity 生命周期回调的简化实现，只需实现必须的 onActivityResumed 方法，
 * 其他生命周期方法提供空实现，可按需重写。
 *
 * 使用方式：
 * ```kotlin
 * ActivityObserver.setActivityLifecycleChangedListener(object : AAFActivityLifecycleChangedListener() {
 *     override fun onActivityResumed(activity: Activity) {
 *         // 处理 Activity 恢复事件
 *     }
 *
 *     override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
 *         // 可选：处理 Activity 创建事件
 *     }
 * })
 * ```
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/6/23.
 */
abstract class AAFActivityLifecycleChangedListener : Application.ActivityLifecycleCallbacks {

    /**
     * Activity 恢复到前台时的回调（必须实现）
     *
     * @param activity 恢复到前台的 Activity
     */
    abstract override fun onActivityResumed(activity: Activity)

    /**
     * Activity 创建时的回调（可选重写）
     *
     * @param activity 被创建的 Activity
     * @param savedInstanceState 保存的状态数据
     */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    /**
     * Activity 销毁时的回调（可选重写）
     *
     * @param activity 被销毁的 Activity
     */
    override fun onActivityDestroyed(activity: Activity) {}

    /**
     * Activity 暂停时的回调（可选重写）
     *
     * @param activity 被暂停的 Activity
     */
    override fun onActivityPaused(activity: Activity) {}

    /**
     * Activity 启动时的回调（可选重写）
     *
     * @param activity 被启动的 Activity
     */
    override fun onActivityStarted(activity: Activity) {}

    /**
     * Activity 保存实例状态时的回调（可选重写）
     *
     * @param activity 正在保存状态的 Activity
     * @param outState 用于保存状态的 Bundle
     */
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    /**
     * Activity 停止时的回调（可选重写）
     *
     * @param activity 被停止的 Activity
     */
    override fun onActivityStopped(activity: Activity) {}
}