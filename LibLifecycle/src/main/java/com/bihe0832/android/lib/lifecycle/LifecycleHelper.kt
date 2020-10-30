package com.bihe0832.android.lib.lifecycle

import android.app.Application
import android.arch.lifecycle.ProcessLifecycleOwner
import android.content.Context

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2020/10/30.
 * Description: Description
 *
 */
object LifecycleHelper {

    var applicationContext: Context? = null
        private set
    @Synchronized
    fun init(application: Application) {
        applicationContext = application.applicationContext
        application.registerActivityLifecycleCallbacks(ActivityObserver)
        ProcessLifecycleOwner.get().lifecycle.addObserver(ApplicationObserver)
    }
}