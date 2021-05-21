package com.bihe0832.android.base.test.ipc

import android.os.Bundle
import android.os.Process
import com.bihe0832.android.base.test.R
import com.bihe0832.android.base.test.ipc.iservice.IZixieIPCTestServiceForMain
import com.bihe0832.android.base.test.ipc.iservice.IZixieIPCTestServiceForTest
import com.bihe0832.android.base.test.ipc.iservice.impl.ZixieIPCTestServiceForMain
import com.bihe0832.android.common.ipc.ServiceManager
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.lib.log.ZLog
import kotlinx.android.synthetic.main.activity_test_text.*


open class TestIPCActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_test_text)
        result.apply {
            text = "点击测试多进程"
            setOnClickListener {
                ZLog.d(ZixieIPCTestServiceForMain.TAG, "-------")
                ZLog.d(ZixieIPCTestServiceForMain.TAG, "TestIPCActivity at process:${Process.myPid()} at Thread:${Thread.currentThread()}")
                ServiceManager.getService(IZixieIPCTestServiceForMain::class.java)?.test()
                ServiceManager.getService(IZixieIPCTestServiceForMain::class.java)?.testMain()
                ServiceManager.getService(IZixieIPCTestServiceForTest::class.java)?.testOther()
                ZLog.d(ZixieIPCTestServiceForMain.TAG, TestForIPC.getTestInfo().toString())
                ZLog.d(ZixieIPCTestServiceForMain.TAG, ServiceManager.getService(IZixieIPCTestServiceForMain::class.java)?.testInfo.toString())
                ZLog.d(ZixieIPCTestServiceForMain.TAG, ServiceManager.getService(IZixieIPCTestServiceForMain::class.java)?.testMainInfo.toString())
                ZLog.d(ZixieIPCTestServiceForMain.TAG, ServiceManager.getService(IZixieIPCTestServiceForTest::class.java)?.testOtherInfo.toString())
                TestObjectForIPC().apply {
                    msg = "TestObjectForIPC"
                    flag = Process.myPid()
                }.let {
                    ServiceManager.getService(IZixieIPCTestServiceForMain::class.java)?.setMainInfo(it)
                    ServiceManager.getService(IZixieIPCTestServiceForTest::class.java)?.setOtherInfo(it)
                }
                ZLog.d(ZixieIPCTestServiceForMain.TAG, "-------")
            }
        }
    }
}