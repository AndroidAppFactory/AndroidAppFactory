package com.bihe0832.android.base.compose.debug.ipc

import android.os.Process

/**
 *
 * @author zixie code@bihe0832.com Created on 5/20/21.
 *
 */
object TestForIPC {

    fun getTestInfo(): TestObjectForIPC {
        return TestObjectForIPC().apply {
            ret = 0
            flag = Process.myPid()
            msg = "Info: " + Process.myPid() + " name " + Thread.currentThread()
        }
    }

}