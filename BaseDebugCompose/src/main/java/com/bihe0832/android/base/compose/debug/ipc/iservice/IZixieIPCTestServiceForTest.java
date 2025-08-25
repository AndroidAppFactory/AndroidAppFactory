package com.bihe0832.android.base.compose.debug.ipc.iservice;

import com.bihe0832.android.base.compose.debug.ipc.TestObjectForIPC;
import com.bihe0832.android.lib.ipc.annotation.Process;
import com.bihe0832.android.lib.ipc.iservice.IService;
import com.bihe0832.android.lib.ipc.annotation.SupportMultiProcess;

@SupportMultiProcess
public interface IZixieIPCTestServiceForTest extends IService {

    @Process(name = "other")
    void testOther();

    @Process(name = "other")
    TestObjectForIPC getTestOtherInfo();

    @Process(name = "other")
    void setOtherInfo(TestObjectForIPC testObj);
}
