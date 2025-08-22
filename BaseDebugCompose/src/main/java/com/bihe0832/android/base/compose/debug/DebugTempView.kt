/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.compose.debug

import androidx.compose.runtime.Composable
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent


@Composable
fun DebugTempView() {
    DebugContent {
        DebugItem("通用测试预处理") {

        }
        DebugItem("简单测试函数") {

        }
    }
}
