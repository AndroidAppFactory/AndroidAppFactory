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
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/6/23.
 * Description: Description
 *
 */
abstract class AAFActivityLifecycleChangedListener : Application.ActivityLifecycleCallbacks {

    abstract override fun onActivityResumed(activity: Activity)

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityStopped(activity: Activity) {}
}