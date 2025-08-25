package com.bihe0832.android.base.compose.debug.ipc.iservice;

import com.bihe0832.android.base.compose.debug.ipc.TestObjectForIPC;
import com.bihe0832.android.lib.ipc.annotation.Process;
import com.bihe0832.android.lib.ipc.iservice.IService;
import com.bihe0832.android.lib.ipc.annotation.SupportMultiProcess;

@SupportMultiProcess
public interface IZixieIPCTestServiceForMain extends IService {

    void test();

    TestObjectForIPC getTestInfo();

    void setTestInfo(TestObjectForIPC testObj);

    @Process(name = "main")
    void testMain();

    @Process(name = "main")
    TestObjectForIPC getTestMainInfo();

    @Process(name = "main")
    void setMainInfo(TestObjectForIPC testObj);
}
