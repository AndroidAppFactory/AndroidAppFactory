package com.bihe0832.android.base.compose.debug.ipc.iservice.impl;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Process;
import com.bihe0832.android.base.compose.debug.ipc.IZixieIPCForTestInterface;
import com.bihe0832.android.base.compose.debug.ipc.TestObjectForIPC;
import com.bihe0832.android.base.compose.debug.ipc.iservice.IZixieIPCTestServiceForTest;
import com.bihe0832.android.lib.log.ZLog;

/**
 * @author zixie code@bihe0832.com Created on 5/19/21.
 */
public class ZixieIPCTestServiceForTest extends IZixieIPCForTestInterface.Stub implements IZixieIPCTestServiceForTest {

    public static final String TAG = "TestIPCActivity";

    @Override
    public IBinder getBinder() {
        return this;
    }

    @Override
    public IInterface getInterface(IBinder binder) {
        return asInterface(binder);
    }

    @Override
    public void testOther() {
        ZLog.d(TAG, "testOther at process:" + Process.myPid() + " at Thread:" + Thread
                .currentThread());
    }

    @Override
    public TestObjectForIPC getTestOtherInfo() {
        ZLog.d(TAG, "getTestOtherInfo at process:" + Process.myPid() + " at Thread:" + Thread.currentThread());
        TestObjectForIPC testObjectForIPC = new TestObjectForIPC();
        testObjectForIPC.flag = Process.myPid();
        return testObjectForIPC;
    }

    @Override
    public void setOtherInfo(TestObjectForIPC testObj) {
        ZLog.d(TAG,
                "setOtherInfo at process:" + Process.myPid() + " at Thread:" + Thread.currentThread());
        ZLog.d(TAG, testObj.toString());
    }


}
