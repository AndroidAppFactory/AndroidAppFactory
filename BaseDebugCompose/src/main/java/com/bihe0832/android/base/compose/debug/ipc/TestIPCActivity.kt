package com.bihe0832.android.base.compose.debug.ipc

import android.os.Process
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bihe0832.android.base.compose.debug.ipc.iservice.IZixieIPCTestServiceForMain
import com.bihe0832.android.base.compose.debug.ipc.iservice.IZixieIPCTestServiceForTest
import com.bihe0832.android.base.compose.debug.ipc.iservice.impl.ZixieIPCTestServiceForMain
import com.bihe0832.android.common.compose.debug.DebugBaseComposeActivity
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.lib.ipc.ServiceManager
import com.bihe0832.android.lib.log.ZLog


open class TestIPCActivity : DebugBaseComposeActivity() {

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                Text(modifier = Modifier.fillMaxSize().clickable {
                    ZLog.d(ZixieIPCTestServiceForMain.TAG, "-------")
                    ZLog.d(ZixieIPCTestServiceForMain.TAG, "TestIPCActivity at process:${Process.myPid()} at Thread:${Thread.currentThread()}")
                    ServiceManager.getService(IZixieIPCTestServiceForMain::class.java)?.test()
                    ServiceManager.getService(IZixieIPCTestServiceForMain::class.java)?.testMain()
                    ServiceManager.getService(IZixieIPCTestServiceForTest::class.java)?.testOther()
                    ZLog.d(ZixieIPCTestServiceForMain.TAG, TestForIPC.getTestInfo().toString())
                    ZLog.d(ZixieIPCTestServiceForMain.TAG, ServiceManager.getService(IZixieIPCTestServiceForMain::class.java)?.testInfo.toString())
                    ZLog.d(ZixieIPCTestServiceForMain.TAG, ServiceManager.getService(IZixieIPCTestServiceForMain::class.java)?.testMainInfo.toString())
                    ZLog.d(ZixieIPCTestServiceForMain.TAG, ServiceManager.getService(
                        IZixieIPCTestServiceForTest::class.java)?.testOtherInfo.toString())
                    TestObjectForIPC().apply {
                        msg = "TestObjectForIPC"
                        flag = Process.myPid()
                    }.let {
                        ServiceManager.getService(IZixieIPCTestServiceForMain::class.java)?.setMainInfo(it)
                        ServiceManager.getService(IZixieIPCTestServiceForTest::class.java)?.setOtherInfo(it)
                    }
                    ZLog.d(ZixieIPCTestServiceForMain.TAG, "-------")
                }, text = "点击测试多进程")
            }
        }
    }
}