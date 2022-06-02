// IProfileInterface.aidl
package com.bihe0832.android.base.debug.ipc;

import com.bihe0832.android.base.debug.ipc.TestObjectForIPC;

interface IZixieIPCForTestInterface {

    void testOther();

     TestObjectForIPC getTestOtherInfo();

    void setOtherInfo(in TestObjectForIPC testObj);
}
