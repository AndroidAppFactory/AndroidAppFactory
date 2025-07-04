package com.bihe0832.android.base.debug

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import com.bihe0832.android.app.Application
import com.bihe0832.android.base.compose.debug.AAFDebugCompose
import com.bihe0832.android.base.debug.ipc.iservice.IZixieIPCTestServiceForMain
import com.bihe0832.android.base.debug.ipc.iservice.IZixieIPCTestServiceForTest
import com.bihe0832.android.base.debug.ipc.iservice.impl.ZixieIPCTestServiceForMain
import com.bihe0832.android.base.debug.ipc.iservice.impl.ZixieIPCTestServiceForTest
import com.bihe0832.android.lib.ipc.ServiceManager
import com.bihe0832.android.lib.log.ZLog

/**
 * @author zixie code@bihe0832.com Created on 5/20/21.
 */
class TestApplication : Application() {
    private val TAG = "TestApplication"
    override fun onCreate() {
        super.onCreate()
        AAFDebugCompose.init()
        val am = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningApps = am.runningAppProcesses
        for (it in runningApps) {
            if (it.pid == Process.myPid() && it.processName != null && it.processName.contains(
                    applicationContext.packageName
                )
            ) {
                ZLog.e(
                    TAG,
                    "Application initCore process: name:" + it.processName + " and id:" + it.pid
                )
            }
        }
        ServiceManager.initApplication(this)
        ServiceManager.registerProcess("main")
        ServiceManager.registerProcess("test")
        ServiceManager.registerService(
            IZixieIPCTestServiceForMain::class.java,
            ZixieIPCTestServiceForMain()
        )
        ServiceManager.registerService(
            IZixieIPCTestServiceForTest::class.java,
            ZixieIPCTestServiceForTest()
        )
    }

    override fun skipPrivacy(): Boolean {
        return true
    }

}