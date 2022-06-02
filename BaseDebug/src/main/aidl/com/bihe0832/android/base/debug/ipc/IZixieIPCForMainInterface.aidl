// IProfileInterface.aidl
package com.bihe0832.android.base.debug.ipc;

import com.bihe0832.android.base.debug.ipc.TestObjectForIPC;


interface IZixieIPCForMainInterface {

    void test();

    void testMain();

    TestObjectForIPC getTestMainInfo();

    void setMainInfo(in TestObjectForIPC testObj);
}
