package com.bihe0832.android.base.test.ipc.iservice;

import com.bihe0832.android.base.test.ipc.TestObjectForIPC;
import com.bihe0832.android.common.ipc.annotation.Process;
import com.bihe0832.android.common.ipc.iservice.IService;
import com.bihe0832.android.common.ipc.annotation.SupportMultiProcess;

@SupportMultiProcess
public interface IZixieIPCTestServiceForTest extends IService {

    @Process(name = "other")
    void testOther();

    @Process(name = "other")
    TestObjectForIPC getTestOtherInfo();

    @Process(name = "other")
    void setOtherInfo(TestObjectForIPC testObj);
}
