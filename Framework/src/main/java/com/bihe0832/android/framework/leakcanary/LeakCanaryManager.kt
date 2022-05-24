package com.bihe0832.android.framework.leakcanary

import android.app.Application
import android.os.StrictMode
import com.bihe0832.android.framework.ZixieContext
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2022/5/23.
 * Description: Description
 *
 */
object LeakCanaryManager {

    private var refWatcher: RefWatcher? = null

    fun init(application: Application) {
        if (LeakCanary.isInAnalyzerProcess(application)) {
            return
        }
        if (!ZixieContext.isOfficial()) {
            val builder = StrictMode.VmPolicy.Builder()
            builder.detectAll()
            builder.penaltyLog()
            StrictMode.setVmPolicy(builder.build())
            refWatcher = LeakCanary.install(application)
        }
    }

    fun addWatch(item: Any?) {
        if (!ZixieContext.isOfficial()) {
            refWatcher?.watch(item)
        }
    }
}