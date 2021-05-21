package com.bihe0832.android.test

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import com.bihe0832.android.app.Application
import com.bihe0832.android.base.test.ipc.iservice.IZixieIPCTestServiceForMain
import com.bihe0832.android.base.test.ipc.iservice.IZixieIPCTestServiceForTest
import com.bihe0832.android.base.test.ipc.iservice.impl.ZixieIPCTestServiceForMain
import com.bihe0832.android.base.test.ipc.iservice.impl.ZixieIPCTestServiceForMain.TAG
import com.bihe0832.android.base.test.ipc.iservice.impl.ZixieIPCTestServiceForTest
import com.bihe0832.android.common.ipc.ServiceManager
import com.bihe0832.android.lib.log.ZLog

/**
 * @author hardyshi code@bihe0832.com Created on 5/20/21.
 */
class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val am = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningApps = am.runningAppProcesses
        for (it in runningApps) {
            if (it.pid == Process.myPid() && it.processName != null && it.processName.contains(applicationContext.packageName)) {
                ZLog.e(TAG, "Application initCore process: name:" + it.processName + " and id:" + it.pid)
            }
        }

        ServiceManager.registerProcess("main")
        ServiceManager.registerProcess("test")
        ServiceManager.registerService(IZixieIPCTestServiceForMain::class.java, ZixieIPCTestServiceForMain())
        ServiceManager.registerService(IZixieIPCTestServiceForTest::class.java, ZixieIPCTestServiceForTest())
    }
}