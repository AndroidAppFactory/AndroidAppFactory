package com.bihe0832.android.base.compose.debug.ipc.iservice.impl;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Process;
import com.bihe0832.android.base.compose.debug.ipc.IZixieIPCForMainInterface;
import com.bihe0832.android.base.compose.debug.ipc.TestObjectForIPC;
import com.bihe0832.android.base.compose.debug.ipc.iservice.IZixieIPCTestServiceForMain;
import com.bihe0832.android.lib.log.ZLog;

/**
 * @author zixie code@bihe0832.com Created on 5/19/21.
 */
public class ZixieIPCTestServiceForMain extends IZixieIPCForMainInterface.Stub implements IZixieIPCTestServiceForMain {

    public static final String TAG = "TestIPCActivity";


    @Override
    public void test() {
        ZLog.d(TAG,
                "test at process:" + Process.myPid() + " at Thread:" + Thread.currentThread());
    }

    @Override
    public TestObjectForIPC getTestInfo() {
        ZLog.d(TAG, "getTestInfo at process:" + Process.myPid() + " at Thread:" + Thread.currentThread());
        TestObjectForIPC testObjectForIPC = new TestObjectForIPC();
        testObjectForIPC.flag = Process.myPid();
        return testObjectForIPC;
    }

    @Override
    public void setTestInfo(TestObjectForIPC testObj) {
        ZLog.d(TAG,
                "testInfo at process:" + Process.myPid() + " at Thread:" + Thread.currentThread());
        ZLog.d(TAG, testObj.toString());
    }

    @Override
    public void testMain() {
        ZLog.d(TAG, "testMain at process:" + Process.myPid() + " at Thread:" + Thread
                .currentThread());

    }

    @Override
    public TestObjectForIPC getTestMainInfo() {
        ZLog.d(TAG,
                "getTestMainInfo at process:" + Process.myPid() + " at Thread:" + Thread.currentThread());
        return getTestInfo();
    }

    @Override
    public void setMainInfo(TestObjectForIPC testObj) {
        ZLog.d(TAG,
                "setMainInfo at process:" + Process.myPid() + " at Thread:" + Thread.currentThread());
        setTestInfo(testObj);
    }


    @Override
    public IBinder getBinder() {
        return this;
    }

    @Override
    public IInterface getInterface(IBinder binder) {
        return asInterface(binder);
    }
}
