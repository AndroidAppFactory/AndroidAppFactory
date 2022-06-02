// IProfileInterface.aidl
package com.bihe0832.android.base.test.ipc;

import com.bihe0832.android.base.test.ipc.TestObjectForIPC;

interface IZixieIPCForTestInterface {

    void testOther();

     TestObjectForIPC getTestOtherInfo();

    void setOtherInfo(in TestObjectForIPC testObj);
}
