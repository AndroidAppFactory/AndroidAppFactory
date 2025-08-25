/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.compose.debug.ipc


import androidx.compose.runtime.Composable
import com.bihe0832.android.common.compose.debug.DebugUtilsV2.startActivityWithException
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent


@Composable
fun DebugIPCView() {
    DebugContent {
        DebugItem("多进程") { startActivityWithException(it, TestIPCActivity::class.java) }
        DebugItem("多进程1") { startActivityWithException(it, TestIPC1Activity::class.java) }
    }
}
