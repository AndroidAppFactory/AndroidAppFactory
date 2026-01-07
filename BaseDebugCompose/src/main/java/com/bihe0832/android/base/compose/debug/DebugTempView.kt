/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.compose.debug

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.qrcode.QrcodeUtils

@Composable
fun DebugTempView() {
    DebugContent {
        val activity = LocalContext.current as? Activity
        DebugItem("通用测试预处理") {

        }
        DebugItem("简单测试函数") {
            QrcodeUtils.openQrScanAndParse()
        }
    }
}
